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

package org.komunumo.ui.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class TagFieldTest {

    @Test
    void inputAndOutputAreEqual() {
        final var tagField = new TagField("Unit test");
        tagField.setValue("test1,test2,test3,test4,test5");
        assertEquals("test1,test2,test3,test4,test5", tagField.getValue());
    }

    @Test
    void outputIsOrdered() {
        final var tagField = new TagField("Unit test");
        tagField.setValue("test3,test1,test5,test4,test2");
        assertEquals("test1,test2,test3,test4,test5", tagField.getValue());
    }

    @Test
    void outputIsTrimmed() {
        final var tagField = new TagField("Unit test");
        tagField.setValue("  test3,test1, test5 , test4,  test2   ");
        assertEquals("test1,test2,test3,test4,test5", tagField.getValue());
    }


    @Test
    void spaceIsIgnored() {
        final var tagField = new TagField("Unit test");
        tagField.setValue("  ,,   ,test1,test2,   ,");
        assertEquals("test1,test2", tagField.getValue());
    }

}
