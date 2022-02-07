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

package org.komunumo.ui.view.website.members;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.SubMenu;
import org.komunumo.ui.view.website.SubMenuItem;
import org.komunumo.ui.view.website.WebsiteLayout;

@Route(value = "members", layout = WebsiteLayout.class)
@CssImport("./themes/komunumo/views/website/members-view.css")
@AnonymousAllowed
public class MembersView extends ContentBlock implements AfterNavigationObserver {

    private final DatabaseService databaseService;

    public MembersView(@NotNull final DatabaseService databaseService) {
        super("Members");
        this.databaseService = databaseService;
        addClassName("members-view");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        final var url = event.getLocation().getPath();
        final var subMenu = new SubMenu();
        databaseService.getPages(PageParent.Members).forEach(page ->
                subMenu.add(new SubMenuItem(page.getCompletePageUrl(), page.getTitle(), url.equals(page.getCompletePageUrl()))));
        setSubMenu(subMenu);
        if (url.contains("/")) {
            final var page = loadPage(databaseService, url);
            this.getUI().ifPresent(ui -> ui.getPage().setTitle("%s: %s"
                    .formatted(databaseService.configuration().getWebsiteName(), page != null ? page.getTitle() : "Members")));
        }
    }

}
