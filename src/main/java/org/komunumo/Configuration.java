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

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "komunumo")
public class Configuration {

    public static class Admin {

        /**
         * The email address of the administrator.
         */
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(@NotNull final String email) {
            this.email = email;
        }
    }

    public static class Email {

        /**
         * The email address used as the default from address.
         */
        private String address = "noreply@localhost";

        public String getAddress() {
            return address;
        }

        public void setAddress(@NotNull final String address) {
            this.address = address;
        }
    }

    /**
     * Version information of Komunumo.
     */
    private String version;

    /**
     * Configure the administrator.
     */
    private Admin admin;

    /**
     * Configure email settings.
     */
    private Email email;

    public String getVersion() {
        return version;
    }

    public void setVersion(@NotNull final String version) {
        this.version = version;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(@NotNull final Admin admin) {
        this.admin = admin;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(@NotNull final Email email) {
        this.email = email;
    }

}
