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
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorDomainRecord;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.entity.SponsorEntity;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Sponsor.SPONSOR;
import static org.komunumo.data.db.tables.SponsorDomain.SPONSOR_DOMAIN;

interface SponsorService extends DSLContextGetter {

    default SponsorRecord newSponsor() {
        final var sponsor = dsl().newRecord(SPONSOR);
        sponsor.setName("");
        sponsor.setWebsite("");
        sponsor.setLogo("");
        sponsor.setDescription("");
        return sponsor;
    }

    default @NotNull SponsorDomainRecord newSponsorDomain() {
        return dsl().newRecord(SPONSOR_DOMAIN);
    }

    default Stream<SponsorEntity> findSponsors(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().select(SPONSOR.asterisk())
                .from(SPONSOR)
                .where(filterValue == null ? DSL.noCondition() : SPONSOR.NAME.like(filterValue))
                .orderBy(SPONSOR.NAME)
                .offset(offset)
                .limit(limit)
                .fetchInto(SponsorEntity.class)
                .stream();
    }

    default Optional<SponsorRecord> getSponsorRecord(@NotNull final Long id) {
        return dsl().selectFrom(SPONSOR)
                .where(SPONSOR.ID.eq(id))
                .fetchOptional();
    }

    default void deleteSponsor(final long sponsorId) {
        getSponsorRecord(sponsorId).ifPresent(sponsorRecord -> {
            deleteSponsorDomains(sponsorRecord);
            sponsorRecord.delete();
        });
    }

    default Set<String> getSponsorDomains(@NotNull final SponsorRecord sponsorRecord) {
        return dsl().selectFrom(SPONSOR_DOMAIN)
                .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(sponsorRecord.getId()))
                .stream()
                .map(SponsorDomainRecord::getDomain)
                .collect(Collectors.toUnmodifiableSet());
    }

    default void setSponsorDomains(@NotNull final SponsorRecord sponsorRecord, @NotNull final Set<String> sponsorDomains) {
        final var newDomains = sponsorDomains.stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet());
        final var oldDomains = getSponsorDomains(sponsorRecord);
        oldDomains.forEach(domain -> {
            if (!newDomains.contains(domain)) {
                dsl().deleteFrom(SPONSOR_DOMAIN)
                        .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(sponsorRecord.getId())
                                .and(SPONSOR_DOMAIN.DOMAIN.eq(domain)))
                        .execute();
            }
        });
        newDomains.forEach(domain -> {
            if (!oldDomains.contains(domain)) {
                dsl().insertInto(SPONSOR_DOMAIN, SPONSOR_DOMAIN.SPONSOR_ID, SPONSOR_DOMAIN.DOMAIN)
                        .values(sponsorRecord.getId(), domain)
                        .execute();
            }
        });
    }

    default Set<String> getActiveSponsorDomains() {
        return dsl().select(SPONSOR_DOMAIN.DOMAIN)
                .from(SPONSOR_DOMAIN, SPONSOR)
                .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(SPONSOR.ID))
                .and(SPONSOR.VALID_FROM.isNull().or(SPONSOR.VALID_FROM.lessOrEqual(LocalDate.now())))
                .and(SPONSOR.VALID_TO.isNull().or(SPONSOR.VALID_TO.greaterOrEqual(LocalDate.now())))
                .stream()
                .map(Record1::value1)
                .collect(Collectors.toUnmodifiableSet());
    }

    default Stream<SponsorEntity> getActiveSponsors(@NotNull final SponsorLevel level) {
        return dsl().selectFrom(SPONSOR)
                .where(SPONSOR.LEVEL.eq(level))
                .and(SPONSOR.VALID_FROM.isNull().or(SPONSOR.VALID_FROM.lessOrEqual(LocalDate.now())))
                .and(SPONSOR.VALID_TO.isNull().or(SPONSOR.VALID_TO.greaterOrEqual(LocalDate.now())))
                .orderBy(SPONSOR.NAME)
                .fetchInto(SponsorEntity.class)
                .stream();
    }

    default void deleteSponsorDomains(@NotNull final SponsorRecord sponsorRecord) {
        dsl().deleteFrom(SPONSOR_DOMAIN)
                .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(sponsorRecord.getId()))
                .execute();
    }
}
