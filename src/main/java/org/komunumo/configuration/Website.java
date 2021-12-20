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

public class Website {

    /**
     * The base URL of the website. Used to generate complete links.
     */
    private String baseUrl = "";

    /**
     * The template to access the website logo (format string supported).
     */
    private String logoUrlTemplate = null;

    /**
     * The minimum number for the logo URL template format string.
     */
    private int minLogoNumber = 0;

    /**
     * The maximum number for the logo URL template format string.
     */
    private int maxLogoNumber = 0;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(@NotNull final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLogoUrlTemplate() {
        return logoUrlTemplate;
    }

    public void setLogoUrlTemplate(@NotNull final String logoUrlTemplate) {
        this.logoUrlTemplate = logoUrlTemplate;
    }

    public int getMinLogoNumber() {
        return minLogoNumber;
    }

    public void setMinLogoNumber(final int minLogoNumber) {
        this.minLogoNumber = minLogoNumber;
    }

    public int getMaxLogoNumber() {
        return maxLogoNumber;
    }

    public void setMaxLogoNumber(final int maxLogoNumber) {
        this.maxLogoNumber = maxLogoNumber;
    }

}
