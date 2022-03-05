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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.SubMenu;
import org.komunumo.ui.view.website.SubMenuItem;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.util.URLUtil;

import java.util.List;

@Route(value = "events", layout = WebsiteLayout.class)
@RouteAlias(value = "events/:location", layout = WebsiteLayout.class)
@CssImport("./themes/komunumo/views/website/events-view.css")
@AnonymousAllowed
public final class EventsView extends ContentBlock implements BeforeEnterObserver, HasDynamicTitle {

    @Serial
    private static final long serialVersionUID = -1477933866218710123L;
    private final DatabaseService databaseService;

    private String selectedLocation;

    public EventsView(@NotNull final DatabaseService databaseService) {
        super("Events");

        this.databaseService = databaseService;
        addClassName("events-view");

        final var upcomingTitle = new H1("Upcoming");
        upcomingTitle.setId("upcoming-title");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var location = params.get("location");
        final var events = databaseService.upcomingEvents().toList();
        final var subMenu = createSubMenu(events, location.orElse(null));
        final var eventsList = new Div();
        eventsList.addClassName("events-list");
        final var filteredEventPreviews = events.stream()
                .filter(event -> location.isEmpty() || URLUtil.createReadableUrl(event.getLocation()).equals(location.get()))
                .map(EventPreview::new)
                .toList();
        if (filteredEventPreviews.isEmpty()) {
            eventsList.add(new H2("No upcoming events found"));
            eventsList.add(new H3("Please try a different location from the menu to the left."));
        } else {
            eventsList.add(filteredEventPreviews.toArray(new EventPreview[0]));
        }
        setSubMenu(subMenu);
        setContent(eventsList);
    }

    private Component createSubMenu(@NotNull final List<Event> events,
                                    @Nullable final String actualLocation) {
        final var subMenu = new SubMenu();
        subMenu.add(new SubMenuItem("/events", "upcoming", true));
        subMenu.add(new SubMenuItem("/events", "all locations", actualLocation == null));
        events.stream()
                .map(Event::getLocation)
                .distinct()
                .sorted()
                .map(location -> {
                    final var url = URLUtil.createReadableUrl(location);
                    if (url.equals(actualLocation)) {
                        selectedLocation = location;
                    }
                    return new SubMenuItem("/events/".concat(url), location, url.equals(actualLocation));
                })
                .forEach(subMenu::add);
        final var pastEvents = new SubMenuItem("/events/past", "Past Events");
        pastEvents.addClassName("past-events");
        subMenu.add(pastEvents);
        return subMenu;
    }

    @Override
    public String getPageTitle() {
        var title = "Events";
        if (selectedLocation != null) {
            if (selectedLocation.equals("Online")) {
                title = "Online events";
            } else {
                title = "Events in %s".formatted(selectedLocation);
            }
        }
        return "%s: %s".formatted(databaseService.configuration().getWebsiteName(), title);
    }

}
