/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.komunumo.data.importer.jugs;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.data.service.SponsorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.Member.MEMBER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;
import static org.komunumo.data.db.tables.Sponsor.SPONSOR;

@SpringComponent
@SuppressWarnings({"SqlResolve", "removal", "java:S112", "java:S1192", "java:S3776"})
public class JUGSImporter {

    @Value("${JUGS_DB_URL}")
    private String dbURL;

    @Value("${JUGS_DB_USER}")
    private String dbUser;

    @Value("${JUGS_DB_PASS}")
    private String dbPass;

    @Bean
    public CommandLineRunner importFromJavaUserGroupSwitzerland(
            @NotNull final SponsorService sponsorService,
            @NotNull final MemberService memberService,
            @NotNull final EventService eventService,
            @NotNull final EventMemberService eventMemberService,
            @NotNull final SpeakerService speakerService,
            @NotNull final EventSpeakerService eventSpeakerService) {
        return args -> {
            Thread.sleep(30_000); // wait 30 seconds to be sure this import does not slow down the startup
            if (dbURL != null && dbUser != null && dbPass != null) {
                final var connection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                importSponsors(sponsorService, connection);
                importMembers(memberService, connection);
                importEvents(eventService, connection);
                importSpeakers(speakerService, eventSpeakerService, eventService, connection);
                importAttendees(eventMemberService, eventService, memberService, connection);
                updateEventLevel(eventService);
            }
        };
    }

