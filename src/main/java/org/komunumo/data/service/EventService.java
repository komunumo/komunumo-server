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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Optional;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.EventRecord;
import org.springframework.stereotype.Service;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.groupConcat;
import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Service
public class EventService {

    private final DSLContext dsl;

    public EventService(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public EventRecord newRecord() {
        return dsl.newRecord(EVENT);
    }

    public Optional<EventRecord> get(final Long id) {
        return Optional.ofNullable(dsl.selectFrom(EVENT).where(EVENT.ID.eq(id)).fetchOne());
    }

    public void store(final EventRecord event) {
        event.store();
    }

    public int countByYear(final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl.fetchCount(EVENT, EVENT.DATE.between(firstDay, lastDay));
    }

    public Stream<Record5<Long, String, String, LocalDateTime, Boolean>> eventsWithSpeakers(final int offset, final int limit, final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter + "%";
        return dsl.select(EVENT.ID, EVENT.TITLE, groupConcat(concat(concat(SPEAKER.FIRST_NAME, " "), SPEAKER.LAST_NAME)).separator(", ").as("speaker"), EVENT.DATE, EVENT.VISIBLE).from(EVENT)
                .leftJoin(EVENT_SPEAKER).on(EVENT.ID.eq(EVENT_SPEAKER.EVENT_ID))
                .leftJoin(SPEAKER).on(EVENT_SPEAKER.SPEAKER_ID.eq(SPEAKER.ID))
                .where(filterValue == null ? DSL.noCondition() : EVENT.TITLE.like(filterValue).or(SPEAKER.FIRST_NAME.like(filterValue).or(SPEAKER.LAST_NAME.like(filterValue))))
                .groupBy(EVENT.ID)
                .orderBy(EVENT.DATE.desc())
                .offset(offset)
                .limit(limit)
                .stream();
    }

}
