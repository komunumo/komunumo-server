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

package org.komunumo.data.entity;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MemberTest {

    private static Member createMemberForTest(@Nullable final LocalDate membershipBegin, @Nullable final LocalDate membershipEnd) {
        final var member = mock(Member.class);
        when(member.getMembershipBegin()).thenReturn(membershipBegin);
        when(member.getMembershipEnd()).thenReturn(membershipEnd);
        doCallRealMethod().when(member).isMembershipActive();
        return member;
    }

    @Test
    void neverBeenMemberShouldReturnFalse() {
        final var member = createMemberForTest(null, null);
        assertFalse(member.isMembershipActive());
    }

    @Test
    void hasBeenMemberBeforeShouldReturnFalse() {
        final var member = createMemberForTest(LocalDate.now().minusYears(2), LocalDate.now().minusYears(1));
        assertFalse(member.isMembershipActive());
    }

    @Test
    void isMemberWithoutEndShouldReturnTrue() {
        final var member = createMemberForTest(LocalDate.now().minusYears(1), null);
        assertTrue(member.isMembershipActive());
    }

    @Test
    void isMemberWithEndInFutureShouldReturnTrue() {
        final var member = createMemberForTest(LocalDate.now().minusYears(1), LocalDate.now().plusYears(1));
        assertTrue(member.isMembershipActive());
    }

    @Test
    void isMemberWithBeginTodayShouldReturnTrue() {
        final var member = createMemberForTest(LocalDate.now(), LocalDate.now().plusYears(1));
        assertTrue(member.isMembershipActive());
    }

    @Test
    void isMemberWithEndTodayShouldReturnTrue() {
        final var member = createMemberForTest(LocalDate.now().minusYears(1), LocalDate.now());
        assertTrue(member.isMembershipActive());
    }

    @Test
    void isMemberWithBeginTomorrowShouldReturnFalse() {
        final var member = createMemberForTest(LocalDate.now().plusYears(1), LocalDate.now().plusYears(1));
        assertFalse(member.isMembershipActive());
    }

    @Test
    void isMemberWithEndYesterdayShouldReturnFalse() {
        final var member = createMemberForTest(LocalDate.now().minusYears(1), LocalDate.now().minusDays(1));
        assertFalse(member.isMembershipActive());
    }

}
