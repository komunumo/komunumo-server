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

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Calendar;

import static java.time.Month.JANUARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // this is exactly what we want to test
    void privateConstructorWithException() {
        final var cause = assertThrows(InvocationTargetException.class, () -> {
            Constructor<DateUtil> constructor = DateUtil.class.getDeclaredConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        }).getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertEquals("Utility class", cause.getMessage());
    }

}
