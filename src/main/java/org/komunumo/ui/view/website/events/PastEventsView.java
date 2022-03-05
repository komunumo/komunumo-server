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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.SubMenu;
import org.komunumo.ui.view.website.SubMenuItem;
import org.komunumo.ui.view.website.WebsiteLayout;

import java.time.Year;
import java.util.Comparator;
import java.util.List;

@Route(value = "events/past", layout = WebsiteLayout.class)
@RouteAlias(value = "events/past/:year", layout = WebsiteLayout.class)
@CssImport("./themes/komunumo/views/website/events-view.css")
@AnonymousAllowed
public final class PastEventsView extends ContentBlock implements BeforeEnterObserver, HasDynamicTitle {

    @Serial
    private static final long serialVersionUID = -5927631850104413849L;
    private final DatabaseService databaseService;

    private Year selectedYear;

    public PastEventsView(@NotNull final DatabaseService databaseService) {
        super("Events");

        this.databaseService = databaseService;
        addClassName("events-view");

        final var upcomingTitle = new H1("Upcoming");
        upcomingTitle.setId("upcoming-title");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var year = params.get("year");
        selectedYear = year.isPresent() ? Year.parse(year.get()) : Year.now();
        final var years = databaseService.getYearsWithPastEvents();
        if (!years.contains(selectedYear) && !years.isEmpty()) {
            beforeEnterEvent.forwardTo(PastEventsView.class, new RouteParameters("year", years.get(0).toString()));
        }

        final var events = databaseService.pastEvents(selectedYear).toList();

        final var subMenu = createSubMenu(years, selectedYear);

        final var eventsList = new Div();
        eventsList.addClassName("events-list");
        events.stream()
                .map(EventPreview::new)
                .forEach(eventsList::add);
        setSubMenu(subMenu);
        setContent(eventsList);
    }

    private static Component createSubMenu(@NotNull final List<Year> years,
                                           @Nullable final Year selectedYear) {
        final var subMenu = new SubMenu();
        subMenu.add(new SubMenuItem("/events", "upcoming"));

        final var pastEvents = new SubMenuItem("/events/past", "Past Events", true);
        pastEvents.addClassName("past-events");
        subMenu.add(pastEvents);

        years.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .map(year -> new SubMenuItem("/events/past/%s".formatted(year), year.toString(), year.equals(selectedYear)))
                .forEach(subMenu::add);
        return subMenu;
    }

    @Override
    public String getPageTitle() {
        return "%s: Past events from %s".formatted(databaseService.configuration().getWebsiteName(), selectedYear);
    }

}
