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
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.entity.EventSpeakerEntity;
import org.komunumo.data.entity.SpeakerListEntity;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.concat;
import static org.komunumo.data.db.tables.EventSpeaker.EVENT_SPEAKER;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

interface SpeakerService extends DSLContextGetter {

    default SpeakerRecord newSpeaker() {
        final var speaker = dsl().newRecord(SPEAKER);
        speaker.setFirstName("");
        speaker.setLastName("");
        speaker.setCompany("");
        speaker.setBio("");
        speaker.setPhoto("");
        speaker.setEmail("");
        speaker.setTwitter("");
        speaker.setLinkedin("");
        speaker.setWebsite("");
        speaker.setAddress("");
        speaker.setZipCode("");
        speaker.setCity("");
        speaker.setState("");
        speaker.setCountry("");
        return speaker;
    }

    default Stream<EventSpeakerEntity> getAllEventSpeakers() {
        return dsl().select(SPEAKER.ID, SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME, SPEAKER.COMPANY, SPEAKER.PHOTO, SPEAKER.BIO)
                .from(SPEAKER)
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .fetchInto(EventSpeakerEntity.class)
                .stream();
    }

    default Stream<SpeakerListEntity> findSpeakers(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().select(SPEAKER.ID, SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME, SPEAKER.COMPANY, SPEAKER.WEBSITE, SPEAKER.EMAIL, SPEAKER.TWITTER,
                        DSL.count(EVENT_SPEAKER.EVENT_ID).as("event_count"))
                .from(SPEAKER)
                .leftJoin(EVENT_SPEAKER).on(SPEAKER.ID.eq(EVENT_SPEAKER.SPEAKER_ID))
                .where(filterValue == null ? DSL.noCondition() :
                        concat(SPEAKER.FIRST_NAME, DSL.value(" "), SPEAKER.LAST_NAME).like(filterValue)
                                .or(SPEAKER.COMPANY.like(filterValue))
                                .or(SPEAKER.EMAIL.like(filterValue))
                                .or(SPEAKER.TWITTER.like(filterValue)))
                .groupBy(SPEAKER.ID)
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .fetchInto(SpeakerListEntity.class)
                .stream();
    }

    default Optional<SpeakerRecord> getSpeakerRecord(@NotNull final Long id) {
        return dsl().selectFrom(SPEAKER)
                .where(SPEAKER.ID.eq(id))
                .fetchOptional();
    }

    default Optional<SpeakerRecord> getSpeaker(@NotNull final String email) {
        return dsl().selectFrom(SPEAKER)
                .where(SPEAKER.EMAIL.eq(email))
                .fetchOptional();
    }

    default Optional<SpeakerRecord> getSpeaker(@NotNull final String firstName,
                                        @NotNull final String lastName,
                                        @NotNull final String company) {
        return dsl().selectFrom(SPEAKER)
                .where(SPEAKER.FIRST_NAME.eq(firstName)
                        .and(SPEAKER.LAST_NAME.eq(lastName))
                        .and(SPEAKER.COMPANY.eq(company)))
                .fetchOptional();
    }

    default void deleteSpeaker(final long speakerId) {
        getSpeakerRecord(speakerId).ifPresent(SpeakerRecord::delete);
    }

}
