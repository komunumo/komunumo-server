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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URLUtilTest {

    @Test
    @SuppressWarnings("HttpUrlsUsage")
    void extractLink() {
        assertEquals("", URLUtil.extractLink(""));
        assertEquals("", URLUtil.extractLink("There is no link in this text!"));
        assertEquals("http://komunumo.org", URLUtil.extractLink("Go to http://komunumo.org and try it out!"));
        assertEquals("https://komunumo.org", URLUtil.extractLink("Go to https://komunumo.org and try it out!"));
        assertEquals("http://komunumo.org/", URLUtil.extractLink("Go to http://komunumo.org/ and try it out!"));
        assertEquals("https://komunumo.org/", URLUtil.extractLink("Go to https://komunumo.org/ and try it out!"));
        assertEquals("https://komunumo.org/test", URLUtil.extractLink("Go to https://komunumo.org/test and try it out!"));
        assertEquals("https://komunumo.org/test/", URLUtil.extractLink("Go to https://komunumo.org/test/ and try it out!"));
        assertEquals("https://komunumo.org/test.html", URLUtil.extractLink("Go to https://komunumo.org/test.html and try it out!"));
        assertEquals("https://komunumo.org/test.php", URLUtil.extractLink("Go to https://komunumo.org/test.php and try it out!"));
        assertEquals("https://komunumo.org/test.pdf", URLUtil.extractLink("Go to https://komunumo.org/test.pdf and try it out!"));
        assertEquals("https://komunumo.org/test.pdf", URLUtil.extractLink("Go to \"https://komunumo.org/test.pdf\" and try it out!"));
        assertEquals("https://komunumo.org/test.pdf", URLUtil.extractLink("Go to 'https://komunumo.org/test.pdf' and try it out!"));
    }

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

    @Test
    @SuppressWarnings("HttpUrlsUsage")
    void isValid() {
        assertFalse(URLUtil.isValid(null));
        assertFalse(URLUtil.isValid(""));
        assertFalse(URLUtil.isValid("   "));
        assertFalse(URLUtil.isValid("test"));
        assertFalse(URLUtil.isValid("http://non-existing.domain/"));
        assertFalse(URLUtil.isValid("komunumo.org"));
        assertFalse(URLUtil.isValid("www.komunumo.org"));
        assertFalse(URLUtil.isValid("http://"));
        assertFalse(URLUtil.isValid("https://"));
        assertTrue(URLUtil.isValid("http://komunumo.org"));
        assertTrue(URLUtil.isValid("http://komunumo.org/"));
        assertTrue(URLUtil.isValid("http://www.komunumo.org"));
        assertTrue(URLUtil.isValid("http://www.komunumo.org/"));
        assertTrue(URLUtil.isValid("https://komunumo.org"));
        assertTrue(URLUtil.isValid("https://komunumo.org/"));
        assertTrue(URLUtil.isValid("https://www.komunumo.org"));
        assertTrue(URLUtil.isValid("https://www.komunumo.org/"));
    }

    @Test
    void privateConstructorWithException() {
        final var cause = assertThrows(InvocationTargetException.class, () -> {
            Constructor<URLUtil> constructor = URLUtil.class.getDeclaredConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        }).getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertEquals("Utility class", cause.getMessage());
    }

}
