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

package org.komunumo.ui.view.website.home.component;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Keyword;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@CssImport("./themes/komunumo/views/website/event-preview.css")
public class EventPreview extends Article {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventPreview(@NotNull final Event event) {
        final var upcoming = new Span(new Text("upcoming"));
        upcoming.addClassName("upcoming");

        final var location = new Span(new Text(event.getLocation()));
        location.addClassName("location");

        final var dateTime = new Span(new Text(event.getDate().format(DATE_TIME_FORMATTER).concat("h")));
        dateTime.addClassName("date-time");

        final var speakers = new Div();
        speakers.addClassName("speakers");
        final var speakerCount = event.getSpeakers().size();
        final var speakerCounter = new AtomicInteger(0);
        event.getSpeakers().forEach(speaker -> {
            final var counter = speakerCount == 1 ? "" : String.format(" %d", speakerCounter.incrementAndGet());
            final var speakerLabel = new Span(new Text(String.format("Speaker%s:", counter)));
            speakerLabel.addClassName("speaker-label");
            final var speakerName = new Span(new Text(speaker.getFullName()));
            final var companyLabel = new Span(new Text("Company:"));
            companyLabel.addClassName("company-label");
            final var companyName = new Span(new Text(speaker.getCompany()));
            speakers.add(new Div(
                    speakerLabel, speakerName,
                    companyLabel, companyName
            ));
        });

        final var keywordLabel = new Span(new Text("Keywords:"));
        keywordLabel.addClassName("keyword-label");
        final var keywordList = new Span(new Text(
                event.getKeywords().stream()
                        .map(Keyword::getKeyword)
                        .collect(Collectors.joining(", "))));
        final var keywords = new Div(
                keywordLabel,
                keywordList
        );
        keywords.addClassName("keywords");

        add(new Div(upcoming, location, dateTime));
        add(new H2(event.getTitle()));
        if (!event.getSubtitle().isBlank()) {
            add(new H3(event.getSubtitle()));
        }
        add(speakers);
        if (!event.getKeywords().isEmpty()) {
            add(keywords);
        }
        add(getDescription(event));
    }

    private Div getDescription(@NotNull final Event event) {
        final var description = event.getDescription();
        final var paragraphEnd = description.contains("</p>") ? description.indexOf("</p>") : description.indexOf("</P>");
        final var html = paragraphEnd > 0 ? description.substring(0, paragraphEnd + 4) : description;
        final var more = new Paragraph(
                new Image("/images/more.gif", "more"),
                new Anchor("javascript:alert('TODO: Implement this link!');", "more")
        );
        more.addClassName("more");
        return new Div(new Html(html), more);
    }
}
