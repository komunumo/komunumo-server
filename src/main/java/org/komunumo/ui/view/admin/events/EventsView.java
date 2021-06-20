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

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
import org.jooq.Record;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.view.admin.AdminView;

import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.data.db.tables.Event.EVENT;

@Route(value = "admin/events", layout = AdminView.class)
@PageTitle("Event Administration")
public class EventsView extends Div implements HasUrlParameter<String> {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private final TextField filterField;
    private final Grid<Record> grid;

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
        filterField.setTitle("Filter events by title, subtitle, or speaker");

        final var newEventButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> newEvent());
        newEventButton.setTitle("Add a new event");
        final var refreshEventsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshEventsButton.setTitle("Refresh the list of events");
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

    private Grid<Record> createGrid() {
        final var grid = new Grid<Record>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(TemplateRenderer.<Record>of("<span style=\"font-weight: bold;\">[[item.title]]</span><br/><span>[[item.subtitle]]</span>")
                .withProperty("title", record -> record.get(EVENT.TITLE))
                .withProperty("subtitle", record -> record.get(EVENT.SUBTITLE)))
                .setHeader("Title").setAutoWidth(true);

        grid.addColumn(TemplateRenderer.<Record>of("<span inner-h-t-m-l=\"[[item.speaker]]\"></span>")
                .withProperty("speaker", this::renderSpeakerLinks))
                .setHeader("Speaker").setAutoWidth(true);

        final var dateRenderer = TemplateRenderer.<Record>of(
                "[[item.date]]")
                .withProperty("date", record -> {
                    final var date = record.get(EVENT.DATE);
                    return date == null ? "" : date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                });
        grid.addColumn(dateRenderer).setHeader("Date").setAutoWidth(true);

        final var visibleRenderer = TemplateRenderer.<Record>of(
                "<iron-icon hidden='[[!item.visible]]' icon='vaadin:eye' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.visible]]' icon='vaadin:eye-slash' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("visible", record -> record.get(EVENT.VISIBLE));
        grid.addColumn(visibleRenderer).setHeader("Visible").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), event -> editEvent(record.get(EVENT.ID)));
            editButton.setTitle("Edit this event");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), event -> deleteEvent(record.get(EVENT.ID)));
            deleteButton.setTitle("Delete this event");
            return new HorizontalLayout(editButton, deleteButton);

        }))
            .setHeader("Actions")
            .setFlexGrow(0)
            .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private String renderSpeakerLinks(@NotNull Record record) {
        final var speaker = record.get("speaker", String.class);
        if (speaker == null || speaker.isBlank()) {
            return "";
        }
        return Arrays.stream(speaker.split(","))
                .map(String::trim)
                .map(s -> String.format("<a href=\"/admin/speakers?filter=%s\">%s</a>", URLEncoder.encode(s, UTF_8), s))
                .collect(Collectors.joining(", "));
    }

    private void newEvent() {
        showEventDialog(eventService.newEvent());
    }

    private void editEvent(@NotNull final Long eventId) {
        final var event = eventService.get(eventId);
        if (event.isPresent()) {
            showEventDialog(event.get());
        } else {
            Notification.show("This event does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void showEventDialog(@NotNull final EventRecord event) {
        final var dialog = new EventDialog(event, eventService, speakerService, eventSpeakerService);
        dialog.addOpenedChangeListener(changeEvent -> {
            if (!changeEvent.isOpened()) { reloadGridItems(); }
        });
        dialog.open();
    }

    private void deleteEvent(@NotNull final Long eventId) {
        final var event = eventService.get(eventId);
        if (event.isPresent()) {
            new ConfirmDialog("Confirm deletion",
                    String.format("Are you sure you want to permanently delete the event \"%s\"?", event.get().getTitle()),
                    "Delete", (dialogEvent) -> {
                eventService.deleteEvent(event.get());
                reloadGridItems();
                dialogEvent.getSource().close();
            },
                    "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
            ).open();
        } else {
            Notification.show("This event does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void reloadGridItems() {
        grid.setItems(query -> eventService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
