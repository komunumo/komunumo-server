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
import org.komunumo.data.db.tables.records.MemberRecord;
import org.springframework.stereotype.Service;

import static org.komunumo.data.db.tables.Member.MEMBER;

@Service
public class MemberService {

    private final DSLContext dsl;

    public MemberService(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public MemberRecord newRecord() {
        return dsl.newRecord(MEMBER);
    }

    public Stream<MemberRecord> list(final int offset, final int limit) {
        return dsl.selectFrom(MEMBER).offset(offset).limit(limit).stream();
    }

    public Optional<MemberRecord> get(final Long id) {
        return Optional.ofNullable(dsl.selectFrom(MEMBER).where(MEMBER.ID.eq(id)).fetchOne());
    }

    public Optional<MemberRecord> getByEmail(final String email) {
        return Optional.ofNullable(dsl.selectFrom(MEMBER).where(MEMBER.EMAIL.eq(email)).fetchOne());
    }

    public void store(final MemberRecord member) {
        member.store();
    }

}
