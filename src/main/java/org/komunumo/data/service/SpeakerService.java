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
import org.komunumo.data.entity.Speaker;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.count;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Service
public class SpeakerService {

    private final DSLContext dsl;

    public SpeakerService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Speaker newSpeaker() {
        return new Speaker(dsl.newRecord(SPEAKER))
                .setFirstName("")
                .setLastName("")
                .setCompany("")
                .setEmail("")
                .setTwitter("");
    }

    public Stream<Speaker> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter + "%";
        return dsl.selectFrom(SPEAKER)
                .where(filterValue == null ? DSL.noCondition() :
                        SPEAKER.FIRST_NAME.like(filterValue)
                                .or(SPEAKER.LAST_NAME.like(filterValue))
                                .or(SPEAKER.COMPANY.like(filterValue))
                                .or(SPEAKER.EMAIL.like(filterValue))
                                .or(SPEAKER.TWITTER.like(filterValue)))
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .fetch()
                .stream()
                .map(Speaker::new);
    }

    public Optional<Speaker> get(@NotNull final Long id) {
        final var record = dsl.selectFrom(SPEAKER).where(SPEAKER.ID.eq(id)).fetchOne();
        return Optional.ofNullable(record == null ? null : new Speaker(record));
    }

    public void store(@NotNull final Speaker speaker) {
        speaker.getRecord().store();
    }

    public void delete(@NotNull final Speaker speaker) {
        speaker.getRecord().delete();
    }

    public void updateEventCount(@NotNull final Speaker speaker) {
        final var record = speaker.getRecord();
        final var result = dsl.select(count())
                .from(EVENT_SPEAKER)
                .where(EVENT_SPEAKER.SPEAKER_ID.eq(speaker.getId()))
                .fetchOne();
        final var eventCount = result != null ? result.value1().longValue() : 0;
        record.set(SPEAKER.EVENT_COUNT, eventCount);
        record.store();
    }

}
