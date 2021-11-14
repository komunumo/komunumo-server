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

package org.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Keyword;
import org.komunumo.data.entity.Speaker;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.condition;
import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.EventMember.EVENT_MEMBER;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Service
public class EventService {

    private final DSLContext dsl;
    private final EventSpeakerService eventSpeakerService;
    private final EventOrganizerService eventOrganizerService;
    private final EventKeywordService eventKeywordService;

    public EventService(@NotNull final DSLContext dsl,
                        @NotNull final EventSpeakerService eventSpeakerService,
                        @NotNull final EventOrganizerService eventOrganizerService,
                        @NotNull final EventKeywordService eventKeywordService) {
        this.dsl = dsl;
        this.eventSpeakerService = eventSpeakerService;
        this.eventOrganizerService = eventOrganizerService;
        this.eventKeywordService = eventKeywordService;
    }

    public Event newEvent() {
        final var event = dsl.newRecord(EVENT)
                .into(Event.class);
        event.setTitle("");
        event.setSubtitle("");
        event.setDescription("");
        event.setAgenda("");
        event.setWebinarUrl("");
        event.setPublished(false);
        event.setSpeakers(List.of());
        event.setKeywords(List.of());
        event.setAttendeeCount(0);
        event.setEventUrl("");
        return event;
    }

    public Event copyEvent(@NotNull final Event originalEvent) {
        final var newEvent = originalEvent.copy().into(Event.class);
        newEvent.setSpeakers(originalEvent.getSpeakers());
        newEvent.setKeywords(originalEvent.getKeywords());
        newEvent.setLocation("");
        newEvent.setWebinarUrl("");
        newEvent.setDate(null);
        newEvent.setPublished(false);
        newEvent.setAttendeeCount(0);
        newEvent.setEventUrl("");
        return newEvent;
    }

    public Optional<Event> get(@NotNull final Long id) {
        return dsl.selectFrom(EVENT)
                .where(EVENT.ID.eq(id))
                .fetchOptionalInto(Event.class);
    }

    public Optional<Event> getByEventUrl(@NotNull final String location, @NotNull final Year year, @NotNull final String url) {
        return dsl.selectFrom(EVENT)
                .where(EVENT.LOCATION.eq(location)
                        .and(DSL.year(EVENT.DATE).eq(year.getValue()))
                        .and(EVENT.EVENT_URL.eq(url)))
                .fetchOptionalInto(Event.class)
                .map(this::addAdditionalData);
    }

    public Optional<Event> getByWebinarUrl(@NotNull final String webinarUrl) {
        final var event = dsl.selectFrom(EVENT)
                .where(EVENT.WEBINAR_URL.eq(webinarUrl))
                .fetchOptionalInto(Event.class);
        if (event.isEmpty() && !webinarUrl.endsWith("/")) {
            return getByWebinarUrl(webinarUrl.concat("/"));
        }
        return event;
    }

    public void store(@NotNull final Event event) {
        event.store();
    }

    public int count() {
        return dsl.fetchCount(EVENT);
    }

    public Stream<Event> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        final var speakerFullName = concat(SPEAKER.FIRST_NAME, DSL.value(" "), SPEAKER.LAST_NAME);
        return dsl.select(EVENT.asterisk())
                .from(EVENT)
                .leftJoin(EVENT_SPEAKER).on(EVENT.ID.eq(EVENT_SPEAKER.EVENT_ID))
                .leftJoin(SPEAKER).on(EVENT_SPEAKER.SPEAKER_ID.eq(SPEAKER.ID))
                .where(filterValue == null ? DSL.noCondition()
                        : EVENT.TITLE.like(filterValue)
                        .or(speakerFullName.like(filterValue)))
                .groupBy(EVENT.ID)
                .orderBy(EVENT.DATE.desc().nullsFirst())
                .offset(offset)
                .limit(limit)
                .fetchInto(Event.class)
                .stream()
                .map(this::addAdditionalData);
    }

    public void deleteEvent(@NotNull final Event event) {
        eventSpeakerService.removeAllSpeakersFromEvent(event);
        eventOrganizerService.removeAllOrganizersFromEvent(event);
        eventKeywordService.removeAllKeywordsFromEvent(event);
        dsl.delete(EVENT).where(EVENT.ID.eq(event.getId())).execute();
    }

    public Stream<Event> upcomingEvents() {
        return dsl.selectFrom(EVENT)
                .where(condition(EVENT.PUBLISHED)
                        .and(EVENT.DATE.greaterOrEqual(LocalDateTime.now().withHour(0).withMinute(0))))
                .orderBy(EVENT.DATE.asc())
                .fetchInto(Event.class)
                .stream()
                .filter(event -> {
                    final var startDate = event.getDate();
                    final var eventDuration = event.getDuration();
                    final var endDate = startDate.plusHours(eventDuration.getHour()).plusMinutes(eventDuration.getMinute());
                    final var now = LocalDateTime.now();
                    return now.isBefore(endDate);
                })
                .map(this::addAdditionalData);
    }

    private Event addAdditionalData(@NotNull final Event event) {
        addSpeakers(event);
        addKeywords(event);
        addAttendeeCount(event);
        return event;
    }

    private void addSpeakers(@NotNull final Event event) {
        final var speakers = dsl.select(SPEAKER.asterisk())
                .from(SPEAKER)
                .join(EVENT_SPEAKER).on(SPEAKER.ID.eq(EVENT_SPEAKER.SPEAKER_ID))
                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .fetchInto(Speaker.class);
        event.setSpeakers(speakers);
    }

    private void addKeywords(@NotNull final Event event) {
        final var keywords = dsl.select(KEYWORD.asterisk())
                .from(KEYWORD)
                .join(EVENT_KEYWORD).on(KEYWORD.ID.eq(EVENT_KEYWORD.KEYWORD_ID))
                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                .orderBy(KEYWORD.KEYWORD_)
                .fetchInto(Keyword.class);
        event.setKeywords(keywords);
    }

    private void addAttendeeCount(@NotNull final Event event) {
        final var attendeeCount = dsl.fetchCount(EVENT_MEMBER, EVENT_MEMBER.EVENT_ID.eq(event.getId()));
        event.setAttendeeCount(attendeeCount);
    }

    public Set<String> getAllLocations() {
        return dsl.selectDistinct(EVENT.LOCATION)
                .from(EVENT)
                .orderBy(EVENT.LOCATION)
                .fetchSet(EVENT.LOCATION);
    }
}
