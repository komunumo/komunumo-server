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

package org.komunumo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

// TODO unnötig
//  @RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
// TODO unnötig
//  @TestPropertySource("classpath:application.properties")
public class ConfigurationTest {

    @Autowired
    private Configuration configuration;

    @Test
    public void whenSimplePropertyQueriedThenReturnsPropertyValue() {
        assertEquals("Incorrectly bound version property",
                "1.0-SNAPSHOT", configuration.getVersion());
    }

    @Test
    public void whenNestedPropertyQueriedThenReturnsPropertyValue() {
        assertEquals("Incorrectly bound admin email property",
                "root@localhost", configuration.getAdmin().getEmail());
        assertEquals("Incorrectly bound email sender property",
                "noreply@localhost", configuration.getEmail().getAddress());
    }

}
