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
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.concat;
import static org.komunumo.data.db.tables.Member.MEMBER;

@Service
public class MemberService {

    private final DSLContext dsl;

    public MemberService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public MemberRecord newMember() {
        final var member = dsl.newRecord(MEMBER);
        member.setFirstName("");
        member.setLastName("");
        member.setEmail("");
        member.setAdmin(false);
        member.setAddress("");
        member.setZipCode("");
        member.setCity("");
        member.setState("");
        member.setCountry("");
        member.setMemberSince(LocalDateTime.now());
        member.setAdmin(false);
        member.setActive(false);
        member.setBlocked(false);
        member.setBlockedReason("");
        return member;
    }

    public Stream<Record> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.select(MEMBER.asterisk())
                .from(MEMBER)
                .where(filterValue == null ? DSL.noCondition() :
                        concat(concat(MEMBER.FIRST_NAME, " "), MEMBER.LAST_NAME).like(filterValue)
                                .or(MEMBER.EMAIL.like(filterValue)))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .stream();
    }

    public Optional<MemberRecord> get(@NotNull final Long id) {
        return Optional.ofNullable(dsl.selectFrom(MEMBER).where(MEMBER.ID.eq(id)).fetchOne());
    }

    public Optional<MemberRecord> getByEmail(@NotNull final String email) {
        return Optional.ofNullable(dsl.selectFrom(MEMBER).where(MEMBER.EMAIL.eq(email)).fetchOne());
    }

    public void store(@NotNull final MemberRecord member) {
        member.store();
    }

    public void delete(@NotNull final MemberRecord member) {
        member.delete();
    }

}
