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

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.DatabaseService;

@Push
@Theme(value = "komunumo")
@PWA(name = "Komunumo", shortName = "Komunumo", iconPath = "")
@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
@NpmPackage(value = "line-awesome", version = "1.3.0")
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class AppShell implements AppShellConfigurator {

    private final DatabaseService databaseService;

    public AppShell(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void configurePage(@NotNull final AppShellSettings settings) {
        AppShellConfigurator.super.configurePage(settings);
        final var pathInfo = settings.getRequest().getPathInfo();
        if (pathInfo.startsWith("admin") || pathInfo.startsWith("/admin")) {
            settings.addFavIcon("icon", "https://komunumo.org/images/favicon/favicon.ico", "16x16");
            settings.addLink("shortcut icon", "https://komunumo.org/images/favicon/favicon.ico");
        } else {
            settings.addFavIcon("icon", databaseService.configuration().getWebsiteFavicon(), "16x16");
            settings.addLink("shortcut icon", databaseService.configuration().getWebsiteFavicon());
        }
    }

}
