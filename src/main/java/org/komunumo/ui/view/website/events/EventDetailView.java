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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.website.WebsiteLayout;

import java.time.Year;

@Route(value = "event/:location/:year/:url", layout = WebsiteLayout.class)
@PageTitle("Events") // TODO title based on event
@CssImport("./themes/komunumo/views/website/event-details.css")
public class EventDetailView extends EventArticle implements BeforeEnterObserver {

    private final EventService eventService;

    public EventDetailView(@NotNull final EventService eventService) {
        super();
        this.eventService = eventService;
        addClassName("event-details");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var location = params.get("location").orElseThrow(NotFoundException::new);
        final var year = params.getInteger("year").orElseThrow(NotFoundException::new);
        final var url = params.get("url").orElseThrow(NotFoundException::new);

        final var event = eventService.getByEventUrl(location, Year.of(year), url)
                .orElseThrow(NotFoundException::new);

        addHeader(event);
        addTitle(event);
        addSpeakers(event);
        addKeywords(event);
        addDescription(event);
    }

}
