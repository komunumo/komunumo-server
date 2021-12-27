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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Nav;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.util.URLUtil;

import java.util.List;

@CssImport("./themes/komunumo/views/website/sub-menu.css")
public class SubMenu extends Nav {

    public SubMenu() {
        addClassName("sub-menu");
    }

    public SubMenu(@NotNull final Component... components) {
        this();
        add(components);
    }

    public SubMenu(@NotNull final List<String> locations, @Nullable final String actualLocation, final boolean onlyLocations) {
        this();

        if (!onlyLocations) {
            add(new SubMenuItem("/events", "upcoming", true));
            add(new SubMenuItem("/events", "all locations", actualLocation == null));
        }

        locations.stream()
                .map(location -> {
                    final var url = URLUtil.createReadableUrl(location);
                    return new SubMenuItem("/events/".concat(url), location, url.equals(actualLocation));
                })
                .forEach(this::add);
    }

}
