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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.KeywordEntity;
import org.komunumo.ui.component.More;

import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@CssImport("./themes/komunumo/views/website/event-article.css")
public class EventArticle extends Article {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @Serial
    private static final long serialVersionUID = -6558638652341245266L;

    public EventArticle() {
        addClassName("event-article");
    }

    /**
     * Add the event header.
     * @param event the event
     * @param withLocation include location
     */
    protected void addHeader(@NotNull final Event event, final boolean withLocation) {
        final var upcoming = new Span(new Text("upcoming"));
        upcoming.addClassName("upcoming");

        final var dateTime = new Span(new Text(event.getDate().format(DATE_TIME_FORMATTER).concat("h")));
        dateTime.addClassName("date-time");

        if (withLocation) {
            final var location = new Span(new Text(event.getLocation()));
            location.addClassName("location");
            add(new Div(upcoming, location, dateTime));
        } else {
            add(new Div(upcoming, dateTime));
        }
    }

    /**
     * Add the event title.
     * @param event the event
     */
    protected void addTitle(@NotNull final Event event) {
        final var detailLink = event.getCompleteEventUrl();
        add(new Anchor(detailLink, new H2(event.getTitle())));
        if (!event.getSubtitle().isBlank()) {
            add(new Anchor(detailLink, new H3(event.getSubtitle())));
        }
    }

    /**
     * Add the event speakers.
     * @param event the event
     */
    protected void addSpeakers(@NotNull final Event event) {
        final var speakers = new Div();
        speakers.addClassName("speakers");
        final var speakerCount = event.getSpeakers().size();
        final var speakerCounter = new AtomicInteger(0);
        event.getSpeakers().forEach(eventSpeakerEntity -> {
            final var counter = speakerCount == 1 ? "" : String.format(" %d", speakerCounter.incrementAndGet());
            final var speakerLabel = new Span(new Text(String.format("Speaker%s:", counter)));
            speakerLabel.addClassName("speaker-label");
            final var speakerName = new Span(new Text(eventSpeakerEntity.fullName()));
            final var companyLabel = new Span(new Text("Company:"));
            companyLabel.addClassName("company-label");
            final var companyName = new Span(new Text(eventSpeakerEntity.company()));
            speakers.add(new Div(
                    speakerLabel, speakerName,
                    companyLabel, companyName
            ));
        });

        add(speakers);
    }

    /**
     * Add the event keywords.
     * @param event the event
     */
    protected void addKeywords(@NotNull final Event event) {
        if (!event.getKeywords().isEmpty()) {
            final var keywordLabel = new Span(new Text("Keywords:"));
            keywordLabel.addClassName("keyword-label");
            final var keywordList = new Span(new Text(
                    event.getKeywords().stream()
                            .map(KeywordEntity::keyword)
                            .collect(Collectors.joining(", "))));
            final var keywords = new Div(
                    keywordLabel,
                    keywordList
            );
            keywords.addClassName("keywords");
            add(keywords);
        }
    }

    /**
     * Add a teaser of the event description.
     * @param event the event
     */
    protected void addDescriptionTeaser(@NotNull final Event event) {
        final var description = event.getDescription();
        final var paragraphEnd = description.contains("</p>") ? description.indexOf("</p>") : description.indexOf("</P>");
        final var html = paragraphEnd > 0 ? description.substring(0, paragraphEnd + 4) : description;
        final var more = new More(event.getCompleteEventUrl());
        add(new Div(new Html("<div>%s</div>".formatted(html)), more));
    }

    /**
     * Add the event description.
     * @param event the event
     */
    protected void addDescription(@NotNull final Event event) {
        add(new Html("<div>%s</div>".formatted(event.getDescription())));
    }

}
