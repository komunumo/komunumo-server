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

package org.komunumo.ui.view.admin.events;

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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.view.admin.AdminView;

import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "admin/events", layout = AdminView.class)
@PageTitle("Event Administration")
public class EventsView extends Div implements HasUrlParameter<String> {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private final TextField filterField;
    private final Grid<EventRecord> grid;

    public EventsView(@NotNull final EventService eventService,
                      @NotNull final SpeakerService speakerService,
                      @NotNull final EventSpeakerService eventSpeakerService) {
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;

        addClassNames("events-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());

        final var newEventButton = new Button(new Icon(VaadinIcon.FILE_ADD), event -> editEvent(eventService.newEvent()));
        final var refreshEventsButton = new Button(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        final var optionBar = new HorizontalLayout(filterField, newEventButton, refreshEventsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    @Override
    public void setParameter(@NotNull final BeforeEvent event,
                             @Nullable @OptionalParameter String parameter) {
        final var location = event.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private Grid<EventRecord> createGrid() {
        final var grid = new Grid<EventRecord>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(TemplateRenderer.<EventRecord>of("<span style=\"font-weight: bold;\">[[item.title]]</span>")
                .withProperty("title", EventRecord::getTitle))
                .setHeader("Title").setAutoWidth(true);

        grid.addColumn(TemplateRenderer.<EventRecord>of("<span inner-h-t-m-l=\"[[item.speaker]]\"></span>")
                .withProperty("speaker", this::renderSpeakerLinks))
                .setHeader("Speaker").setAutoWidth(true);

        final var dateRenderer = TemplateRenderer.<EventRecord>of(
                "[[item.date]]")
                .withProperty("date", record -> {
                    final var date = record.getDate();
                    return date == null ? "" : date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                });
        grid.addColumn(dateRenderer).setHeader("Date").setAutoWidth(true);

        final var visibleRenderer = TemplateRenderer.<EventRecord>of(
                "<iron-icon hidden='[[!item.visible]]' icon='vaadin:eye' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.visible]]' icon='vaadin:eye-slash' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("visible", EventRecord::getVisible);
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

    private String renderSpeakerLinks(@NotNull EventRecord event) {
        final var speaker = event.getSpeaker();
        if (speaker == null || speaker.isBlank()) {
            return "";
        }
        return Arrays.stream(speaker.split(","))
                .map(String::trim)
                .map(s -> String.format("<a href=\"/admin/speakers?filter=%s\">%s</a>", URLEncoder.encode(s, UTF_8), s))
                .collect(Collectors.joining(", "));
    }

    private void editEvent(@NotNull final EventRecord event) {
        final var dialog = new EventDialog(event, eventService, speakerService, eventSpeakerService);
        dialog.addOpenedChangeListener(changeEvent -> { if (!changeEvent.isOpened()) { reloadGridItems(); } } );
        dialog.open();
    }

    private void deleteEvent(@NotNull final EventRecord event) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the event \"%s\"?", event.getTitle()),
                "Delete", (dialogEvent) -> {
                    eventService.deleteEvent(event);
                    reloadGridItems();
                    dialogEvent.getSource().close();
                },
                "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> eventService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}