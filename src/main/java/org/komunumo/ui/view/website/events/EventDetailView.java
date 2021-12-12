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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.Year;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.util.URLUtil;

@Route(value = "event/:location/:year/:url", layout = WebsiteLayout.class)
@PageTitle("Events") // TODO title based on event
@CssImport("./themes/komunumo/views/website/event-details.css")
@AnonymousAllowed
public class EventDetailView extends ContentBlock implements BeforeEnterObserver {

    private final EventService eventService;

    private final Map<String, String> locationMapper = new HashMap<>();

    public EventDetailView(@NotNull final EventService eventService) {
        super("Events");
        this.eventService = eventService;
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var location = params.get("location").orElseThrow(NotFoundException::new);
        final var year = params.getInteger("year").orElseThrow(NotFoundException::new);
        final var url = params.get("url").orElseThrow(NotFoundException::new);
        final var previewCode = getPreviewCode(beforeEnterEvent);

        final var event = eventService.getByEventUrl(mapLocation(location), Year.of(year), url)
                .orElseThrow(NotFoundException::new);

        if (!previewCode.isBlank() && event.getPublished()) {
            beforeEnterEvent.forwardTo(event.getCompleteEventUrl());
        }

        if (!event.getPublished() && !previewCode.equals(event.getEventPreviewCode())) {
            throw new NotFoundException();
        }

        final var article = new EventDetailArticle();
        article.addSpeakerBox(event);
        article.addHeader(event, false);
        article.addTitle(event);
        article.addLocation(event);
        article.addRoom(event);
        article.addKeywords(event);
        article.addAgenda(event);
        article.addBorder();
        article.addSpeakers(event);
        article.addDescription(event);
        article.addLevel(event);
        article.addLanguage(event);
        setContent(article);

        final var eventsOverview = new UnorderedList(new ListItem(new Anchor("/events", "Events overview")));
        eventsOverview.addClassName("location-selector");
        setSubMenu(eventsOverview);
    }

    private String mapLocation(@NotNull final String location) {
        if (!locationMapper.containsKey(location)) {
            eventService.getAllLocations()
                    .forEach(value -> locationMapper.put(URLUtil.createReadableUrl(value), value));
        }
        return locationMapper.getOrDefault(location, location);
    }

    private String getPreviewCode(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getLocation().getQueryParameters().getParameters();
        final var preview = params.getOrDefault("preview", null);
        return preview != null ? preview.get(0) : "";
    }

}
