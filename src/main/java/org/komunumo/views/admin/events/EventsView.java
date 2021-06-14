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

import com.vaadin.componentfactory.EnhancedDatePicker;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.EventLocation;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.entity.EventGridItem;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.views.admin.AdminView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Route(value = "admin/events", layout = AdminView.class)
@PageTitle("Event Administration")
public class EventsView extends Div {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private final TextField filterField;
    private final Grid<EventGridItem> grid;

    public EventsView(@Autowired final EventService eventService,
                      @Autowired final SpeakerService speakerService,
                      @Autowired final EventSpeakerService eventSpeakerService) {
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;

        addClassNames("events-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = createFilter();
        final var newEventButton = new Button(new Icon(VaadinIcon.FILE_ADD), event -> showEventDialog(null));

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
                        new Button(new Icon(VaadinIcon.EDIT), event -> showEventDialog(record)),
                        new Button(new Icon(VaadinIcon.TRASH), event ->
                                new ConfirmDialog("Confirm deletion",
                                        String.format("Are you sure you want to permanently delete the event \"%s\"?", record.getTitle()),
                                        "Delete", (dialogEvent) -> {
                                            final var eventId = record.getId();
                                            eventService.deleteEvent(eventId);
                                            reloadGridItems();
                                            dialogEvent.getSource().close();
                                        },
                                        "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
                                ).open()
                        )
                )
            ))
            .setHeader("Actions")
            .setFlexGrow(0)
            .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private void showEventDialog(final EventGridItem record) {
        final var dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        final var title = new H2(record == null ? "New event" : "Edit event");
        title.getStyle().set("margin-top", "0");

        final var titleField = new TextField("Title");
        titleField.setRequiredIndicatorVisible(true);
        final var subtitleField = new TextField("Subtitle");
        final var speakerField = new MultiselectComboBox<SpeakerRecord>("Speaker");
        speakerField.setOrdered(true);
        speakerField.setItemLabelGenerator(speakerRecord -> String.format("%s %s",
                speakerRecord.getFirstName(), speakerRecord.getLastName()));
        speakerField.setItems(speakerService.list(0, Integer.MAX_VALUE));
        final var abstractField = new TextArea("Abstract");
        final var agendaField = new TextArea("Agenda");
        final var levelField = new Select<>(EventLevel.values());
        levelField.setLabel("Level");
        final var languageField = new Select<>(EventLanguage.values());
        languageField.setLabel("Language");
        final var locationField = new Select<>(EventLocation.values());
        locationField.setLabel("Location");
        final var dateField = new EnhancedDatePicker("Date");
        dateField.setPattern("yyyy-MM-dd");
        dateField.setMin(LocalDate.now());
        dateField.setWeekNumbersVisible(true);
        final var timeField = new TimePicker("Time");
        timeField.setStep(Duration.ofHours(1));
        final var visibleField = new Checkbox("Visible");

        if (record != null) {
            titleField.setValue(record.getTitle());
            subtitleField.setValue(record.getSubtitle());
            speakerField.setValue(eventSpeakerService.getSpeakersForEvent(record.getId())
                    .collect(Collectors.toSet()));
            abstractField.setValue(record.getAbstract());
            agendaField.setValue(record.getAgenda());
            levelField.setValue(record.getLevel());
            languageField.setValue(record.getLanguage());
            locationField.setValue(record.getLocation());
            dateField.setValue(record.getDate().toLocalDate());
            timeField.setValue(record.getDate().toLocalTime());
            visibleField.setValue(record.getVisible());
        }

        final var form = new FormLayout();
        form.add(titleField, subtitleField, speakerField, levelField,
                abstractField, agendaField, languageField, locationField,
                dateField, timeField, visibleField);

        final var saveButton = new Button("Save");
        saveButton.setDisableOnClick(true);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(record == null || record.getDate().isAfter(LocalDateTime.now()));
        saveButton.addClickListener(event -> {
            if (titleField.getValue().isBlank()) {
                Notification.show("Please enter at least the title!");
                saveButton.setEnabled(true);
            } else if (dateField.getValue() == null || timeField.getValue() == null) {
                Notification.show("Please enter a date and a time or none of them!");
                saveButton.setEnabled(true);
            } else if (LocalDateTime.of(dateField.getValue(), timeField.getValue()).isBefore(LocalDateTime.now())) {
                Notification.show("Please enter a date and time in the future!");
                saveButton.setEnabled(true);
            } else {
                final var eventRecord = record != null
                        ? eventService.get(record.getId()).orElse(eventService.newRecord())
                        : eventService.newRecord();
                eventRecord.setTitle(titleField.getValue());
                eventRecord.setSubtitle(subtitleField.getValue());
                eventRecord.setAbstract(abstractField.getValue());
                eventRecord.setAgenda(agendaField.getValue());
                eventRecord.setLevel(levelField.getValue());
                eventRecord.setLanguage(languageField.getValue());
                eventRecord.setLocation(locationField.getValue());
                eventRecord.setDate(dateField.getValue() == null || timeField.getValue() == null ? null
                        : LocalDateTime.of(dateField.getValue(), timeField.getValue()));
                eventRecord.setVisible(visibleField.getValue());
                eventRecord.store();

                eventSpeakerService.deleteEventSpeakers(eventRecord.getId());
                speakerField.getValue().forEach(speakerRecord -> {
                    if (eventSpeakerService.get(eventRecord.getId(), speakerRecord.getId()).isEmpty()) {
                        final var eventSpeaker = eventSpeakerService.newRecord();
                        eventSpeaker.setEventId(eventRecord.getId());
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
        grid.setItems(query -> eventService.eventsForGrid(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
