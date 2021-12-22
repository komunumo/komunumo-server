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

package org.komunumo.ui.view.website.home;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.NewsService;
import org.komunumo.data.service.SubscriptionService;
import org.komunumo.ui.view.website.NewsBlock;
import org.komunumo.ui.view.website.WebsiteLayout;

@Route(value = "", layout = WebsiteLayout.class)
@PageTitle("Home")
@CssImport("./themes/komunumo/views/website/home-view.css")
@CssImport("./themes/komunumo/views/website/events-view.css")
@AnonymousAllowed
public class HomeView extends Div {

    public HomeView(@NotNull final NewsService newsService,
                    @NotNull final SubscriptionService subscriptionService,
                    @NotNull final EventService eventService) {
        addClassName("home-view");
        add(
                new NewsBlock(newsService, subscriptionService),
                new EventPreviewBlock(eventService)
        );
    }

}
