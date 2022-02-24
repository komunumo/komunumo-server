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

package org.komunumo.data.entity.reports;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused") // getters used by jasper reports
public final class RegistrationListEntityWrapper {

    private final String attendee;
    private final String city;

    public RegistrationListEntityWrapper(@NotNull final String firstName, @NotNull final String lastName, @NotNull final String city) {
        this.attendee = "%s %s".formatted(firstName, lastName).trim();
        this.city = city;
    }

    public String getAttendee() {
        return attendee;
    }

    public String getCity() {
        return city;
    }

    public String getCheck() {
        return "[   ]";
    }

}
