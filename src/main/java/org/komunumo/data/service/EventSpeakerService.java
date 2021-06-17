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
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.EventSpeakerRecord;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.select;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Service
public class EventSpeakerService {

    private final DSLContext dsl;

    public EventSpeakerService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<EventSpeakerRecord> get(@NotNull final Long eventId,
                                            @NotNull final Long speakerId) {
        return Optional.ofNullable(dsl.selectFrom(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.EVENT_ID.eq(eventId).and(EVENT_SPEAKER.SPEAKER_ID.eq(speakerId)))
                .fetchOne());
    }

    public Stream<SpeakerRecord> getSpeakersForEvent(@NotNull final EventRecord event) {
        return dsl
                .selectFrom(SPEAKER)
                .where(SPEAKER.ID.in(
                        select(EVENT_SPEAKER.SPEAKER_ID)
                                .from(EVENT_SPEAKER)
                                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                ))
                .fetch()
                .stream();
    }

    public void setEventSpeakers(@NotNull final EventRecord event,
                                 @NotNull final Set<SpeakerRecord> speakers) {
        synchronized (speakers) {
            getSpeakersForEvent(event).forEach(speaker -> {
                if (speakers.contains(speaker)) {
                    speakers.remove(speaker);
                } else {
                    removeSpeakerFromEvent(event, speaker);
                }
            });
            speakers.forEach(speaker -> addSpeakerToEvent(event, speaker));
        }
    }

    private void addSpeakerToEvent(@NotNull final EventRecord event,
                                   @NotNull final SpeakerRecord speaker) {
        final var eventSpeaker = dsl.newRecord(EVENT_SPEAKER);
        eventSpeaker.setEventId(event.getId());
        eventSpeaker.setSpeakerId(speaker.getId());
        eventSpeaker.store();
    }

    private void removeSpeakerFromEvent(@NotNull final EventRecord event,
                                        @NotNull final SpeakerRecord speaker) {
        dsl.delete(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                .and(EVENT_SPEAKER.SPEAKER_ID.eq(speaker.getId()))
                .execute();
    }

    public void removeAllSpeakersFromEvent(@NotNull final EventRecord event) {
        dsl.delete(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.EVENT_ID.eq(event.getId()))
                .execute();
    }
}
