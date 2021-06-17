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

package org.komunumo.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

public class GravatarUtil {

    public static final String GRAVATAR_URL = "https://www.gravatar.com/avatar/";

    public static String getGravatarAddress(@NotNull final String email) {
        return GRAVATAR_URL + DigestUtils.md5Hex(email);
    }

    public static String getGravatarAddress(@NotNull final String email, final int size) {
        if (size < 1 || size > 2048) {
            throw new IllegalArgumentException("The size must be between 1 and 2'048!");
        }
        return GRAVATAR_URL + DigestUtils.md5Hex(email) + "?s=" + size;
    }

}
