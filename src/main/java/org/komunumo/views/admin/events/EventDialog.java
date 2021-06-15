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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.EventLocation;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.entity.EventGridItem;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.util.LocalizedEnhancedDatePickerI18NProvider;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class EventDialog extends Dialog {

    private final Focusable<? extends Component> focusField;

    public EventDialog(final EventGridItem record,
                       final EventService eventService,
                       final SpeakerService speakerService,
                       final EventSpeakerService eventSpeakerService) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

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
        dateField.setI18n(new LocalizedEnhancedDatePickerI18NProvider());
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
                close();
            }
        });
        saveButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        final var cancelButton = new Button("Cancel", event -> close());
        final var buttonBar = new HorizontalLayout(saveButton, cancelButton);

        add(title, form, buttonBar);

        focusField = titleField;
    }

    @Override
    public void open() {
        super.open();
        focusField.focus();
    }
}
