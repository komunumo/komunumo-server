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

import java.time.LocalTime;
import java.time.Year;
import java.util.Optional;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventRecord;
import org.springframework.stereotype.Service;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.komunumo.data.db.tables.Event.EVENT;

@Service
public class EventService {

    private final DSLContext dsl;

    public EventService(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public EventRecord newRecord() {
        return dsl.newRecord(EVENT);
    }

    public Stream<EventRecord> list(final int offset, final int limit) {
        return dsl.selectFrom(EVENT).offset(offset).limit(limit).stream();
    }

    public void update(final EventRecord event) {
        event.update();
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
}
