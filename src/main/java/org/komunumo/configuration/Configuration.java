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

package org.komunumo.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class Configuration {

    private final Map<String, String> configuration;

    public Configuration(@NotNull Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public String getWebsiteBaseUrl() {
        return configuration.getOrDefault("website.url", "http://localhost:8080");
    }

    public String getWebsiteName() {
        return configuration.getOrDefault("website.name", "");
    }

    public String getWebsiteContactAddress() {
        return configuration.getOrDefault("website.contact.address", "");
    }

    public String getWebsiteContactEmail() {
        return configuration.getOrDefault("website.contact.email", "noreply@localhost");
    }

    public String getWebsiteCopyright() {
        return configuration.getOrDefault("website.copyright", "");
    }

    public String getWebsiteAboutText() {
        return configuration.getOrDefault("website.about.text", "");
    }

    public String getWebsiteLogo() {
        return configuration.getOrDefault("website.logo", "");
    }

    public int getWebsiteLogoWidth() {
        return Integer.parseInt(configuration.getOrDefault("website.logo.width", "0"));
    }

    public int getWebsiteLogoHeight() {
        return Integer.parseInt(configuration.getOrDefault("website.logo.height", "0"));
    }

    public String getWebsiteLogoTemplate() {
        return configuration.getOrDefault("website.logo.template", "");
    }

    public int getWebsiteMinLogoNumber() {
        return Integer.parseInt(configuration.getOrDefault("website.logo.min", "0"));
    }

    public int getWebsiteMaxLogoNumber() {
        return Integer.parseInt(configuration.getOrDefault("website.logo.max", "0"));
    }

}
