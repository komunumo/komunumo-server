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

import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static java.time.Month.JANUARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateUtilTest {

    @Test
    void dateToLocalDateWithNull() {
        assertNull(DateUtil.dateToLocalDate(null));
    }

    @Test
    void dateToLocalDateWithDate() {
        final var calendar = Calendar.getInstance();
        calendar.set(1974, Calendar.JANUARY, 30, 1, 0);
        final var date = calendar.getTime();

        final var localDate = DateUtil.dateToLocalDate(date);

        assertEquals(1974, localDate.getYear());
        assertEquals(JANUARY, localDate.getMonth());
        assertEquals(30, localDate.getDayOfMonth());
    }

}
