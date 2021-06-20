package org.komunumo.ui.view.website.home.component;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;

import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.EventRecord;

import static org.komunumo.data.db.tables.Event.EVENT;

public class EventPreview extends Div {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventPreview(@NotNull final EventRecord event) {
        add(
                new H2(event.get(EVENT.TITLE)),
                new H3(event.get(EVENT.SUBTITLE)),
                new Div(
                        new Span(new Text(event.get(EVENT.LOCATION).toString())),
                        new Span(new Text(event.get(EVENT.DATE).format(DATE_TIME_FORMATTER)))
                ),
                new Text(event.get(EVENT.ABSTRACT))
        );
    }
}
