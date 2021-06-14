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

import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventSpeakerRecord;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Service
public class EventSpeakerService {

    private final DSLContext dsl;
    private final SpeakerService speakerService;

    public EventSpeakerService(final DSLContext dsl, final SpeakerService speakerService) {
        this.dsl = dsl;
        this.speakerService = speakerService;
    }

    public EventSpeakerRecord newRecord() {
        return dsl.newRecord(EVENT_SPEAKER);
    }

    public Optional<EventSpeakerRecord> get(final Long eventId, final Long speakerId) {
        return Optional.ofNullable(dsl.selectFrom(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.EVENT_ID.eq(eventId).and(EVENT_SPEAKER.SPEAKER_ID.eq(speakerId)))
                .fetchOne());
    }

    public void store(final EventSpeakerRecord eventSpeaker) {
        eventSpeaker.store();
    }

    public void deleteEventSpeakers(final Long eventId) {
        dsl.delete(EVENT_SPEAKER).where(EVENT_SPEAKER.EVENT_ID.eq(eventId)).execute();
    }

    public Stream<SpeakerRecord> getSpeakersForEvent(final Long eventId) {
        return dsl.select(SPEAKER.asterisk())
                .from(SPEAKER)
                .leftJoin(EVENT_SPEAKER).on(SPEAKER.ID.eq(EVENT_SPEAKER.SPEAKER_ID))
                .where(EVENT_SPEAKER.EVENT_ID.eq(eventId))
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .fetch()
                .stream()
                .map(record -> speakerService.get(record.get(SPEAKER.ID)).orElse(null));
        // TODO get rid of the last map operation by help of jOOQ
    }
}
