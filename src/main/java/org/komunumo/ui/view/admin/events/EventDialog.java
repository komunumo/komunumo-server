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
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.EventLocation;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.komunumo.util.ComponentUtil.createDatePicker;

public class EventDialog extends Dialog {

    private final Focusable<? extends Component> focusField;

    public EventDialog(@NotNull final EventRecord event,
                       @NotNull final EventService eventService,
                       @NotNull final SpeakerService speakerService,
                       @NotNull final EventSpeakerService eventSpeakerService) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        final var title = new H2(event.getId() == null ? "New event" : "Edit event");
        title.getStyle().set("margin-top", "0");

        final var titleField = new TextField("Title");
        titleField.setRequiredIndicatorVisible(true);
        titleField.setValue(event.getTitle());

        final var subtitleField = new TextField("Subtitle");
        subtitleField.setValue(event.getSubtitle());

        final var speakerField = new MultiselectComboBox<SpeakerRecord>("Speaker");
        speakerField.setOrdered(true);
        speakerField.setItemLabelGenerator(speaker -> String.format("%s %s", speaker.getFirstName(), speaker.getLastName()));
        speakerField.setItems(speakerService.getAllSpeakers());
        speakerField.setValue(eventSpeakerService.getSpeakersForEvent(event)
                .collect(Collectors.toSet()));

        final var abstractField = new TextArea("Abstract");
        abstractField.setValue(event.getAbstract());

        final var agendaField = new TextArea("Agenda");
        agendaField.setValue(event.getAgenda());

        final var levelField = new Select<>(EventLevel.values());
        levelField.setLabel("Level");
        levelField.setValue(event.getLevel());

        final var languageField = new Select<>(EventLanguage.values());
        languageField.setLabel("Language");
        languageField.setValue(event.getLanguage());

        final var locationField = new Select<>(EventLocation.values());
        locationField.setLabel("Location");
        locationField.setValue(event.getLocation());

        final var dateField = createDatePicker("Date", event.getDate() == null ? null : event.getDate().toLocalDate());

        final var timeField = new TimePicker("Time");
        timeField.setStep(Duration.ofHours(1));
        if (event.getDate() != null) {
            timeField.setValue(event.getDate().toLocalTime());
        }

        final var visibleField = new Checkbox("Visible");
        visibleField.setValue(event.getVisible());
        visibleField.addValueChangeListener(changeEvent -> {
            if (changeEvent.getValue() && (titleField.isEmpty()
                    || speakerField.isEmpty()
                    || levelField.isEmpty()
                    || abstractField.isEmpty()
                    || agendaField.isEmpty()
                    || languageField.isEmpty()
                    || locationField.isEmpty()
                    || dateField.isEmpty()
                    || timeField.isEmpty())) {
                Notification.show("To make an event visible on the website, you have to fill out all fields!");
                visibleField.setValue(false);
            }
        });

        final var form = new FormLayout();
        form.add(titleField, subtitleField, speakerField, levelField,
                abstractField, agendaField, languageField, locationField,
                dateField, timeField, visibleField);

        final var saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(event.getDate() == null || event.getDate().isAfter(LocalDateTime.now()));
        saveButton.addClickListener(clickEvent -> {
            if (titleField.getValue().isBlank()) {
                Notification.show("Please enter at least the title!");
            } else if (dateField.getValue() == null && timeField.getValue() != null
                    || dateField.getValue() != null && timeField.getValue() == null) {
                Notification.show("Please enter a date and a time or none of them!");
            } else if (dateField.getValue() != null && timeField.getValue() != null
                    && LocalDateTime.of(dateField.getValue(), timeField.getValue()).isBefore(LocalDateTime.now())) {
                Notification.show("Please enter a date and time in the future!");
            } else {
                saveButton.setEnabled(false);
                event.setTitle(titleField.getValue());
                event.setSubtitle(subtitleField.getValue());
                event.setAbstract(abstractField.getValue());
                event.setAgenda(agendaField.getValue());
                event.setLevel(levelField.getValue());
                event.setLanguage(languageField.getValue());
                event.setLocation(locationField.getValue());
                event.setDate(dateField.getValue() == null || timeField.getValue() == null ? null
                        : LocalDateTime.of(dateField.getValue(), timeField.getValue()));
                event.setVisible(visibleField.getValue());
                eventService.store(event);
                eventSpeakerService.setEventSpeakers(event, speakerField.getValue());

                Notification.show("Event saved.");
                close();
            }
        });
        saveButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        final var cancelButton = new Button("Cancel", clickEvent -> close());
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
