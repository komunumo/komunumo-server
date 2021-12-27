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

package org.komunumo.ui.view.website;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Nav;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.util.URLUtil;

import java.util.List;

@CssImport("./themes/komunumo/views/website/sub-menu.css")
public class SubMenu extends Nav {

    private final String actualLocation;

    public SubMenu(@NotNull final List<String> locations) {
        this(locations, null, true);
    }

    public SubMenu(@NotNull final List<String> locations, @Nullable final String actualLocation) {
        this(locations, actualLocation, false);
    }

    public SubMenu(@NotNull final List<String> locations, @Nullable final String actualLocation, final boolean onlyLocations) {
        this.actualLocation = actualLocation;
        addClassName("sub-menu");

        if (!onlyLocations) {
            final var upcoming = new SubMenuItem("/events", "upcoming");
            upcoming.addClassName("active");
            add(upcoming);

            final var allCities = new SubMenuItem("/events", "all locations");
            if (actualLocation == null) {
                allCities.addClassName("active");
            }
            add(allCities);
        }

        locations.stream()
                .map(this::toListItem)
                .forEach(this::add);
    }

    private SubMenuItem toListItem(@NotNull final String location) {
        final var url = URLUtil.createReadableUrl(location);
        final var item = new SubMenuItem("/events/".concat(url), location);
        if (actualLocation != null && actualLocation.equals(url)) {
            item.addClassName("active");
        }
        return item;
    }
}
