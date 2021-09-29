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
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Keyword;
import org.komunumo.data.entity.Speaker;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.groupConcat;
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

    public EventService(@NotNull final DSLContext dsl,
                        @NotNull final EventSpeakerService eventSpeakerService) {
        this.dsl = dsl;
        this.eventSpeakerService = eventSpeakerService;
    }

    public EventRecord newEvent() {
        final var event = dsl.newRecord(EVENT);
        event.setTitle("");
        event.setSubtitle("");
        event.setDescription("");
        event.setAgenda("");
        event.setVisible(false);
        return event;
    }

    public Optional<EventRecord> get(@NotNull final Long id) {
        return dsl.fetchOptional(EVENT, EVENT.ID.eq(id));
    }

    public void store(@NotNull final EventRecord event) {
        event.store();
    }

    public int count() {
        return dsl.fetchCount(EVENT);
    }

    public int countByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl.fetchCount(EVENT, EVENT.DATE.between(firstDay, lastDay));
    }

    public Stream<Record> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        final var speakerFullName = concat(SPEAKER.FIRST_NAME, DSL.value(" "), SPEAKER.LAST_NAME);
        return dsl.select(EVENT.asterisk(),
                    groupConcat(speakerFullName).separator(", ").as("speaker"),
                    DSL.selectCount().from(EVENT_MEMBER).where(EVENT.ID.eq(EVENT_MEMBER.EVENT_ID)).asField("attendees"))
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
                .stream();
    }

    public void deleteEvent(@NotNull final EventRecord event) {
        eventSpeakerService.removeAllSpeakersFromEvent(event);
        dsl.delete(EVENT).where(EVENT.ID.eq(event.getId())).execute();
    }

    public Stream<Event> upcomingEvents() {
        return dsl.selectFrom(EVENT)
                .where(condition(EVENT.VISIBLE)
                        // minusHours(1) - show events as upcoming which had just started
                        .and(EVENT.DATE.greaterOrEqual(LocalDateTime.now().minusHours(1))))
                .orderBy(EVENT.DATE.asc())
                .fetchInto(Event.class)
                .stream()
                .map(this::addSpeakers)
                .map(this::addKeywords);
    }

    private Event addSpeakers(@NotNull final Event event) {
        final var speakers = dsl.select(SPEAKER.asterisk())
                .from(SPEAKER)
                .join(EVENT_SPEAKER).on(SPEAKER.ID.eq(EVENT_SPEAKER.SPEAKER_ID))
                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .fetchInto(Speaker.class);
        event.setSpeakers(speakers);
        return event;
    }

    private Event addKeywords(@NotNull final Event event) {
        final var keywords = dsl.select(KEYWORD.asterisk())
                .from(KEYWORD)
                .join(EVENT_KEYWORD).on(KEYWORD.ID.eq(EVENT_KEYWORD.KEYWORD_ID))
                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                .orderBy(KEYWORD.KEYWORD_)
                .fetchInto(Keyword.class);
        event.setKeywords(keywords);
        return event;
    }

    public Set<String> getAllLocations() {
        return dsl.selectDistinct(EVENT.LOCATION)
                .from(EVENT)
                .orderBy(EVENT.LOCATION)
                .fetchSet(EVENT.LOCATION);
    }
}
