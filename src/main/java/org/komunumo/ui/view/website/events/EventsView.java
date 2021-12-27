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

package org.komunumo.ui.view.website.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.SubMenu;
import org.komunumo.ui.view.website.SubMenuItem;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.util.URLUtil;

import java.util.List;

@Route(value = "events", layout = WebsiteLayout.class)
@RouteAlias(value = "events/:location", layout = WebsiteLayout.class)
@PageTitle("Events")
@CssImport("./themes/komunumo/views/website/events-view.css")
@AnonymousAllowed
public class EventsView extends ContentBlock implements BeforeEnterObserver {

    private final EventService eventService;

    public EventsView(@NotNull final EventService eventService) {
        super("Events");

        this.eventService = eventService;
        addClassName("events-view");

        final var upcomingTitle = new H1("Upcoming");
        upcomingTitle.setId("upcoming-title");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var location = params.get("location");
        final var events = eventService.upcomingEvents().toList();
        final var locationSelector = createLocationSelector(events, location.orElse(null));
        final var eventsList = new Div();
        eventsList.addClassName("events-list");
        events.stream()
                .filter(event -> location.isEmpty() || URLUtil.createReadableUrl(event.getLocation()).equals(location.get()))
                .map(EventPreview::new)
                .forEach(eventsList::add);
        setSubMenu(locationSelector);
        setContent(eventsList);
    }

    private Component createLocationSelector(List<Event> events, @Nullable final String actualLocation) {
        final var locationSelector = new SubMenu();
        locationSelector.add(new SubMenuItem("/events", "upcoming", true));
        locationSelector.add(new SubMenuItem("/events", "all locations", actualLocation == null));
        events.stream()
                .map(Event::getLocation)
                .distinct()
                .sorted()
                .map(location -> {
                    final var url = URLUtil.createReadableUrl(location);
                    return new SubMenuItem("/events/".concat(url), location, url.equals(actualLocation));
                })
                .forEach(locationSelector::add);
        return locationSelector;
    }

}
