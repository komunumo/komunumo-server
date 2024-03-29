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

package org.komunumo.ui.view.admin.settings;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.ApplicationServiceInitListener;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Route(value = "admin/settings", layout = AdminLayout.class)
@RouteAlias(value = "admin/settings/:id", layout = AdminLayout.class)
@PageTitle("Komunumo Settings")
@CssImport(value = "./themes/komunumo/views/admin/settings-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public final class SettingsView extends ResizableView implements BeforeEnterObserver {

    private static final String ANCHOR_PREFIX = "admin/settings/";
    @Serial
    private static final long serialVersionUID = -2617826959693372843L;

    private final DatabaseService databaseService;
    private final ApplicationServiceInitListener applicationServiceInitListener;

    private final List<Tab> settingTabs;
    private final Div content;
    private final Tabs tabs;

    public SettingsView(@NotNull final DatabaseService databaseService,
                        @NotNull final ApplicationServiceInitListener applicationServiceInitListener) {
        this.databaseService = databaseService;
        this.applicationServiceInitListener = applicationServiceInitListener;
        addClassNames("settings-view", "flex", "flex-col", "h-full");
        settingTabs = new ArrayList<>();

        content = new Div();
        content.addClassName("tab-content");

        final var configuration = new Tab(new Anchor(ANCHOR_PREFIX + "configuration", "Configuration"));
        configuration.setId("configuration");
        settingTabs.add(configuration);

        final var locationColors = new Tab(new Anchor(ANCHOR_PREFIX + "location-colors", "Location colors"));
        locationColors.setId("location-colors");
        settingTabs.add(locationColors);

        final var mailTemplates = new Tab(new Anchor(ANCHOR_PREFIX + "mail-templates", "Mail templates"));
        mailTemplates.setId("mail-templates");
        settingTabs.add(mailTemplates);

        final var redirects = new Tab(new Anchor(ANCHOR_PREFIX + "redirects", "Redirects"));
        redirects.setId("redirects");
        settingTabs.add(redirects);

        tabs = new Tabs(settingTabs.toArray(new Tab[0]));

        add(tabs, content);
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var tabId = params.get("id").orElse("");
        final var tabToSelect = getTab(tabId);
        tabs.setSelectedTab(tabToSelect);
        setContent(tabToSelect);
    }

    private Tab getTab(@NotNull final String tabId) {
        return settingTabs.stream()
                .filter(tab -> tabId.equals(tab.getId().orElse("")))
                .findFirst()
                .orElse(settingTabs.get(0));
    }

    private void setContent(@NotNull final Tab tab) {
        content.removeAll();
        final var tabId = tab.getId().orElse("");
        final var tabContent = switch (tabId) {
            case "configuration" -> new ConfigurationSetting(databaseService);
            case "mail-templates" -> new MailTemplateSetting(databaseService);
            case "location-colors" -> new LocationColorSetting(databaseService);
            case "redirects" -> new RedirectSetting(databaseService, applicationServiceInitListener);
            default -> new Paragraph("This setting has not been implemented yet!");
        };
        content.add(tabContent);
    }

}
