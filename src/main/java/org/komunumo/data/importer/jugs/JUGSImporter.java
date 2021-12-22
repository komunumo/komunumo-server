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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.impl.DSL;
import org.komunumo.ApplicationServiceInitListener;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.EventType;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.EventSpeakerEntity;
import org.komunumo.data.service.EventKeywordService;
import org.komunumo.data.service.EventOrganizerService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.KeywordService;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.NewsService;
import org.komunumo.data.service.RedirectService;
import org.komunumo.data.service.RegistrationService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.data.service.SponsorService;
import org.komunumo.util.URLUtil;

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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.EventOrganizer.EVENT_ORGANIZER;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;
import static org.komunumo.data.db.tables.Member.MEMBER;
import static org.komunumo.data.db.tables.News.NEWS;
import static org.komunumo.data.db.tables.Registration.REGISTRATION;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;
import static org.komunumo.data.db.tables.Sponsor.SPONSOR;

@SuppressWarnings({"SqlResolve", "removal", "java:S112", "java:S1192", "java:S3776", "deprecation"})
public class JUGSImporter {

    private final DSLContext dsl;
    private final SponsorService sponsorService;
    private final MemberService memberService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;
    private final EventOrganizerService eventOrganizerService;
    private final KeywordService keywordService;
    private final EventKeywordService eventKeywordService;
    private final NewsService newsService;
    private final RedirectService redirectService;
    private final ApplicationServiceInitListener applicationServiceInitListener;

    private int hansMaerkiId;
    private int rogerSuessId;
    private int sandroRuchId;

    private int memberMergeCount = 0;
    private int speakerMergeCount = 0;

    private UI ui = null;

    public JUGSImporter(
            @NotNull final DSLContext dsl,
            @NotNull final SponsorService sponsorService,
            @NotNull final MemberService memberService,
            @NotNull final EventService eventService,
            @NotNull final RegistrationService registrationService,
            @NotNull final SpeakerService speakerService,
            @NotNull final EventSpeakerService eventSpeakerService,
            @NotNull final EventOrganizerService eventOrganizerService,
            @NotNull final KeywordService keywordService,
            @NotNull final EventKeywordService eventKeywordService,
            @NotNull final NewsService newsService,
            @NotNull final RedirectService redirectService,
            @NotNull final ApplicationServiceInitListener applicationServiceInitListener) {
        this.dsl = dsl;
        this.sponsorService = sponsorService;
        this.memberService = memberService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;
        this.eventOrganizerService = eventOrganizerService;
        this.keywordService = keywordService;
        this.eventKeywordService = eventKeywordService;
        this.newsService = newsService;
        this.redirectService = redirectService;
        this.applicationServiceInitListener = applicationServiceInitListener;
    }

    private void showNotification(@NotNull final String message) {
        ui.access(() -> Notification.show(message));
    }

    public void importFromJavaUserGroupSwitzerland(
            @NotNull final String dbURL,
            @NotNull final String dbUser,
            @NotNull final String dbPass) {
        ui = UI.getCurrent();
        new Thread(() -> {
            try {
                showNotification("Importing data from Java User Group Switzerland in the background...");
                final var connection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                connection.setReadOnly(true);
                importSponsors(connection);
                importMembers(connection);
                addMissingMembers();
                importEvents(connection);
                importKeywords(connection);
                importSpeakers(connection);
                importRegistrations(connection);
                importNews(connection);
                updateEventLevel();
                mergeMembers();
                mergeSpeakers();
                showNotification("Importing data from Java User Group Switzerland successfully finished.");
            } catch (final SQLException e) {
                showNotification("Error importing data from Java User Group Switzerland: " + e.getMessage());
            }
        }).start();
    }

