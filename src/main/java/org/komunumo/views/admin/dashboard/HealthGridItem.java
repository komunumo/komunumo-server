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

package org.komunumo.views.admin.dashboard;

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;

/**
 * Simple DTO class for the inbox list to demonstrate complex object data
 */
public class HealthGridItem {

    private LocalDate date;
    private String city;
    private String country;
    private String status;
    private String theme;

    public HealthGridItem(@NotNull final LocalDate date, @NotNull final String city, @NotNull final String country,
                          @NotNull final String status, @NotNull final String theme) {
        this.date = date;
        this.city = city;
        this.country = country;
        this.status = status;
        this.theme = theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NotNull final LocalDate date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(@NotNull final String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(@NotNull final String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(@NotNull final String status) {
        this.status = status;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(@NotNull final String theme) {
        this.theme = theme;
    }
}
