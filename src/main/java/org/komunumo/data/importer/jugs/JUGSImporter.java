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
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Speaker;
import org.komunumo.data.service.EventKeywordService;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.KeywordService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;
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

    private int hansMaerkiId;
    private int rogerSuessId;
    private int sandroRuchId;

    @Bean
    public CommandLineRunner importFromJavaUserGroupSwitzerland(
            @NotNull final SponsorService sponsorService,
            @NotNull final MemberService memberService,
            @NotNull final EventService eventService,
            @NotNull final EventMemberService eventMemberService,
            @NotNull final SpeakerService speakerService,
            @NotNull final EventSpeakerService eventSpeakerService,
            @NotNull final KeywordService keywordService,
            @NotNull final EventKeywordService eventKeywordService) {
        return args -> {
            Thread.sleep(30_000); // wait 30 seconds to be sure this import does not slow down the startup
            if (dbURL != null && dbUser != null && dbPass != null) {
                final var connection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                connection.setReadOnly(true);
                importSponsors(sponsorService, connection);
                importMembers(memberService, connection);
                addMissingMembers(memberService);
                importEvents(eventService, eventMemberService, memberService, connection);
                importKeywords(eventService, keywordService, eventKeywordService, connection);
                importSpeakers(speakerService, eventSpeakerService, eventService, connection);
                importRegistrations(eventMemberService, eventService, memberService, connection);
                updateEventLevel(eventService);
            }
        };
    }

    private void importKeywords(@NotNull final EventService eventService,
                                @NotNull final KeywordService keywordService,
                                @NotNull final EventKeywordService eventKeywordService,
                                @NotNull final Connection connection)
            throws SQLException {
        if (keywordService.count() > 0) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, bezeichnung FROM eventlabels");
            while (result.next()) {
                final var keyword = keywordService.newKeyword();
                keyword.set(KEYWORD.ID, result.getLong("id"));
                keyword.set(KEYWORD.KEYWORD_, result.getString("bezeichnung"));
                keywordService.store(keyword);
            }
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT DISTINCT events_id, eventlabels_id FROM eventsXeventlabels");
            while (result.next()) {
                final var eventId = result.getLong("events_id");
                final var keywordId = result.getLong("eventlabels_id");
                final var event = eventService.get(eventId);
                final var keyword = keywordService.get(keywordId);
                if (event.isPresent() && keyword.isPresent()) {
                    final var eventKeyword = eventKeywordService.newEventKeyword();
                    eventKeyword.set(EVENT_KEYWORD.EVENT_ID, eventId);
                    eventKeyword.set(EVENT_KEYWORD.KEYWORD_ID, keywordId);
                    eventKeywordService.store(eventKeyword);
                }
            }
        }
    }

    private void addMissingMembers(@NotNull final MemberService memberService) {
        final var hansMaerki = memberService.newMember();
        hansMaerki.setFirstName("Hans");
        hansMaerki.setLastName("Märki");
        memberService.store(hansMaerki);
        hansMaerkiId = hansMaerki.getId().intValue();

        final var rogerSuess = memberService.newMember();
        rogerSuess.setFirstName("Roger");
        rogerSuess.setLastName("Süess");
        memberService.store(rogerSuess);
        rogerSuessId = rogerSuess.getId().intValue();

        final var sandroRuch = memberService.newMember();
        sandroRuch.setFirstName("Sandro");
        sandroRuch.setLastName("Ruch");
        memberService.store(sandroRuch);
        sandroRuchId = sandroRuch.getId().intValue();
    }

    private void importRegistrations(@NotNull final EventMemberService eventMemberService,
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
                final var event = eventService.get(eventId);
                if (event.isEmpty()) {
                    continue;
                }
                final var memberId = result.getLong("personen_id");
                final var registerDate = getRegisterDate(result.getString("aenderung"), result.getString("anmdatum"));
                final var noShow = "Online".equalsIgnoreCase(event.get().getLocation())
                        || result.getString("noshow") != null && result.getString("noshow").equals("1");
                try {
                    eventMemberService.registerForEvent(eventId, memberId, registerDate, noShow);
                } catch (final Exception e1) {
                    if (memberService.get(memberId, true).isEmpty()) {
                        final var member = memberService.newMember();
                        member.setId(memberId);
                        member.setFirstName(RandomStringUtils.randomAlphabetic(32));
                        member.setLastName(RandomStringUtils.randomAlphabetic(32));
                        member.setEmail(RandomStringUtils.randomAlphabetic(32));
                        member.setRegistrationDate(registerDate);
                        member.setAccountDeleted(true);
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
                .filter(Event::getVisible)
                .filter(event -> event.getLevel() == null)
                .map(event -> eventService.get(event.getId()))
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

    private Speaker getSpeaker(final @NotNull SpeakerService speakerService,
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
                              @NotNull final EventMemberService eventMemberService,
                              @NotNull final MemberService memberService,
                              @NotNull final Connection connection)
            throws SQLException {
        if (eventService.count() > 0) {
            return;
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, ort, datum, startzeit, zeitende, titel, untertitel, agenda, abstract, sichtbar, verantwortung, url_webinar FROM events_neu WHERE sichtbar='ja' OR datum >= '2021-01-01' ORDER BY id");
            while (result.next()) {
                final var event = eventService.get(result.getLong("id"))
                        .orElse(eventService.newEvent());
                if (event.get(EVENT.ID) == null) {
                    event.set(EVENT.ID, result.getLong("id"));
                    event.set(EVENT.LOCATION, getEmptyForNull(result.getString("ort")));
                    event.set(EVENT.WEBINAR_URL, getEmptyForNull(result.getString("url_webinar")));
                    event.set(EVENT.DATE, getDateTime(result.getString("datum"), result.getString("startzeit")));
                    event.set(EVENT.DURATION, getDuration(result.getString("startzeit"), result.getString("zeitende")));
                    event.set(EVENT.TITLE, getEmptyForNull(result.getString("titel")));
                    event.set(EVENT.SUBTITLE, getEmptyForNull(result.getString("untertitel")));
                    event.set(EVENT.AGENDA, getEmptyForNull(result.getString("agenda")));
                    event.set(EVENT.DESCRIPTION, getEmptyForNull(result.getString("abstract")));
                    event.set(EVENT.VISIBLE, result.getString("sichtbar").equalsIgnoreCase("ja"));
                    eventService.store(event);
                    addOrganizers(memberService, eventMemberService, event, result.getString("verantwortung"));
                }
            }
        }
    }

    private void addOrganizers(@NotNull final MemberService memberService,
                               @NotNull final EventMemberService eventMemberService,
                               @NotNull final Event event,
                               @Nullable final String verantwortung) {
        if (verantwortung != null && !verantwortung.isBlank()) {
            final List<Integer> organizerIds = new ArrayList<>();
            switch (verantwortung) {
                case "Alain": organizerIds.add(4790); break;
                case "Alex": organizerIds.add(6845); break;
                case "Andreas": organizerIds.add(1227); break;
                case "Arif": organizerIds.add(21); break;
                case "Arif, Arthy": organizerIds.add(21); organizerIds.add(100); break;
                case "Arthy": organizerIds.add(100); break;
                case "Bruno Schaeffer": organizerIds.add(1864); break;
                case "Christian": organizerIds.add(2922); break;
                case "Christoph": organizerIds.add(1108); break;
                case "Corsin": organizerIds.add(828); break;
                case "Dani":
                case "Daniel": organizerIds.add(1486); break;
                case "Dom":
                case "Dominik":
                case "Dominik Berger": organizerIds.add(15809); break;
                case "Edwin": organizerIds.add(1116); break;
                case "Edwin / Martin": organizerIds.add(1116); organizerIds.add(1820); break;
                case "Erich": organizerIds.add(882); break;
                case "Florin / Jochen": organizerIds.add(3241); organizerIds.add(5600); break;
                case "Hans Märki, /ch/open": organizerIds.add(hansMaerkiId); break;
                case "Jochen": organizerIds.add(5600); break;
                case "Lukas": organizerIds.add(5187); break;
                case "Marc": organizerIds.add(3790); break;
                case "Marcus": organizerIds.add(5889); break;
                case "Marcus/Peti": organizerIds.add(5889); organizerIds.add(5244); break;
                case "Markus":
                case "Markus Pilz": organizerIds.add(1518); break;
                case "Martin Jäger": organizerIds.add(67); break;
                case "Martin Jäger, Edwin": organizerIds.add(67); organizerIds.add(1116); break;
                case "Martin":
                case "Martin Kernland": organizerIds.add(1820); break;
                case "Matthias": organizerIds.add(12206); break;
                case "Matthias Zimmermann": organizerIds.add(12206); break;
                case "Micha": organizerIds.add(11465); break;
                case "Micha / Bruno Schaeffer": organizerIds.add(11465); organizerIds.add(1864); break;
                case "Milan": organizerIds.add(11457); break;
                case "Oliver": organizerIds.add(14587); break;
                case "Patrick": organizerIds.add(7135); break;
                case "Patrick/Corsin": organizerIds.add(7135); organizerIds.add(828); break;
                case "Peter": organizerIds.add(11480); break;
                case "Peti": organizerIds.add(5244); break;
                case "Philipp":
                case "Philipp Oser": organizerIds.add(192); break;
                case "Philipp (Qbi)": organizerIds.add(192); organizerIds.add(1099); break;
                case "Qbi": organizerIds.add(1099); break;
                case "Roger": organizerIds.add(rogerSuessId); break;
                case "Roger / Arthy": organizerIds.add(rogerSuessId); organizerIds.add(100); break;
                case "Ruedi": organizerIds.add(4396); break;
                case "Sandro Ruch": organizerIds.add(sandroRuchId); break;
                case "Serano": organizerIds.add(4628); break;
                case "Silvano": organizerIds.add(575); break;
                case "Silvano, Peter": organizerIds.add(575); organizerIds.add(11480); break;
                case "Simon": organizerIds.add(15228); break;
                case "Thomas":
                case "Thomas Wenger": organizerIds.add(4423); break;
            }
            final var organizers = organizerIds.stream()
                    .map(Integer::longValue)
                    .map(memberService::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            if (!organizers.isEmpty()) {
                eventMemberService.setEventOrganizers(event, organizers);
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
        final var adminIds = List.of(192, 882, 2922, 4423, 5091, 5244, 5889, 7135, 15809);
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, vname, nname, e_mail, strasse, plz, wohnort, land, datum, join_as FROM teilnehmer ORDER BY id");
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
                    member.set(MEMBER.REGISTRATION_DATE, getDateTime(result.getString("datum")));
                    member.set(MEMBER.ACCOUNT_ACTIVE, true);
                    member.set(MEMBER.ADMIN, adminIds.contains(result.getInt("id")));
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
