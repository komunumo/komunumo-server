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
import org.jooq.impl.DSL;
import org.komunumo.data.entity.MailTemplateId;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.util.URLUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.concat;
import static org.komunumo.data.db.tables.Member.MEMBER;

interface MemberService extends DSLContextGetter, MailService {

    default Member newMember() {
        final var member = dsl().newRecord(MEMBER)
                .into(Member.class);
        member.setFirstName("");
        member.setLastName("");
        member.setCompany("");
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
        member.setAccountActive(false);
        member.setAccountBlocked(false);
        member.setAccountBlockedReason("");
        member.setAccountDeleted(false);
        member.setComment("");
        return member;
    }

    default Stream<Member> findMembers(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().select(MEMBER.asterisk())
                .from(MEMBER)
                .where(MEMBER.ACCOUNT_DELETED.isFalse().and(
                        filterValue == null ? DSL.noCondition()
                        : concat(concat(MEMBER.FIRST_NAME, " "), MEMBER.LAST_NAME).like(filterValue)
                                .or(MEMBER.EMAIL.like(filterValue))))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .fetchInto(Member.class)
                .stream();
    }

    default Optional<Member> getMember(@NotNull final Long id) {
        return dsl().selectFrom(MEMBER)
                .where(MEMBER.ID.eq(id)
                        .and(MEMBER.ACCOUNT_DELETED.isFalse()))
                .fetchOptionalInto(Member.class);
    }

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    default Optional<Member> getMember(@NotNull final Long id, final boolean ignoreDeleted) {
        return dsl().selectFrom(MEMBER)
                .where(MEMBER.ID.eq(id)
                        .and(ignoreDeleted ? DSL.noCondition() : MEMBER.ACCOUNT_DELETED.isFalse()))
                .fetchOptionalInto(Member.class);
    }

    default Optional<Member> getMemberByEmail(@NotNull final String email) {
        return dsl().selectFrom(MEMBER)
                .where(MEMBER.EMAIL.eq(email)
                        .and(MEMBER.ACCOUNT_DELETED.isFalse()))
                .orderBy(MEMBER.REGISTRATION_DATE.desc())
                .limit(1)
                .fetchOptionalInto(Member.class);
    }

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated
    default Optional<Member> getMemberByName(@NotNull final String firstName, @NotNull final String lastName) {
        return dsl().selectFrom(MEMBER)
                .where(MEMBER.FIRST_NAME.eq(firstName)
                        .and(MEMBER.LAST_NAME.eq(lastName))
                        .and(MEMBER.ACCOUNT_DELETED.isFalse()))
                .orderBy(MEMBER.REGISTRATION_DATE.desc())
                .limit(1)
                .fetchOptionalInto(Member.class);
    }

    private void anonymizeMember(@NotNull final Member member) {
        member.setFirstName(RandomStringUtils.randomAlphabetic(32));
        member.setLastName(RandomStringUtils.randomAlphabetic(32));
        member.setCompany("");
        member.setEmail(RandomStringUtils.randomAlphabetic(32));
        member.setAddress("");
        member.setZipCode("");
        member.setCity("");
        member.setState("");
        member.setCountry("");
        member.setAdmin(false);
        member.setPasswordHash("");
        member.setActivationCode("");
        member.setAccountActive(false);
        member.setAccountBlocked(false);
        member.setAccountBlockedReason("");
        member.setAccountDeleted(true);
        member.setComment("");
        member.store();
    }

    /**
     * The member will not be deleted from the database. The entity will be
     * anonymized and marked as deleted. Reason: The ID of the member is used
     * in a lot of references. To keep the references intact and not to get
     * wrong statistics, the record stays in the database forever.
     *
     * @param member the member to be deleted
     */
    default void deleteMember(@NotNull final Member member) {
        anonymizeMember(member);
    }

    default Stream<Member> getAllAdmins() {
        return dsl().selectFrom(MEMBER)
                .where(MEMBER.ADMIN.isTrue()
                        .and(MEMBER.ACCOUNT_DELETED.isFalse()))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .fetchInto(Member.class)
                .stream();
    }

    default Member createMember(@NotNull final String firstName, @NotNull final String lastName, @NotNull final String emailAddress) {
        final var activationCode = RandomStringUtils.randomAlphanumeric(16);

        final var member = newMember();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(emailAddress);
        member.setActivationCode(activationCode);
        member.store();

        final var variables = Map.of(
                "email", emailAddress,
                "website.name", configuration().getWebsiteName(),
                "validation.url", "%s/member/validate?email=%s&code=%s".formatted(
                        configuration().getWebsiteBaseUrl(),
                        URLUtil.encode(emailAddress),
                        URLUtil.encode(activationCode)));
        sendMail(MailTemplateId.MEMBER_CONFIRM_EMAIL, variables, emailAddress);

        return member;
    }

}
