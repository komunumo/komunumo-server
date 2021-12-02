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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.website.WebsiteLayout;

import java.time.Year;

@Route(value = "event/:location/:year/:url", layout = WebsiteLayout.class)
@PageTitle("Events") // TODO title based on event
@CssImport("./themes/komunumo/views/website/event-details.css")
@AnonymousAllowed
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
        final var previewCode = getPreviewCode(beforeEnterEvent);

        final var event = eventService.getByEventUrl(location, Year.of(year), url)
                .orElseThrow(NotFoundException::new);

        if (!previewCode.isBlank() && event.getPublished()) {
            beforeEnterEvent.forwardTo(event.getCompleteEventUrl()); // TODO "301 Moved Permanently"
        }

        if (!event.getPublished() && !previewCode.equals(event.getEventPreviewCode())) {
            throw new NotFoundException();
        }

        addSpeakerBox(event);
        addHeader(event, false);
        addTitle(event);
        addLocation(event);
        addRoom(event);
        addKeywords(event);
        addAgenda(event);
        addBorder();
        addSpeakers(event);
        addDescription(event);
        addLevel(event);
        addLanguage(event);
    }

    private String getPreviewCode(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getLocation().getQueryParameters().getParameters();
        final var preview = params.getOrDefault("preview", null);
        return preview != null ? preview.get(0) : "";
    }

    private void addSpeakerBox(@NotNull final Event event) {
        final var div = new Div();
        div.addClassName("speakerbox");
        for (final var speaker : event.getSpeakers()) {
            div.add(new Div(
                    new Image(speaker.photo(), speaker.fullName()),
                    new Html("<div>%s</div>".formatted(speaker.bio()))
            ));
        }
        add(div);
    }

    private void addLocation(@NotNull final Event event) {
        final var locationLabel = new Span(new Text("Location:"));
        locationLabel.addClassName("location-label");
        final var location = new Div(
                locationLabel,
                new Span(new Text(event.getLocation()))
        );
        location.addClassName("location");
        add(location);
    }

    private void addRoom(@NotNull final Event event) { // TODO there is no room property on the event
        final var roomLabel = new Span(new Text("Room:"));
        roomLabel.addClassName("room-label");
        final var room = new Div(
                roomLabel,
                new Html("<span>%s</span>".formatted("Testikowski"))
        );
        room.addClassName("room");
        add(room);
    }

    private void addAgenda(@NotNull final Event event) {
        final var agendaLabel = new Span(new Text("Agenda:"));
        agendaLabel.addClassName("agenda-label");
        final var agenda = new Div(
                agendaLabel,
                new Html("<span>%s</span>".formatted(event.getAgenda()))
        );
        agenda.addClassName("agenda");
        add(agenda);
    }

    private void addBorder() {
        add(new Hr());
    }

    private void addLevel(@NotNull final Event event) {
        final var levelLabel = new Span(new Text("Level:"));
        levelLabel.addClassName("level-label");
        final var level = new Div(
                levelLabel,
                new Span(new Text(event.getLevel().toString()))
        );
        level.addClassName("level");
        add(level);
    }

    private void addLanguage(@NotNull final Event event) {
        final var languageLabel = new Span(new Text("Language:"));
        languageLabel.addClassName("language-label");
        final var language = new Div(
                languageLabel,
                new Span(new Text(event.getLanguage().toString()))
        );
        language.addClassName("language");
        add(language);
    }

}
