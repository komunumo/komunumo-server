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

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.komunumo.data.entity.Member;
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

    public Member newMember() {
        final var member = dsl.newRecord(MEMBER)
                .into(Member.class);
        member.setFirstName("");
        member.setLastName("");
        member.setEmail("");
        member.setAdmin(false);
        member.setAddress("");
        member.setZipCode("");
        member.setCity("");
        member.setState("");
        member.setCountry("");
        member.setRegistrationDate(LocalDateTime.now());
        member.setMembershipBegin(null);
        member.setMembershipEnd(null);
        member.setAdmin(false);
        member.setActive(false);
        member.setBlocked(false);
        member.setBlockedReason("");
        member.setDeleted(false);
        return member;
    }

    public int count() {
        return dsl.fetchCount(MEMBER, MEMBER.DELETED.isFalse());
    }

    public Stream<Member> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.select(MEMBER.asterisk())
                .from(MEMBER)
                .where(MEMBER.DELETED.isFalse().and(
                        filterValue == null ? DSL.noCondition() :
                        concat(concat(MEMBER.FIRST_NAME, " "), MEMBER.LAST_NAME).like(filterValue)
                                .or(MEMBER.EMAIL.like(filterValue))))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .fetchInto(Member.class)
                .stream();
    }

    public Optional<Member> get(@NotNull final Long id) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.ID.eq(id)
                        .and(MEMBER.DELETED.isFalse()))
                .fetchOptionalInto(Member.class);
    }

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    public Optional<Member> get(@NotNull final Long id, final boolean ignoreDeleted) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.ID.eq(id)
                        .and(ignoreDeleted ? DSL.noCondition() : MEMBER.DELETED.isFalse()))
                .fetchOptionalInto(Member.class);
    }

    public Optional<Member> getByEmail(@NotNull final String email) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.EMAIL.eq(email)
                        .and(MEMBER.DELETED.isFalse()))
                .orderBy(MEMBER.REGISTRATION_DATE.desc())
                .limit(1)
                .fetchOptionalInto(Member.class);
    }

    public void store(@NotNull final Member member) {
        member.store();
    }

    public void delete(@NotNull final Member member) {
        try {
            member.delete();
        } catch (final Exception e) {
            // member id used in foreign keys
            // need to keep the member entity
            // just anonymize member data
            member.setFirstName(RandomStringUtils.randomAlphabetic(32));
            member.setLastName(RandomStringUtils.randomAlphabetic(32));
            member.setEmail(RandomStringUtils.randomAlphabetic(32));
            member.setAddress("");
            member.setZipCode("");
            member.setCity("");
            member.setState("");
            member.setCountry("");
            member.setAdmin(false);
            member.setPasswordSalt("");
            member.setPasswordHash("");
            member.setActivationCode("");
            member.setActive(false);
            member.setBlocked(false);
            member.setBlockedReason("");
            member.setDeleted(true);
            store(member);
        }
    }

    public Stream<Member> getAllAdmins() {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.ADMIN.isTrue()
                        .and(MEMBER.DELETED.isFalse()))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .fetchInto(Member.class)
                .stream();
    }
}