    private void importNews(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);

        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, timestamp, start, stop, titel, untertitel, teaser, beschreibung FROM news ORDER BY id ASC");
            while (result.next()) {
                final var news = newsService.getNewsRecord(result.getLong("id"))
                        .orElse(newsService.newNews());
                if (news.getId() == null && !result.getString("titel").isBlank()) {
                    news.set(NEWS.ID, result.getLong("id"));
                    news.set(NEWS.CREATED, getDateTime(result.getString("timestamp")));
                    news.set(NEWS.TITLE, result.getString("titel"));
                    news.set(NEWS.SUBTITLE, result.getString("untertitel"));
                    news.set(NEWS.TEASER, result.getString("teaser"));
                    news.set(NEWS.MESSAGE, result.getString("beschreibung"));
                    news.set(NEWS.SHOW_FROM, getDateTime(result.getString("start")));
                    news.set(NEWS.SHOW_TO, getDateTime(result.getString("stop")));
                    news.store();
                    counter.incrementAndGet();
                }
            }
        }
        showNotification(counter.get() + " news imported.");
    }

    private void importKeywords(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, bezeichnung FROM eventlabels");
            while (result.next()) {
                final var keyword = keywordService.getKeywordRecord(result.getLong("id"))
                        .orElse(keywordService.newKeyword());
                if (keyword.getId() == null) {
                    keyword.set(KEYWORD.ID, result.getLong("id"));
                    keyword.set(KEYWORD.KEYWORD_, result.getString("bezeichnung"));
                    keywordService.store(keyword);
                    counter.incrementAndGet();
                }
            }
        }
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT DISTINCT events_id, eventlabels_id FROM eventsXeventlabels");
            while (result.next()) {
                final var eventId = result.getLong("events_id");
                final var keywordId = result.getLong("eventlabels_id");
                final var event = eventService.get(eventId);
                final var keyword = keywordService.getKeywordRecord(keywordId);
                if (event.isPresent() && keyword.isPresent()
                        && eventKeywordService.getKeywordsForEvent(event.orElseThrow())
                            .filter(keywordEntity -> keywordEntity.id() == keywordId).findAny().isEmpty()) {
                    final var eventKeyword = eventKeywordService.newEventKeyword();
                    eventKeyword.set(EVENT_KEYWORD.EVENT_ID, eventId);
                    eventKeyword.set(EVENT_KEYWORD.KEYWORD_ID, keywordId);
                    eventKeywordService.store(eventKeyword);
                }
            }
        }
        showNotification(counter.get() + " new keywords imported.");
    }

    private void addMissingMembers() {
        final var hansMaerki = memberService.getByName("Hans", "Märki")
                .orElse(memberService.newMember());
        hansMaerki.setFirstName("Hans");
        hansMaerki.setLastName("Märki");
        memberService.store(hansMaerki);
        hansMaerkiId = hansMaerki.getId().intValue();

        final var rogerSuess = memberService.getByName("Roger", "Süess")
                .orElse(memberService.newMember());
        rogerSuess.setFirstName("Roger");
        rogerSuess.setLastName("Süess");
        memberService.store(rogerSuess);
        rogerSuessId = rogerSuess.getId().intValue();

        final var sandroRuch = memberService.getByName("Sandro", "Ruch")
                .orElse(memberService.newMember());
        sandroRuch.setFirstName("Sandro");
        sandroRuch.setLastName("Ruch");
        memberService.store(sandroRuch);
        sandroRuchId = sandroRuch.getId().intValue();
    }

    private void importRegistrations(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT events_id, personen_id, aenderung, anmdatum, noshow, hashtag FROM eventteiln");
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
                final var deregisterCode = result.getString("hashtag");
                try {
                    if (registrationService.registerForEvent(eventId, memberId, registerDate, noShow, deregisterCode)) {
                        counter.incrementAndGet();
                    }
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
                            if (registrationService.registerForEvent(eventId, memberId, registerDate, noShow, deregisterCode)) {
                                counter.incrementAndGet();
                            }
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
        showNotification(counter.get() + " new registrations imported.");
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

    private void updateEventLevel() {
        eventService.find(0, Integer.MAX_VALUE, null)
                .filter(Event::getPublished)
                .filter(event -> event.getLevel() == null)
                .map(event -> eventService.get(event.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(event -> {
                    event.set(EVENT.LEVEL, EventLevel.All);
                    eventService.store(event);
                });
        showNotification("Updating event levels done.");
    }

    private void importSpeakers(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, vname, nname, firma, bio, image, e_mail, twitter, firmenurl, events_id, lang_talk, abstract, level FROM eventspeaker ORDER BY id DESC");
            while (result.next()) {
                final var speakerRecord = getSpeaker(speakerService, result);
                if (speakerRecord.get(SPEAKER.ID) == null
                        && (!getEmptyForNull(result.getString("vname")).isBlank()
                        || !getEmptyForNull(result.getString("nname")).isBlank())) {
                    speakerRecord.set(SPEAKER.ID, result.getLong("id"));
                    speakerRecord.set(SPEAKER.FIRST_NAME, getEmptyForNull(result.getString("vname")));
                    speakerRecord.set(SPEAKER.LAST_NAME, getEmptyForNull(result.getString("nname")));
                    speakerRecord.set(SPEAKER.COMPANY, getEmptyForNull(result.getString("firma")));
                    speakerRecord.set(SPEAKER.BIO, getEmptyForNull(result.getString("bio")));
                    speakerRecord.set(SPEAKER.PHOTO, getPhoto(result.getString("image")));
                    speakerRecord.set(SPEAKER.EMAIL, getEmptyForNull(result.getString("e_mail")));
                    speakerRecord.set(SPEAKER.TWITTER, getTwitter(result.getString("twitter")));
                    speakerRecord.set(SPEAKER.WEBSITE, getEmptyForNull(result.getString("firmenurl")));
                    counter.incrementAndGet();
                    speakerService.store(speakerRecord);
                }
                final var eventId = result.getLong("events_id");
                if (eventId > 0) {
                    final var event = eventService.get(eventId).orElse(null);
                    if (event != null) {
                        if (speakerRecord.get(SPEAKER.ID) != null) {
                            final var speakers = eventSpeakerService.getSpeakersForEvent(event).collect(Collectors.toSet());
                            final var eventSpeakerEntity = new EventSpeakerEntity(
                                    speakerRecord.getId(), speakerRecord.getFirstName(), speakerRecord.getLastName(),
                                    speakerRecord.getCompany(), speakerRecord.getPhoto(), speakerRecord.getBio());
                            if (!speakers.contains(eventSpeakerEntity)) {
                                speakers.add(eventSpeakerEntity);
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
                            if (levelTalk >= 1 && levelTalk <= 3 || event.get(EVENT.PUBLISHED)) {
                                final var level = switch (levelTalk) {
                                    case 1 -> EventLevel.Beginner;
                                    case 2 -> EventLevel.Intermediate;
                                    case 3 -> EventLevel.Advanced;
                                    default -> EventLevel.All;
                                };
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
        showNotification(counter.get() + " new speakers imported.");
    }

    private SpeakerRecord getSpeaker(final @NotNull SpeakerService speakerService,
                                     final @NotNull ResultSet result) throws SQLException {
        final var speakerId = result.getLong("id");

        final var speakerById = speakerService.getSpeakerRecord(speakerId);
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

    private void importEvents(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, ort, datum, startzeit, zeitende, titel, untertitel, agenda, abstract, sichtbar, verantwortung, urldatei, url_webinar, anm_formular FROM events_neu WHERE sichtbar='ja' OR datum >= '2021-01-01' ORDER BY id");
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
                    event.set(EVENT.EVENT_URL, URLUtil.createReadableUrl(getEmptyForNull(result.getString("titel"))));
                    event.set(EVENT.PUBLISHED, result.getString("sichtbar").equalsIgnoreCase("ja"));

                    final var eventTitle = getEmptyForNull(result.getString("titel")).toLowerCase(Locale.getDefault());
                    if (eventTitle.contains("workshoptage")
                            || eventTitle.contains("/ch/open")
                            || eventTitle.contains("ch open")
                            || eventTitle.contains("baselone")
                            || (getEmptyForNull(result.getString("anm_formular")).isEmpty()
                                    && !getEmptyForNull(result.getString("ort")).equalsIgnoreCase("Online"))) {
                        event.setType(EventType.Sponsored);
                    } else if (eventTitle.contains("bier")
                            || eventTitle.contains("networking")
                            || eventTitle.contains("the beer event")) {
                        event.setType(EventType.Meetup);
                    } else if (eventTitle.contains("workshop")) {
                        event.setType(EventType.Workshop);
                    } else {
                        event.setType(EventType.Talk);
                    }

                    eventService.store(event);
                    addOrganizers(event, result.getString("verantwortung"));
                    counter.incrementAndGet();

                    if (result.getString("urldatei") != null && !result.getString("urldatei").isBlank()) {
                        redirectService.addRedirect(result.getString("urldatei"), event.getCompleteEventUrl());
                    }
                }
            }
        }
        applicationServiceInitListener.reloadRedirects();
        showNotification(counter.get() + " new events imported.");
    }

    private void addOrganizers(@NotNull final Event event,
                               @Nullable final String verantwortung) {
        if (verantwortung != null && !verantwortung.isBlank()) {
            final List<Integer> organizerIds = new ArrayList<>();
            switch (verantwortung) {
                case "Alain" -> organizerIds.add(4790);
                case "Alex" -> organizerIds.add(6845);
                case "Andreas" -> organizerIds.add(1227);
                case "Arif" -> organizerIds.add(21);
                case "Arif, Arthy" -> {
                    organizerIds.add(21);
                    organizerIds.add(100);
                }
                case "Arthy" -> organizerIds.add(100);
                case "Bruno Schaeffer" -> organizerIds.add(1864);
                case "Christian" -> organizerIds.add(2922);
                case "Christoph" -> organizerIds.add(1108);
                case "Corsin" -> organizerIds.add(828);
                case "Dani", "Daniel" -> organizerIds.add(1486);
                case "Dom", "Dominik", "Dominik Berger" -> organizerIds.add(15809);
                case "Edwin" -> organizerIds.add(1116);
                case "Edwin / Martin" -> {
                    organizerIds.add(1116);
                    organizerIds.add(1820);
                }
                case "Erich" -> organizerIds.add(882);
                case "Florin / Jochen" -> {
                    organizerIds.add(3241);
                    organizerIds.add(5600);
                }
                case "Hans Märki, /ch/open" -> organizerIds.add(hansMaerkiId);
                case "Jochen" -> organizerIds.add(5600);
                case "Lukas" -> organizerIds.add(5187);
                case "Marc" -> organizerIds.add(3790);
                case "Marcus" -> organizerIds.add(5889);
                case "Marcus/Peti" -> {
                    organizerIds.add(5889);
                    organizerIds.add(5244);
                }
                case "Markus", "Markus Pilz" -> organizerIds.add(1518);
                case "Martin Jäger" -> organizerIds.add(67);
                case "Martin Jäger, Edwin" -> {
                    organizerIds.add(67);
                    organizerIds.add(1116);
                }
                case "Martin", "Martin Kernland" -> organizerIds.add(1820);
                case "Matthias" -> organizerIds.add(12206);
                case "Matthias Zimmermann" -> organizerIds.add(12206);
                case "Micha" -> organizerIds.add(11465);
                case "Micha / Bruno Schaeffer" -> {
                    organizerIds.add(11465);
                    organizerIds.add(1864);
                }
                case "Milan" -> organizerIds.add(11457);
                case "Oliver" -> organizerIds.add(14587);
                case "Patrick" -> organizerIds.add(7135);
                case "Patrick/Corsin" -> {
                    organizerIds.add(7135);
                    organizerIds.add(828);
                }
                case "Peter" -> organizerIds.add(11480);
                case "Peti" -> organizerIds.add(5244);
                case "Philipp", "Philipp Oser" -> organizerIds.add(192);
                case "Philipp (Qbi)" -> {
                    organizerIds.add(192);
                    organizerIds.add(1099);
                }
                case "Qbi" -> organizerIds.add(1099);
                case "Roger" -> organizerIds.add(rogerSuessId);
                case "Roger / Arthy" -> {
                    organizerIds.add(rogerSuessId);
                    organizerIds.add(100);
                }
                case "Ruedi" -> organizerIds.add(4396);
                case "Sandro Ruch" -> organizerIds.add(sandroRuchId);
                case "Serano" -> organizerIds.add(4628);
                case "Silvano" -> organizerIds.add(575);
                case "Silvano, Peter" -> {
                    organizerIds.add(575);
                    organizerIds.add(11480);
                }
                case "Simon" -> organizerIds.add(15228);
                case "Thomas", "Thomas Wenger" -> organizerIds.add(4423);
            }
            final var organizers = organizerIds.stream()
                    .map(Integer::longValue)
                    .map(memberService::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            if (!organizers.isEmpty()) {
                eventOrganizerService.setEventOrganizers(event, organizers);
            }
        }
    }

    private String getEmptyForNull(@Nullable final String text) {
        return text != null ? text : "";
    }

    private void importMembers(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);
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
                    counter.incrementAndGet();
                }
            }
        }
        showNotification(counter.get() + " new members imported.");
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
        if (datum.startsWith("0000")) {
            return null;
        }
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

    private void importSponsors(@NotNull final Connection connection)
            throws SQLException {
        final var counter = new AtomicInteger(0);
        try (var statement = connection.createStatement()) {
            final var result = statement.executeQuery(
                    "SELECT id, firma, sponsortyp, url, logo FROM sponsoren WHERE aktiv='ja' ORDER BY id");
            while (result.next()) {
                final var sponsorRecord = sponsorService.getSponsorRecord(result.getLong("id"))
                        .orElse(sponsorService.newSponsor());
                if (sponsorRecord.get(SPONSOR.ID) == null) {
                    sponsorRecord.set(SPONSOR.ID, result.getLong("id"));
                    sponsorRecord.set(SPONSOR.NAME, result.getString("firma"));
                    sponsorRecord.set(SPONSOR.LEVEL, getSponsorLevel(result.getString("sponsortyp")));
                    sponsorRecord.set(SPONSOR.WEBSITE, result.getString("url").replaceFirst("^http://", "https://"));
                    sponsorRecord.set(SPONSOR.LOGO, "https://jug.ch/images/sponsors/" + result.getString("logo"));
                    sponsorService.store(sponsorRecord);
                    if (!sponsorRecord.getWebsite().isBlank()) {
                        final var domain = URLUtil.getDomainFromUrl(sponsorRecord.getWebsite());
                        final var sponsorDomainRecord = sponsorService.newSponsorDomain();
                        sponsorDomainRecord.setSponsorId(sponsorRecord.getId());
                        sponsorDomainRecord.setDomain(domain);
                        sponsorDomainRecord.store();
                    }
                    counter.incrementAndGet();
                }
            }
        }
        showNotification(counter.get() + " new sponsors imported.");
    }

    private SponsorLevel getSponsorLevel(@NotNull final String sponsortyp) {
        return SponsorLevel.valueOf(
                sponsortyp.equalsIgnoreCase("Platin")
                        ? "Platinum"
                        : WordUtils.capitalizeFully(sponsortyp));
    }

    private void mergeMembers() {
        memberMergeCount = 0;
        dsl.select(MEMBER.EMAIL, DSL.count(MEMBER.EMAIL).as("email_count"))
                .from(MEMBER)
                .groupBy(MEMBER.EMAIL)
                .having(DSL.count(MEMBER.EMAIL).greaterThan(1))
                .stream().forEach(this::mergeMembers);
        showNotification(memberMergeCount + " duplicate members merged");
    }

    private void mergeMembers(@NotNull final Record2<String, Integer> record) {
        mergeMembers(record.get(MEMBER.EMAIL));
        memberMergeCount += record.get("email_count", Integer.class);
    }

    private void mergeMembers(@NotNull final String email) {
        final var recordList = dsl.selectFrom(MEMBER)
                .where(MEMBER.EMAIL.eq(email))
                .orderBy(MEMBER.ID.asc())
                .stream().collect(Collectors.toList());
        while (recordList.size() > 1) {
            mergeMembers(recordList.get(0), recordList.get(1));
            recordList.remove(1);
        }
    }

    private void mergeMembers(@NotNull final MemberRecord member1, @NotNull final MemberRecord member2) {
        dsl.update(EVENT_ORGANIZER)
                .set(EVENT_ORGANIZER.MEMBER_ID, member1.getId())
                .where(EVENT_ORGANIZER.MEMBER_ID.eq(member2.getId()))
                .execute();

        dsl.selectFrom(REGISTRATION)
                .where(REGISTRATION.MEMBER_ID.eq(member2.getId()))
                .forEach(record ->
                        dsl.insertInto(REGISTRATION, REGISTRATION.EVENT_ID, REGISTRATION.MEMBER_ID, REGISTRATION.DATE, REGISTRATION.NO_SHOW)
                                .values(record.getEventId(), member1.getId(), record.getDate(), record.getNoShow())
                                .onDuplicateKeyIgnore()
                                .execute());
        dsl.deleteFrom(REGISTRATION)
                .where(REGISTRATION.MEMBER_ID.eq(member2.getId()))
                .execute();

        Stream.of(MEMBER.FIRST_NAME, MEMBER.LAST_NAME, MEMBER.COMPANY, MEMBER.EMAIL, MEMBER.ADDRESS, MEMBER.ZIP_CODE,
                MEMBER.CITY, MEMBER.STATE, MEMBER.COUNTRY).forEach(field -> {
            if (!member2.get(field).isBlank()) {
                member1.set(field, member2.get(field));
            }
        });

        if (member1.getRegistrationDate() == null && member2.getRegistrationDate() != null
                || member1.getRegistrationDate() != null && member2.getRegistrationDate() != null
                && member1.getRegistrationDate().isAfter(member2.getRegistrationDate())) {
            member1.setRegistrationDate(member2.getRegistrationDate());
        }

        if (member1.getMembershipBegin() == null && member2.getMembershipBegin() != null
                || member1.getMembershipBegin() != null && member2.getMembershipBegin() != null
                && member1.getMembershipBegin().isAfter(member2.getMembershipBegin())) {
            member1.setMembershipBegin(member2.getMembershipBegin());
        }

        if (member1.getMembershipEnd() == null && member2.getMembershipEnd() != null
                || member1.getMembershipEnd() != null && member2.getMembershipEnd() != null
                && member1.getMembershipEnd().isBefore(member2.getMembershipEnd())) {
            member1.setMembershipEnd(member2.getMembershipEnd());
        }

        if (member1.getMembershipId() == 0 && member2.getMembershipId() > 0) {
            member1.setMembershipId(member2.getMembershipId());
        }

        if (!member1.getAdmin() && member2.getAdmin()) {
            member1.setAdmin(true);
        }

        member1.store();
        member2.delete();
    }

    private void mergeSpeakers() {
        speakerMergeCount = 0;
        dsl.select(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME, DSL.count(DSL.asterisk()).as("name_count"))
                .from(SPEAKER)
                .groupBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .having(DSL.count(DSL.asterisk()).greaterThan(1))
                .stream().forEach(this::mergeSpeakers);
        showNotification(speakerMergeCount + " duplicate speakers merged");
    }

    private void mergeSpeakers(@NotNull final Record3<String, String, Integer> record) {
        mergeSpeakers(record.get(SPEAKER.FIRST_NAME), record.get(SPEAKER.LAST_NAME));
        speakerMergeCount += record.get("name_count", Integer.class);
    }

    private void mergeSpeakers(@NotNull final String firstName, @NotNull final String lastName) {
        final var recordList = dsl.selectFrom(SPEAKER)
                .where(SPEAKER.FIRST_NAME.eq(firstName).and(SPEAKER.LAST_NAME.eq(lastName)))
                .orderBy(SPEAKER.ID.desc())
                .stream().collect(Collectors.toList());
        while (recordList.size() > 1) {
            final var speaker1 = recordList.get(0);
            final var speaker2 = recordList.get(1);
            if (speaker1.getId() > speaker2.getId()) {
                mergeSpeakers(speaker1, speaker2);
                recordList.remove(1);
            } else {
                mergeSpeakers(speaker2, speaker1);
                recordList.remove(0);
            }
        }
    }

    private void mergeSpeakers(@NotNull final SpeakerRecord speaker1, @NotNull final SpeakerRecord speaker2) {
        dsl.selectFrom(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.SPEAKER_ID.eq(speaker2.getId()))
                .forEach(record ->
                        dsl.insertInto(EVENT_SPEAKER, EVENT_SPEAKER.EVENT_ID, EVENT_SPEAKER.SPEAKER_ID)
                                .values(record.getEventId(), speaker1.getId())
                                .onDuplicateKeyIgnore()
                                .execute());
        dsl.deleteFrom(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.SPEAKER_ID.eq(speaker2.getId()))
                .execute();

        speaker2.delete();
    }

}
