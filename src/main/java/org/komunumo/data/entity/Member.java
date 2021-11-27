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

import org.komunumo.data.db.tables.records.MemberRecord;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Member extends MemberRecord {

    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName()).trim();
    }

    public Set<Role> getRoles() {
        final var roles = new HashSet<Role>();
        if (getAccountActive() && !getAccountBlocked() && !getPasswordChange() && !getAccountDeleted()) {
            if (getAdmin()) {
                roles.add(Role.ADMIN);
            }
            if (isMembershipActive()) {
                roles.add(Role.MEMBER);
            }
        }
        return Set.copyOf(roles);
    }

    public boolean isMembershipActive() {
        final var today = LocalDate.now();
        final var membershipBegin = getMembershipBegin();
        final var membershipEnd = getMembershipEnd();
        return (membershipBegin != null && membershipBegin.minusDays(1).isBefore(today))
                && (membershipEnd == null || membershipEnd.plusDays(1).isAfter(today));
    }

}
