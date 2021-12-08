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

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.SponsorDomainRecord;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.entity.SponsorEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Sponsor.SPONSOR;
import static org.komunumo.data.db.tables.SponsorDomain.SPONSOR_DOMAIN;

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

    public @NotNull SponsorDomainRecord newSponsorDomain() {
        return dsl.newRecord(SPONSOR_DOMAIN);
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

    public Set<String> getSponsorDomains(@NotNull final SponsorRecord sponsorRecord) {
        return dsl.selectFrom(SPONSOR_DOMAIN)
                .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(sponsorRecord.getId()))
                .stream()
                .map(SponsorDomainRecord::getDomain)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void setSponsorDomains(@NotNull final SponsorRecord sponsorRecord, @NotNull final Set<String> sponsorDomains) {
        final var newDomains = sponsorDomains.stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet());
        final var oldDomains = getSponsorDomains(sponsorRecord);
        oldDomains.forEach(domain -> {
            if (!newDomains.contains(domain)) {
                dsl.deleteFrom(SPONSOR_DOMAIN)
                        .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(sponsorRecord.getId())
                                .and(SPONSOR_DOMAIN.DOMAIN.eq(domain)))
                        .execute();
            }
        });
        newDomains.forEach(domain -> {
            if (!oldDomains.contains(domain)) {
                dsl.insertInto(SPONSOR_DOMAIN, SPONSOR_DOMAIN.SPONSOR_ID, SPONSOR_DOMAIN.DOMAIN)
                        .values(sponsorRecord.getId(), domain)
                        .execute();
            }
        });
    }

    public Set<String> getActiveSponsorDomains() {
        return dsl.select(SPONSOR_DOMAIN.DOMAIN)
                .from(SPONSOR_DOMAIN, SPONSOR)
                .where(SPONSOR_DOMAIN.SPONSOR_ID.eq(SPONSOR.ID))
                .and(SPONSOR.VALID_FROM.isNull().or(SPONSOR.VALID_FROM.lessOrEqual(LocalDate.now())))
                .and(SPONSOR.VALID_TO.isNull().or(SPONSOR.VALID_TO.greaterOrEqual(LocalDate.now())))
                .stream()
                .map(Record1::value1)
                .collect(Collectors.toUnmodifiableSet());
    }

}
