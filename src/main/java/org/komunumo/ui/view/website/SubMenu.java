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

import java.io.Serial;

import org.jetbrains.annotations.NotNull;

@CssImport("./themes/komunumo/views/website/sub-menu.css")
public class SubMenu extends Nav {

    @Serial
    private static final long serialVersionUID = -1625103028098617871L;

    public SubMenu() {
        addClassName("sub-menu");
    }

    public SubMenu(@NotNull final Component... components) {
        this();
        add(components);
    }
}
