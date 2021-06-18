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
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.concat;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Service
public class SpeakerService {

    private final DSLContext dsl;

    public SpeakerService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public SpeakerRecord newSpeaker() {
        final var speaker = dsl.newRecord(SPEAKER);
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

    public Stream<SpeakerRecord> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.selectFrom(SPEAKER)
                .where(filterValue == null ? DSL.noCondition() :
                        concat(concat(SPEAKER.FIRST_NAME, " "), SPEAKER.LAST_NAME).like(filterValue)
                                .or(SPEAKER.COMPANY.like(filterValue))
                                .or(SPEAKER.EMAIL.like(filterValue))
                                .or(SPEAKER.TWITTER.like(filterValue)))
                .orderBy(SPEAKER.FIRST_NAME, SPEAKER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .stream();
    }

    public Optional<SpeakerRecord> get(@NotNull final Long id) {
        return Optional.ofNullable(dsl.selectFrom(SPEAKER).where(SPEAKER.ID.eq(id)).fetchOne());
    }

    public void store(@NotNull final SpeakerRecord speaker) {
        speaker.store();
    }

    public void delete(@NotNull final SpeakerRecord speaker) {
        speaker.delete();
    }

}
