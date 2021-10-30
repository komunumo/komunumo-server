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

import java.time.ZonedDateTime;
import java.util.Locale;

public record BigMarkerRegistration(String firstName, String lastName, String email,
                                    ZonedDateTime registrationDate, boolean unsubscribed, boolean attendedLive,
                                    String membership) {

    @Override
    public String email() {
        if (email == null && firstName.startsWith("Guest-")) {
            return String.format("%s@bigmarker.com", firstName.replaceAll("[()]", "")
                    .toLowerCase(Locale.getDefault()));
        }
        return email;
    }

    public boolean noShow() {
        return !attendedLive() && !unsubscribed();
    }

}
