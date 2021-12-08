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

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class URLUtilTest {

    @Test
    void createReadableUrl() {
        assertEquals("hans-im-glueck", URLUtil.createReadableUrl("Hans im Glück"));
        assertEquals("i-have_a-reallyamazingidea", URLUtil.createReadableUrl("I have_a-really\\Amazing§§Idea"));
        assertEquals("count-from-1-to-3", URLUtil.createReadableUrl("Count from 1 to 3"));
    }

    @Test
    @SuppressWarnings("HttpUrlsUsage")
    void getDomainFromUrl() {
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("komunumo.org"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("www.komunumo.org"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("http://komunumo.org"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("http://komunumo.org/"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("http://www.komunumo.org/"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("https://komunumo.org/"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("https://komunumo.org/index.html"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("https://komunumo.org/subdir"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("https://komunumo.org/subdir/"));
        assertEquals("komunumo.org", URLUtil.getDomainFromUrl("https://komunumo.org/subdir/index.html"));
        assertEquals("", URLUtil.getDomainFromUrl(""));
    }
}
