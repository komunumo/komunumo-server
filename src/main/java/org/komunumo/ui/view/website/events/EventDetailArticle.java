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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.DescriptionList;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.DatabaseService;

import java.time.LocalDateTime;
import java.util.Locale;

public class EventDetailArticle extends EventArticle {

    protected EventDetailArticle() {
        super();
        addClassName("event-details");
    }

    protected void addSpeakerBox(@NotNull final Event event) {
        final var div = new Div();
        div.addClassName("speakerbox");
        for (final var speaker : event.getSpeakers()) {
            if (!speaker.photo().isBlank()) {
                div.add(new Image(speaker.photo(), speaker.fullName()));
            }
            div.add(new Html("<div>%s</div>".formatted(speaker.bio())));
        }
        add(div);
    }

    protected void addLocation(@NotNull final Event event) {
        final var locationLabel = new Span(new Text("Location:"));
        locationLabel.addClassName("location-label");
        final var location = new Div(
                locationLabel,
                new Span(new Text(event.getLocation()))
        );
        location.addClassName("location");
        add(location);
    }

    protected void addRoom(@NotNull final Event event) {
        if (!event.getRoom().isBlank() || !event.getTravelInstructions().isBlank()) {
            final var room = new Div();
            room.addClassName("room");

            final var roomLabel = new Span(new Text("Room:"));
            roomLabel.addClassName("room-label");
            room.add(roomLabel);

            if (!event.getRoom().isBlank()) {
                room.add(new Span(event.getRoom()));
            }

            if (!event.getRoom().isBlank() && !event.getTravelInstructions().isBlank()) {
                room.add(new Text(", "));
            }

            if (!event.getTravelInstructions().isBlank()) {
                room.add(new Anchor(event.getTravelInstructions(), "Travel instructions"));
            }

            add(room);
        }
    }

    protected void addAgenda(@NotNull final Event event) {
        final var agendaLabel = new Span(new Text("Agenda:"));
        agendaLabel.addClassName("agenda-label");
        final var agenda = new Div(
                agendaLabel,
                new Html("<span>%s</span>".formatted(event.getAgenda()))
        );
        agenda.addClassName("agenda");
        add(agenda);
    }

    protected void addBorder() {
        add(new Hr());
    }

    protected void addLevel(@NotNull final Event event) {
        final var levelLabel = new Span(new Text("Level:"));
        levelLabel.addClassName("level-label");
        final var level = new Div(
                levelLabel,
                new Span(new Text(event.getLevel().toString()))
        );
        level.addClassName("level");
        add(level);
    }

    protected void addLanguage(@NotNull final Event event) {
        if (event.getLanguage() != null) {
            final var languageLabel = new Span(new Text("Language:"));
            languageLabel.addClassName("language-label");
            final var language = new Div(
                    languageLabel,
                    new Span(new Text(event.getLanguage().toString().toLowerCase(Locale.getDefault())))
            );
            language.addClassName("language");
            add(language);
        }
    }

    protected void addRegistrationForm(@NotNull final DatabaseService databaseService,
                                       @NotNull final Event event, String deregisterCode) {
        if (event.getDate().isAfter(LocalDateTime.now())) {
            add(new Hr());
            if (deregisterCode.isBlank()) {
                add(new EventRegistrationForm(databaseService, event));
            } else {
                add(new EventDeregistrationForm(databaseService, event, deregisterCode));
            }
        }
    }

    protected void addLevelInfo() {
        final var levelInfo = new DescriptionList();
        levelInfo.add(new DescriptionList.Term("Beginner"));
        levelInfo.add(new DescriptionList.Description("The presented topic is new to the audience or only little and superficial experience exists. This talk will mainly cover basic aspects of the topic and not go into much detail."));
        levelInfo.add(new DescriptionList.Term("Intermediate"));
        levelInfo.add(new DescriptionList.Description("The presented topic is known to the audience, serious practical experience is expected. This talk might cover some basic aspects of the topic, but will as well go into depth and details."));
        levelInfo.add(new DescriptionList.Term("Advanced"));
        levelInfo.add(new DescriptionList.Description("The presented topic is well-known to the audience, serious practical experience and a deep understanding are required. This talk will not cover basics of the topic, but will go into depth, might discuss details, compare different approaches, and so on."));

        add(new Hr(), new H4("Levels"), levelInfo);
    }

    public void addYoutube(@NotNull final Event event) {
        if (!event.getYoutube().isBlank()) {
            add(new Hr(), new Html("""
                    <iframe width="560" height="315"
                            src="https://www.youtube-nocookie.com/embed/%s"
                            title="YouTube video player"
                            frameborder="0"
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen>
                    </iframe>""".formatted(event.getYoutube())));
        }
    }
}
