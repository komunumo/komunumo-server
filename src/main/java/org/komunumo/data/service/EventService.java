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
import static org.jooq.impl.DSL.groupConcat;
import static org.jooq.impl.DSL.when;
import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.EventMember.EVENT_MEMBER;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
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
        event.setAbstract("");
        event.setAgenda("");
        event.setVisible(false);
        return event;
    }

    public Optional<EventRecord> get(@NotNull final Long id) {
        return Optional.ofNullable(dsl.selectFrom(EVENT).where(EVENT.ID.eq(id)).fetchOne());
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
        return dsl.select(EVENT.asterisk(),
                    groupConcat(concat(concat(SPEAKER.FIRST_NAME, " "), SPEAKER.LAST_NAME)).separator(", ").as("speaker"),
                    DSL.selectCount().from(EVENT_MEMBER).where(EVENT.ID.eq(EVENT_MEMBER.EVENT_ID)).asField("attendees"))
                .from(EVENT)
                .leftJoin(EVENT_SPEAKER).on(EVENT.ID.eq(EVENT_SPEAKER.EVENT_ID))
                .leftJoin(SPEAKER).on(EVENT_SPEAKER.SPEAKER_ID.eq(SPEAKER.ID))
                .where(filterValue == null ? DSL.noCondition()
                        : EVENT.TITLE.like(filterValue)
                        .or(concat(concat(SPEAKER.FIRST_NAME, " "), SPEAKER.LAST_NAME).like(filterValue)))
                .groupBy(EVENT.ID)
                .orderBy(when(EVENT.DATE.isNull(), 0).otherwise(1), EVENT.DATE.desc())
                .offset(offset)
                .limit(limit)
                .stream();
    }

    public void deleteEvent(@NotNull final EventRecord event) {
        eventSpeakerService.removeAllSpeakersFromEvent(event);
        dsl.delete(EVENT).where(EVENT.ID.eq(event.getId())).execute();
    }

    public Stream<EventRecord> upcomingEvents() {
        return dsl.selectFrom(EVENT)
                .where(EVENT.VISIBLE.eq(true)
                        // minusHours(1) - show events as upcoming which had just started
                        .and(EVENT.DATE.greaterOrEqual(LocalDateTime.now().minusHours(1))))
                .orderBy(EVENT.DATE.asc())
                .stream();
    }

    public Set<String> getAllLocations() {
        return dsl.selectDistinct(EVENT.LOCATION)
                .from(EVENT)
                .orderBy(EVENT.LOCATION)
                .fetchSet(EVENT.LOCATION);
    }
}
