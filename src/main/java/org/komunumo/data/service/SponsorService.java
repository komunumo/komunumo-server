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
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.springframework.stereotype.Service;

import static org.komunumo.data.db.tables.Sponsor.SPONSOR;

@Service
public class SponsorService {

    private final DSLContext dsl;

    public SponsorService(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public SponsorRecord newRecord() {
        return dsl.newRecord(SPONSOR);
    }

    public Stream<SponsorRecord> list(final int offset, final int limit) {
        return dsl.selectFrom(SPONSOR).offset(offset).limit(limit).stream();
    }

    public void update(final SponsorRecord sponsor) {
        sponsor.update();
    }

    public Optional<SponsorRecord> get(final Long id) {
        return Optional.ofNullable(dsl.selectFrom(SPONSOR).where(SPONSOR.ID.eq(id)).fetchOne());
    }

    public void store(final SponsorRecord sponsor) {
        sponsor.store();
    }
}
