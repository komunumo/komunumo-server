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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.SubscriptionService;

public class EventDetailArticle extends EventArticle {

    protected EventDetailArticle() {
        super();
        addClassName("event-details");
    }

    protected void addSpeakerBox(@NotNull final Event event) {
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

    protected void addRoom(@NotNull final Event event) { // TODO there is no room property on the event
        final var roomLabel = new Span(new Text("Room:"));
        roomLabel.addClassName("room-label");
        final var room = new Div(
                roomLabel,
                new Html("<span>%s</span>".formatted("Testikowski"))
        );
        room.addClassName("room");
        add(room);
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

    protected void addRegistrationForm(@NotNull final MemberService memberService,
                                       @NotNull final EventMemberService eventMemberService,
                                       @NotNull final SubscriptionService subscriptionService,
                                       @NotNull final Event event, String deregisterCode) {
        add(new Hr());
        if (deregisterCode.isBlank()) {
            add(new EventRegistrationForm(memberService, eventMemberService, subscriptionService, event));
        } else {
            add(new EventDeregistrationForm(eventMemberService, event, deregisterCode));
        }
        add(new Hr());
    }

}
