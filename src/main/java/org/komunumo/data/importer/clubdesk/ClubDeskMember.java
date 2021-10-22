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

package org.komunumo.data.importer.clubdesk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class ClubDeskMember {

    private final LocalDate membershipBeginDate;
    private final LocalDate membershipEndDate;

    private final Integer membershipId;
    private final Integer membershipFee;

    private final String firstName;
    private final String lastName;
    private final String company;
    private final String email;

    private final String address;
    private final String zipCode;
    private final String city;

    public ClubDeskMember(@Nullable final LocalDate membershipBeginDate,
                          @Nullable final LocalDate membershipEndDate,
                          @Nullable final Integer membershipId,
                          @Nullable final Integer membershipFee,
                          @NotNull final String firstName,
                          @NotNull final String lastName,
                          @NotNull final String company,
                          @NotNull final String email,
                          @NotNull final String address,
                          @NotNull final String zipCode,
                          @NotNull final String city) {
        this.membershipBeginDate = membershipBeginDate;
        this.membershipEndDate = membershipEndDate;
        this.membershipId = membershipId;
        this.membershipFee = membershipFee;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.email = email;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
    }

    public LocalDate getMembershipBeginDate() {
        return membershipBeginDate;
    }

    public LocalDate getMembershipEndDate() {
        return membershipEndDate;
    }

    public Integer getMembershipId() {
        return membershipId;
    }

    public Integer getMembershipFee() {
        return membershipFee;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClubDeskMember that = (ClubDeskMember) o;
        return membershipId == that.membershipId;
    }

    @Override
    public int hashCode() {
        return membershipId;
    }

    @Override
    public String toString() {
        return "ClubDeskMember{" +
                "membershipBeginDate=" + membershipBeginDate +
                ", membershipEndDate=" + membershipEndDate +
                ", membershipId=" + membershipId +
                ", membershipFee=" + membershipFee +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", company='" + company + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
