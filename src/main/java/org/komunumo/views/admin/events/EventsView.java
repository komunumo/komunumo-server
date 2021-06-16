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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.EventGridItem;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.views.admin.AdminView;

import java.time.format.DateTimeFormatter;

@Route(value = "admin/events", layout = AdminView.class)
@PageTitle("Event Administration")
public class EventsView extends Div {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private final TextField filterField;
    private final Grid<EventGridItem> grid;

    public EventsView(@NotNull final EventService eventService,
                      @NotNull final SpeakerService speakerService,
                      @NotNull final EventSpeakerService eventSpeakerService) {
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;

        addClassNames("events-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = createFilter();

        final var newEventButton = new Button(new Icon(VaadinIcon.FILE_ADD), event -> editEvent(null));
        final var refreshEventsButton = new Button(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        final var optionBar = new HorizontalLayout(filterField, newEventButton, refreshEventsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);

        reloadGridItems();
    }

    private TextField createFilter() {
        final var filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.focus();
        filter.addValueChangeListener(event -> reloadGridItems());
        return filter;
    }

    private Grid<EventGridItem> createGrid() {
        final var grid = new Grid<EventGridItem>();
        grid.addColumn(EventGridItem::getTitle).setHeader("Title").setAutoWidth(true);
        grid.addColumn(EventGridItem::getSpeaker).setHeader("Speaker").setAutoWidth(true);

        final var dateRenderer = TemplateRenderer.<EventGridItem>of(
                "[[item.date]]")
                .withProperty("date", record -> {
                    final var date = record.getDate();
                    return date == null ? "" : date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                });
        grid.addColumn(dateRenderer).setHeader("Date").setAutoWidth(true);

        final var visibleRenderer = TemplateRenderer.<EventGridItem>of(
                "<iron-icon hidden='[[!item.visible]]' icon='vaadin:eye' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.visible]]' icon='vaadin:eye-slash' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("visible", EventGridItem::getVisible);
        grid.addColumn(visibleRenderer).setHeader("Visible").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record ->
                new HorizontalLayout(
                        new Button(new Icon(VaadinIcon.EDIT), event -> editEvent(record)),
                        new Button(new Icon(VaadinIcon.TRASH), event -> deleteEvent(record))
                )
            ))
            .setHeader("Actions")
            .setFlexGrow(0)
            .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private void editEvent(@Nullable final EventGridItem record) {
        final var dialog = new EventDialog(record, eventService, speakerService, eventSpeakerService);
        dialog.addDialogCloseActionListener(event -> reloadGridItems());
        dialog.open();
    }

    private void deleteEvent(@NotNull final EventGridItem record) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the event \"%s\"?", record.getTitle()),
                "Delete", (dialogEvent) -> {
            final var eventId = record.getId();
            eventService.deleteEvent(eventId);
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> eventService.eventsForGrid(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
