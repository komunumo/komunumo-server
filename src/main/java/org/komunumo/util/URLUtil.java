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

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public class URLUtil {

    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|)((ht|f)tp(s?)://|www\\.)"
                    + "(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};]*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static String extractLink(@NotNull final String text) {
        final var matcher = urlPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static String createReadableUrl(@NotNull final String text) {
        return URLEncoder.encode(
                text.toLowerCase(Locale.getDefault())
                        .replace("ä", "ae")
                        .replace("ö", "oe")
                        .replace("ü", "ue")
                        .replace("ß", "ss")
                        .replaceAll("[^a-z0-9_\\-\\s]", "")
                        .replaceAll("\\s", "-"),
                StandardCharsets.UTF_8);
    }

    public static String getDomainFromUrl(@NotNull final String url) {
        return url.replaceAll(".+//|www.|/.+|/$", "");
    }

    public URLUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String encode(@NotNull final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (@NotNull final UnsupportedEncodingException e) {
            return "";
        }
    }
}
