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

import com.vaadin.flow.router.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.entity.EventGridItem;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.Year;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.jooq.impl.DSL.when;
import static org.komunumo.data.db.tables.Event.EVENT;

@Service
public class EventService {

    private final DSLContext dsl;
    private final EventSpeakerService eventSpeakerService;

    public EventService(@NotNull final DSLContext dsl,
                        @NotNull final EventSpeakerService eventSpeakerService) {
        this.dsl = dsl;
        this.eventSpeakerService = eventSpeakerService;
    }

    public EventRecord newRecord() {
        return dsl.newRecord(EVENT);
    }

    public Optional<EventRecord> get(@NotNull final Long id) {
        return Optional.ofNullable(dsl.selectFrom(EVENT).where(EVENT.ID.eq(id)).fetchOne());
    }

    public void store(@NotNull final EventRecord event) {
        event.store();
    }

    public int countByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl.fetchCount(EVENT, EVENT.DATE.between(firstDay, lastDay));
    }

    public Stream<EventGridItem> eventsForGrid(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter + "%";
        return dsl.selectFrom(EVENT)
                .where(filterValue == null ? DSL.noCondition()
                        : EVENT.TITLE.like(filterValue)
                        .or(EVENT.SUBTITLE.like(filterValue))
                        .or(EVENT.SPEAKER.like(filterValue)))
                .orderBy(when(EVENT.DATE.isNull(), 0).otherwise(1), EVENT.DATE.desc())
                .offset(offset)
                .limit(limit)
                .stream()
                .map(EventGridItem::new);
    }

    public void deleteEvent(@NotNull final Long eventId) {
        final var event = get(eventId).orElseThrow(NotFoundException::new); // TODO use event object as parameter
        eventSpeakerService.removeAllSpeakersFromEvent(event);
        dsl.delete(EVENT).where(EVENT.ID.eq(event.getId())).execute();
    }

}
