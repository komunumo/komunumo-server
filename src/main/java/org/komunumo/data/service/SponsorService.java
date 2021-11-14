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
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.entity.SponsorEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Sponsor.SPONSOR;

@Service
public class SponsorService {

    private final DSLContext dsl;

    public SponsorService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public SponsorRecord newSponsor() {
        final var sponsor = dsl.newRecord(SPONSOR);
        sponsor.setName("");
        sponsor.setWebsite("");
        sponsor.setLogo("");
        return sponsor;
    }

    public int count() {
        return dsl.fetchCount(SPONSOR);
    }

    public Stream<SponsorEntity> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.select(SPONSOR.asterisk())
                .from(SPONSOR)
                .where(filterValue == null ? DSL.noCondition() : SPONSOR.NAME.like(filterValue))
                .orderBy(SPONSOR.NAME)
                .offset(offset)
                .limit(limit)
                .fetchInto(SponsorEntity.class)
                .stream();
    }

    public Optional<SponsorRecord> getSponsorRecord(@NotNull final Long id) {
        return dsl.selectFrom(SPONSOR)
                .where(SPONSOR.ID.eq(id))
                .fetchOptional();
    }

    public void store(@NotNull final SponsorRecord sponsorRecord) {
        sponsorRecord.store();
    }

    public void delete(final long sponsorId) {
        getSponsorRecord(sponsorId).ifPresent(SponsorRecord::delete);
    }

}
