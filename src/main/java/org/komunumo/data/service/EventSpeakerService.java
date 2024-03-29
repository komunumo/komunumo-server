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
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.EventSpeakerEntity;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.select;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

interface EventSpeakerService extends DSLContextGetter {

    default Stream<EventSpeakerEntity> getSpeakersForEvent(@NotNull final Event event) {
        return dsl()
                .select(SPEAKER.ID, SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME, SPEAKER.COMPANY, SPEAKER.PHOTO, SPEAKER.BIO)
                .from(SPEAKER)
                .where(SPEAKER.ID.in(
                        select(EVENT_SPEAKER.SPEAKER_ID)
                                .from(EVENT_SPEAKER)
                                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                ))
                .fetchInto(EventSpeakerEntity.class)
                .stream();
    }

    default void setEventSpeakers(@NotNull final Event event,
                                  @NotNull final Set<EventSpeakerEntity> eventSpeakerEntities) {
        final var eventSpeakers = new HashSet<EventSpeakerEntity>(eventSpeakerEntities.size());
        eventSpeakers.addAll(eventSpeakerEntities);
        getSpeakersForEvent(event).forEach(eventSpeakerEntity -> {
            if (eventSpeakers.contains(eventSpeakerEntity)) {
                eventSpeakers.remove(eventSpeakerEntity);
            } else {
                removeSpeakerFromEvent(event, eventSpeakerEntity);
            }
        });
        eventSpeakers.forEach(eventSpeakerEntity -> addSpeakerToEvent(event, eventSpeakerEntity));
    }

    private void addSpeakerToEvent(@NotNull final Event event,
                                   @NotNull final EventSpeakerEntity eventSpeakerEntity) {
        final var eventSpeaker = dsl().newRecord(EVENT_SPEAKER);
        eventSpeaker.setEventId(event.getId());
        eventSpeaker.setSpeakerId(eventSpeakerEntity.id());
        eventSpeaker.store();
    }

    private void removeSpeakerFromEvent(@NotNull final Event event,
                                        @NotNull final EventSpeakerEntity eventSpeakerEntity) {
        dsl().delete(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                .and(EVENT_SPEAKER.SPEAKER_ID.eq(eventSpeakerEntity.id()))
                .execute();
    }

    default void removeAllSpeakersFromEvent(@NotNull final Event event) {
        dsl().delete(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                .execute();
    }

}
