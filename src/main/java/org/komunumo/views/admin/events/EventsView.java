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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jooq.Record5;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.views.admin.AdminView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.komunumo.data.db.tables.Event.EVENT;

@Route(value = "admin/events", layout = AdminView.class)
@PageTitle("Event Administration")
public class EventsView extends Div {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private final TextField filterField;
    private final Grid<Record5<Long, String, String, LocalDateTime, Boolean>> grid;

    public EventsView(@Autowired final EventService eventService,
                      @Autowired final SpeakerService speakerService,
                      @Autowired final EventSpeakerService eventSpeakerService) {
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;

        addClassNames("events-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = createFilter();
        final var newEventButton = new Button(new Icon(VaadinIcon.FILE_ADD), event -> showNewEventDialog());

        final var optionBar = new HorizontalLayout(filterField, newEventButton);
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

    private Grid<Record5<Long, String, String, LocalDateTime, Boolean>> createGrid() {
        final var grid = new Grid<Record5<Long, String, String, LocalDateTime, Boolean>>();
        grid.addColumn(record -> record.get(EVENT.TITLE)).setHeader("Title").setAutoWidth(true);
        grid.addColumn(record -> record.get("speaker")).setHeader("Speaker").setAutoWidth(true);
        final var dateRenderer = TemplateRenderer.<Record5<Long, String, String, LocalDateTime, Boolean>>of(
                "[[item.date]]")
                .withProperty("date", record -> {
                    final var date = record.get(EVENT.DATE);
                    return date == null ? "" : date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                });
        grid.addColumn(dateRenderer).setHeader("Date").setAutoWidth(true);
        final var visibleRenderer = TemplateRenderer.<Record5<Long, String, String, LocalDateTime, Boolean>>of(
                "<iron-icon hidden='[[!item.visible]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.visible]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("visible", record -> record.get(EVENT.VISIBLE));
        grid.addColumn(visibleRenderer).setHeader("Visible").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record -> new Button(new Icon(VaadinIcon.TRASH), event ->
                new ConfirmDialog("Confirm deletion",
                    String.format("Are you sure you want to permanently delete the event \"%s\"?", record.get(EVENT.TITLE)),
                    "Delete", (dialogEvent) -> {
                        final var eventId = record.get(EVENT.ID);
                        eventSpeakerService.deleteEventSpeakers(eventId);
                        eventService.deleteEvent(eventId);
                        reloadGridItems();
                        dialogEvent.getSource().close();
                    },
                    "Cancel", (dialogEvent) -> dialogEvent.getSource().close())
                .open())))
            .setHeader("Actions")
            .setFlexGrow(0)
            .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private void showNewEventDialog() {
        final var dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        final var title = new H2("New Event");
        title.getStyle().set("margin-top", "0");

        final var titleField = new TextField("Title");
        final var speakerField = new MultiselectComboBox<SpeakerRecord>("Speaker");
        speakerField.setOrdered(true);
        speakerField.setItemLabelGenerator(speakerRecord -> String.format("%s %s",
                speakerRecord.getFirstName(), speakerRecord.getLastName()));
        speakerField.setItems(speakerService.list(0, Integer.MAX_VALUE));
        final var dateField = new DateTimePicker("Date");
        final var visibleField = new Checkbox("Visible");

        final var form = new FormLayout();
        form.add(titleField, speakerField, dateField, visibleField);

        final var saveButton = new Button("Save");
        saveButton.setDisableOnClick(true);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            if (titleField.getValue().isBlank()) {
                Notification.show("Please enter at least the title!");
                saveButton.setEnabled(true);
            } else {
                final var newEvent = eventService.newRecord();
                newEvent.setTitle(titleField.getValue());
                newEvent.setDate(dateField.getValue());
                newEvent.setVisible(visibleField.getValue());
                newEvent.store();

                speakerField.getValue().forEach(speakerRecord -> {
                    if (eventSpeakerService.get(newEvent.getId(), speakerRecord.getId()).isEmpty()) {
                        final var eventSpeaker = eventSpeakerService.newRecord();
                        eventSpeaker.setEventId(newEvent.getId());
                        eventSpeaker.setSpeakerId(speakerRecord.getId());
                        eventSpeaker.store();
                    }
                });

                Notification.show("Event saved.");
                reloadGridItems();
                dialog.close();
            }
        });
        saveButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        final var cancelButton = new Button("Cancel", event -> dialog.close());
        final var buttonBar = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(title, form, buttonBar);

        dialog.open();
        titleField.focus();
    }

    private void reloadGridItems() {
        grid.setItems(query -> eventService.eventsWithSpeakers(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
