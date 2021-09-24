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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.EventRecord;

import java.time.format.DateTimeFormatter;

import static org.komunumo.data.db.tables.Event.EVENT;

public class EventPreview extends Div {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventPreview(@NotNull final EventRecord event) {
        add(
                new H2(event.get(EVENT.TITLE)),
                new H3(event.get(EVENT.SUBTITLE)),
                new Div(
                        new Span(new Text(event.get(EVENT.LOCATION))),
                        new Span(new Text(event.get(EVENT.DATE).format(DATE_TIME_FORMATTER)))
                ),
                new Text(event.get(EVENT.DESCRIPTION))
        );
    }
}
