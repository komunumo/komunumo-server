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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class GravatarUtilTest {

    @Test
    void getGravatarAddressWithoutSize() {
        final var address = GravatarUtil.getGravatarAddress("test@komunumo.org");
        assertEquals("https://www.gravatar.com/avatar/dbe68b110371ad7ba90ad31b0ea1512c?d=mp&s=80", address);
    }

    @Test
    void getGravatarAddressWithSize100() {
        final var address = GravatarUtil.getGravatarAddress("test@komunumo.org", 100);
        assertEquals("https://www.gravatar.com/avatar/dbe68b110371ad7ba90ad31b0ea1512c?d=mp&s=100", address);
    }

    @Test
    void getGravatarAddressWithInvalidSize() {
        final var tooSmall = assertThrows(IllegalArgumentException.class,
                () -> GravatarUtil.getGravatarAddress("test@komunumo.org", 0));
        assertEquals("The size must be between 1 and 2'048!", tooSmall.getMessage());

        final var tooBig = assertThrows(IllegalArgumentException.class,
                () -> GravatarUtil.getGravatarAddress("test@komunumo.org", 2049));
        assertEquals("The size must be between 1 and 2'048!", tooBig.getMessage());
    }

    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // this is exactly what we want to test
    void privateConstructorWithException() {
        final var cause = assertThrows(InvocationTargetException.class, () -> {
            Constructor<GravatarUtil> constructor = GravatarUtil.class.getDeclaredConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        }).getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertEquals("Utility class", cause.getMessage());
    }

}
