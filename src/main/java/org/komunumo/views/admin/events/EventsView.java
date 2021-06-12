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

package org.komunumo.views.admin.events;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jooq.Record5;
import org.komunumo.data.service.EventService;
import org.komunumo.views.admin.AdminView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.komunumo.data.db.tables.Event.EVENT;

@Route(value = "admin/events", layout = AdminView.class)
@PageTitle("Event Administration")
public class EventsView extends Div {

    private final EventService eventService;

    public EventsView(@Autowired final EventService eventService) {
        this.eventService = eventService;
        addClassNames("events-view", "flex", "flex-col", "h-full");

        final var grid = createGrid();
        final var filter = createFilter(grid);
        final var newEventButton = createNewEventButton();

        final var optionBar = new HorizontalLayout(filter, newEventButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
    }

    private TextField createFilter(final Grid<Record5<Long, String, String, LocalDateTime, Boolean>> grid) {
        final var filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.focus();
        filter.addValueChangeListener(event -> grid.setItems(query -> eventService.eventsWithSpeakers(query.getOffset(), query.getLimit(), filter.getValue())));
        return filter;
    }

    private Grid<Record5<Long, String, String, LocalDateTime, Boolean>> createGrid() {
        final var grid = new Grid<Record5<Long, String, String, LocalDateTime, Boolean>>();
        grid.addColumn(record -> record.get(EVENT.TITLE)).setAutoWidth(true).setHeader("Title");
        grid.addColumn(record -> record.get("speaker")).setAutoWidth(true).setHeader("Speaker");
        grid.addColumn(record -> record.get(EVENT.DATE)).setAutoWidth(true).setHeader("Date");
        final var visibleRenderer = TemplateRenderer.<Record5<Long, String, String, LocalDateTime, Boolean>>of(
                "<iron-icon hidden='[[!item.visible]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.visible]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("visible", record -> record.get(EVENT.VISIBLE));
        grid.addColumn(visibleRenderer).setHeader("Visible").setAutoWidth(true);

        grid.setItems(query -> eventService.eventsWithSpeakers(query.getOffset(), query.getLimit(), null));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private Button createNewEventButton() {
        final var button = new Button("New Event");
        button.addClickListener(event -> Notification.show("Sorry, this feature is not available yet! Please come back later…"));
        return button;
    }
}