    private void importAttendees(@NotNull final EventMemberService eventMemberService,
                                 @NotNull final EventService eventService,
                                 @NotNull final MemberService memberService,
                                 @NotNull final Connection connection)
            throws SQLException {
        if (eventMemberService.count() > 0) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT events_id, personen_id, aenderung, anmdatum, noshow FROM eventteiln");
            while (result.next()) {
                final var eventId = result.getLong("events_id");
                if (eventId == 0) {
                    continue;
                }
                final var memberId = result.getLong("personen_id");
                final var registerDate = getRegisterDate(result.getString("aenderung"), result.getString("anmdatum"));
                final var noShow = result.getString("noshow") != null && result.getString("noshow").equals("1");
                try {
                    eventMemberService.registerForEvent(eventId, memberId, registerDate, noShow);
                } catch (final Exception e1) {
                    if (memberService.get(memberId).isEmpty()) {
                        final var member = memberService.newMember();
                        member.setId(memberId);
                        member.setFirstName(RandomStringUtils.randomAlphabetic(32));
                        member.setLastName(RandomStringUtils.randomAlphabetic(32));
                        member.setEmail(RandomStringUtils.randomAlphabetic(32));
                        member.setMemberSince(registerDate);
                        member.setDeleted(true);
                        memberService.store(member);
                        try {
                            eventMemberService.registerForEvent(eventId, memberId, registerDate, noShow);
                        } catch (final Exception e2) {
                            if (eventService.get(eventId).isPresent()) {
                                throw e2;
                            }
                        }
                    } else {
                        if (eventService.get(eventId).isPresent()) {
                            throw e1;
                        }
                    }
                }
            }
        }

    }

    private LocalDateTime getRegisterDate(@Nullable final String aenderung, @Nullable final String anmdatum) {
        final var dateTime = aenderung != null && !aenderung.isBlank() && !aenderung.startsWith("0")
                ? getDateTime(aenderung) : null;
        final var date = anmdatum != null && !anmdatum.isBlank() && !anmdatum.startsWith("0")
                ? getDate(anmdatum) : null;

        if (dateTime != null && date != null) {
            return LocalDateTime.of(date, dateTime.toLocalTime());
        } else if (date != null) {
            return LocalDateTime.of(date, LocalTime.MIN);
        } else return Objects.requireNonNullElse(dateTime, LocalDateTime.MIN);
    }

    private void updateEventLevel(@NotNull final EventService eventService) {
        eventService.find(0, Integer.MAX_VALUE, null)
                .filter(record -> record.get(EVENT.VISIBLE))
                .filter(record -> record.get(EVENT.LEVEL) == null)
                .map(record -> eventService.get(record.get(EVENT.ID)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(event -> {
                    event.set(EVENT.LEVEL, EventLevel.All);
                    eventService.store(event);
                });
    }

    private void importSpeakers(@NotNull final SpeakerService speakerService,
                                @NotNull final EventSpeakerService eventSpeakerService,
                                @NotNull final EventService eventService,
                                @NotNull final Connection connection)
            throws SQLException {
        if (speakerService.count() > 0) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, vname, nname, firma, bio, image, e_mail, twitter, firmenurl, events_id, lang_talk, abstract, level FROM eventspeaker ORDER BY id DESC");
            while (result.next()) {
                final var speaker = getSpeaker(speakerService, result);
                if (speaker.get(SPEAKER.ID) == null
                        && (!getEmptyForNull(result.getString("vname")).isBlank()
                        || !getEmptyForNull(result.getString("nname")).isBlank())) {
                    speaker.set(SPEAKER.ID, result.getLong("id"));
                    speaker.set(SPEAKER.FIRST_NAME, getEmptyForNull(result.getString("vname")));
                    speaker.set(SPEAKER.LAST_NAME, getEmptyForNull(result.getString("nname")));
                    speaker.set(SPEAKER.COMPANY, getEmptyForNull(result.getString("firma")));
                    speaker.set(SPEAKER.BIO, getEmptyForNull(result.getString("bio")));
                    speaker.set(SPEAKER.PHOTO, getPhoto(result.getString("image")));
                    speaker.set(SPEAKER.EMAIL, getEmptyForNull(result.getString("e_mail")));
                    speaker.set(SPEAKER.TWITTER, getTwitter(result.getString("twitter")));
                    speaker.set(SPEAKER.WEBSITE, getEmptyForNull(result.getString("firmenurl")));
                    speakerService.store(speaker);
                }
                final var eventId = result.getLong("events_id");
                if (eventId > 0) {
                    final var event = eventService.get(eventId).orElse(null);
                    if (event != null) {
                        if (speaker.get(SPEAKER.ID) != null) {
                            final var speakers = eventSpeakerService.getSpeakersForEvent(event).collect(Collectors.toSet());
                            if (!speakers.contains(speaker)) {
                                speakers.add(speaker);
                                eventSpeakerService.setEventSpeakers(event, speakers);
                            }
                        }

                        var eventModified = false;

                        if (event.getLanguage() == null) {
                            final var langTalk = result.getString("lang_talk");
                            if (langTalk != null && !langTalk.isBlank()) {
                                final var language = EventLanguage.valueOf(langTalk.toUpperCase());
                                event.setLanguage(language);
                                eventModified = true;
                            }
                        }

                        if (event.getLevel() == null) {
                            final var levelTalk = result.getInt("level");
                            if (levelTalk >= 1 && levelTalk <= 3 || event.get(EVENT.VISIBLE)) {
                                EventLevel level;
                                switch (levelTalk) {
                                    case 1:
                                        level = EventLevel.Beginner;
                                        break;
                                    case 2:
                                        level = EventLevel.Intermediate;
                                        break;
                                    case 3:
                                        level = EventLevel.Advanced;
                                        break;
                                    default:
                                        level = EventLevel.All;
                                }
                                event.setLevel(level);
                                eventModified = true;
                            }
                        }

                        if (event.getDescription() == null || event.getDescription().isBlank()) {
                            final var description = result.getString("abstract");
                            if (description != null && !description.isBlank()) {
                                event.setDescription(description);
                                eventModified = true;
                            }
                        }

                        if (eventModified) {
                            eventService.store(event);
                        }
                    }
                }
            }
        }
    }

    private SpeakerRecord getSpeaker(final @NotNull SpeakerService speakerService,
                                     final @NotNull ResultSet result) throws SQLException {
        final var speakerId = result.getLong("id");

        final var speakerById = speakerService.get(speakerId);
        if (speakerById.isPresent()) {
            return speakerById.get();
        }

        final var firstName = result.getString("vname");
        final var lastName = result.getString("nname");
        final var company = result.getString("firma");
        if (!firstName.isBlank() && !lastName.isBlank() && !company.isBlank()) {
            final var speakerByName = speakerService.getSpeaker(firstName, lastName, company);
            if (speakerByName.isPresent()) {
                return speakerByName.get();
            }
        }

        final var email = result.getString("e_mail");
        if (!email.isBlank()) {
            final var speakerByEmail = speakerService.getSpeaker(email);
            if (speakerByEmail.isPresent()) {
                return speakerByEmail.get();
            }
        }

        return speakerService.newSpeaker();
    }

    private String getTwitter(@Nullable final String twitter) {
        if (twitter == null || twitter.isBlank()) {
            return "";
        } else if (twitter.startsWith("https://")) {
            return twitter;
        } else {
            return "https://twitter.com/" + twitter;
        }
    }

    private String getPhoto(@Nullable final String image) {
        if (image == null || image.isBlank()) {
            return "";
        } else if (image.startsWith("https://")) {
            return image;
        } else {
            return "https://jug.ch/images/speaker/" + image;
        }
    }

    private void importEvents(@NotNull final EventService eventService,
                              @NotNull final Connection connection)
            throws SQLException {
        if (eventService.count() > 0) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, ort, datum, startzeit, zeitende, titel, untertitel, agenda, abstract, sichtbar FROM events_neu WHERE sichtbar='ja' OR datum >= '2021-01-01' ORDER BY id");
            while (result.next()) {
                final var event = eventService.get(result.getLong("id"))
                        .orElse(eventService.newEvent());
                if (event.get(EVENT.ID) == null) {
                    event.set(EVENT.ID, result.getLong("id"));
                    event.set(EVENT.LOCATION, getEmptyForNull(result.getString("ort")));
                    event.set(EVENT.DATE, getDateTime(result.getString("datum"), result.getString("startzeit")));
                    event.set(EVENT.DURATION, getDuration(result.getString("startzeit"), result.getString("zeitende")));
                    event.set(EVENT.TITLE, getEmptyForNull(result.getString("titel")));
                    event.set(EVENT.SUBTITLE, getEmptyForNull(result.getString("untertitel")));
                    event.set(EVENT.AGENDA, getEmptyForNull(result.getString("agenda")));
                    event.set(EVENT.DESCRIPTION, getEmptyForNull(result.getString("abstract")));
                    event.set(EVENT.VISIBLE, result.getString("sichtbar").equalsIgnoreCase("ja"));
                    eventService.store(event);
                }
            }
        }
    }

    private String getEmptyForNull(@Nullable final String text) {
        return text != null ? text : "";
    }

    private void importMembers(@NotNull final MemberService memberService,
                               @NotNull final Connection connection)
            throws SQLException {
        if (memberService.count() > 1) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, vname, nname, e_mail, strasse, plz, wohnort, land, datum FROM teilnehmer ORDER BY id");
            while (result.next()) {
                final var member = memberService.get(result.getLong("id"))
                        .orElse(memberService.newMember());
                if (member.get(MEMBER.ID) == null) {
                    final var email = result.getString("e_mail");
                    if (email == null || email.isBlank()) {
                        continue;
                    }
                    member.set(MEMBER.ID, result.getLong("id"));
                    member.set(MEMBER.FIRST_NAME, result.getString("vname"));
                    member.set(MEMBER.LAST_NAME, result.getString("nname"));
                    member.set(MEMBER.EMAIL, email);
                    member.set(MEMBER.ADDRESS, result.getString("strasse"));
                    member.set(MEMBER.ZIP_CODE, result.getString("plz"));
                    member.set(MEMBER.CITY, result.getString("wohnort"));
                    member.set(MEMBER.COUNTRY, result.getString("land"));
                    member.set(MEMBER.MEMBER_SINCE, getDateTime(result.getString("datum")));
                    member.set(MEMBER.ACTIVE, true);
                    memberService.store(member);
                }
            }
        }
    }

    private LocalTime getDuration(@NotNull final String startzeit, @NotNull final String zeitende) {
        final var start = LocalTime.parse(startzeit, DateTimeFormatter.ofPattern("HH:mm:ss"));
        final var end = LocalTime.parse(zeitende, DateTimeFormatter.ofPattern("HH:mm:ss"));

        if (start == null || end == null) {
            return null;
        }

        final var duration = Duration.between(start, end);
        return LocalTime.of(0,0).plus(duration);
    }

    private LocalDate getDate(@NotNull final String datum) {
        try {
            return LocalDate.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (final DateTimeParseException e) {
            throw new RuntimeException(String.format("Can't parse date (%s)", datum), e);
        }
    }

    private LocalDateTime getDateTime(@Nullable final String datum, @Nullable final String startzeit) {
        if (datum == null || startzeit == null) {
            return null;
        }
        try {
            return LocalDateTime.of(
                    LocalDate.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalTime.parse(startzeit, DateTimeFormatter.ofPattern("HH:mm:ss.S"))
            );
        } catch (final DateTimeParseException e1) {
            try {
                return LocalDateTime.of(
                        LocalDate.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        LocalTime.parse(startzeit, DateTimeFormatter.ofPattern("HH:mm:ss"))
                );
            } catch (final DateTimeParseException e2) {
                throw new RuntimeException(String.format("Can't parse date (%s) and time(%s)", datum, startzeit), e2);
            }
        }
    }

    private LocalDateTime getDateTime(@NotNull final String datum) {
        try {
            return LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        } catch (final DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (final DateTimeParseException e2) {
                throw new RuntimeException(String.format("Can't parse date (%s)", datum), e2);
            }
        }
    }

    private void importSponsors(@NotNull final SponsorService sponsorService,
                                @NotNull final Connection connection)
            throws SQLException {
        if (sponsorService.count() > 0) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, firma, sponsortyp, url, logo FROM sponsoren WHERE aktiv='ja' ORDER BY id");
            while (result.next()) {
                final var sponsor = sponsorService.get(result.getLong("id"))
                        .orElse(sponsorService.newSponsor());
                if (sponsor.get(SPONSOR.ID) == null) {
                    sponsor.set(SPONSOR.ID, result.getLong("id"));
                    sponsor.set(SPONSOR.NAME, result.getString("firma"));
                    sponsor.set(SPONSOR.LEVEL, getSponsorLevel(result.getString("sponsortyp")));
                    sponsor.set(SPONSOR.WEBSITE, result.getString("url"));
                    sponsor.set(SPONSOR.LOGO, "https://jug.ch/images/sponsors/" + result.getString("logo"));
                    sponsorService.store(sponsor);
                }
            }
        }
    }

    private SponsorLevel getSponsorLevel(@NotNull final String sponsortyp) {
        return SponsorLevel.valueOf(
                sponsortyp.equalsIgnoreCase("Platin")
                        ? "Platinum"
                        : WordUtils.capitalizeFully(sponsortyp));
    }

}
