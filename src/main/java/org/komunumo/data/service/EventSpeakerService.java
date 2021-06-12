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

import java.util.Optional;

import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventSpeakerRecord;
import org.springframework.stereotype.Service;

import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;

@Service
public class EventSpeakerService {

    private final DSLContext dsl;

    public EventSpeakerService(final DSLContext dsl) {
        this.dsl = dsl;
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

}
