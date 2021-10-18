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

package org.komunumo.data.importer.bigmarker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Locale;

public class BigMarkerRegistration {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final ZonedDateTime registrationDate;
    private final boolean unsubscribed;
    private final boolean attendedLive;

    public BigMarkerRegistration(@NotNull final String firstName,
                                 @NotNull final String lastName,
                                 @Nullable final String email,
                                 @Nullable final ZonedDateTime registrationDate,
                                 final boolean unsubscribed,
                                 final boolean attendedLive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.registrationDate = registrationDate;
        this.unsubscribed = unsubscribed;
        this.attendedLive = attendedLive;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        if (email == null && firstName.startsWith("Guest-")) {
            return String.format("%s@bigmarker.com", firstName.replaceAll("[()]", "")
                    .toLowerCase(Locale.getDefault()));
        }
        return email;
    }

    public ZonedDateTime getRegistrationDate() {
        return registrationDate;
    }

    public boolean hasAttendedLive() {
        return attendedLive;
    }

    public boolean hasUnsubscribed() {
        return unsubscribed;
    }

    public boolean isNoShow() {
        return !hasAttendedLive() && !hasUnsubscribed();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (BigMarkerRegistration) o;
        return getEmail().equals(that.getEmail());
    }

    @Override
    public int hashCode() {
        return getEmail().hashCode();
    }

    @Override
    public String toString() {
        return "BigMarkerRegistration{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", registrationDate=" + registrationDate +
                ", unsubscribed=" + unsubscribed +
                ", attendedLive=" + attendedLive +
                '}';
    }
}
