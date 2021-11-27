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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.ui.view.website.events.EventPreview;
import org.komunumo.ui.view.website.events.LocationSelector;

@Route(value = "", layout = WebsiteLayout.class)
@PageTitle("Home")
@CssImport("./themes/komunumo/views/website/events-view.css")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView(@NotNull final EventService eventService) {
        addClassName("home-view");

        final var pageTitle = new H2("Events");
        pageTitle.setId("page-title");

        final var events = eventService.upcomingEvents().toList();
        final var eventLocations = events.stream()
                .map(Event::getLocation)
                .distinct()
                .sorted()
                .toList();
        final var locationSelector = new LocationSelector(eventLocations);

        final var eventsList = new Div();
        eventsList.addClassName("events-list");
        events.stream()
                .map(EventPreview::new)
                .forEach(eventsList::add);

        final var eventsLayout = new HorizontalLayout();
        eventsLayout.add(new Div(pageTitle, locationSelector), eventsList);
        add(eventsLayout);
    }

}
