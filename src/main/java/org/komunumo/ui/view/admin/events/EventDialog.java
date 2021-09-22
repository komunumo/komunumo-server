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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;

import java.time.LocalTime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.DateTimePicker;
import org.komunumo.ui.component.EditDialog;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class EventDialog extends EditDialog<EventRecord> {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private Set<SpeakerRecord> speakers;

    public EventDialog(@NotNull final String title,
                       @NotNull final EventService eventService,
                       @NotNull final SpeakerService speakerService,
                       @NotNull final EventSpeakerService eventSpeakerService) {
        super(title);
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<EventRecord> binder) {
        final var title = new TextField("Title");
        final var subtitle = new TextField("Subtitle");
        final var speaker = new MultiselectComboBox<SpeakerRecord>("Speaker");
        final var abstrakt = new TextArea("Abstract");
        final var agenda = new TextArea("Agenda");
        final var level = new Select<>(EventLevel.values());
        final var language = new Select<>(EventLanguage.values());
        final var location = new ComboBox<String>("Location");
        final var date = new DateTimePicker("Date & Time");
        final var duration = new TimePicker("Duration");
        final var visible = new Checkbox("Visible");

        title.setRequiredIndicatorVisible(true);
        title.setValueChangeMode(EAGER);
        subtitle.setValueChangeMode(EAGER);
        speaker.setOrdered(true);
        speaker.setItemLabelGenerator(value -> String.format("%s %s", value.getFirstName(), value.getLastName()));
        speaker.setItems(speakerService.getAllSpeakers());
        level.setLabel("Level");
        language.setLabel("Language");
        location.setItems(eventService.getAllLocations());
        location.setAllowCustomValue(true);
        date.setMin(LocalDateTime.now());
        duration.setStep(Duration.ofMinutes(15));
        duration.setMinTime(LocalTime.of(1, 0));
        duration.setMaxTime(LocalTime.of(3, 0));
        visible.addValueChangeListener(changeEvent -> {
            final var value = changeEvent.getValue();
            speaker.setRequiredIndicatorVisible(value);
            level.setRequiredIndicatorVisible(value);
            abstrakt.setRequiredIndicatorVisible(value);
            language.setRequiredIndicatorVisible(value);
            location.setRequiredIndicatorVisible(value);
            date.setRequiredIndicatorVisible(value);
            duration.setRequiredIndicatorVisible(value);
            binder.validate();
        });

        formLayout.add(title, subtitle, speaker, level, abstrakt, agenda,
                language, location, date, duration, visible);

        binder.forField(title)
                .withValidator(new StringLengthValidator(
                        "Please enter the title of the event (max. 255 chars)", 1, 255))
                .bind(EventRecord::getTitle, EventRecord::setTitle);

        binder.forField(subtitle)
                .withValidator(new StringLengthValidator(
                        "The subtitle is too long (max. 255 chars)", 0, 255))
                .bind(EventRecord::getSubtitle, EventRecord::setSubtitle);

        binder.forField(speaker)
                .withValidator(value -> !visible.getValue() || !value.isEmpty(),
                        "Please select at least one speaker")
                .bind(this::getSpeaker, this::setSpeaker);

        binder.forField(level)
                .withValidator(value -> !visible.getValue() || value != null,
                        "Please select a level")
                .bind(EventRecord::getLevel, EventRecord::setLevel);

        binder.forField(abstrakt)
                .withValidator(value -> !visible.getValue() || value != null && !value.isBlank(),
                        "Please enter the abstract")
                .bind(EventRecord::getAbstract, EventRecord::setAbstract);

        binder.forField(agenda)
                .bind(EventRecord::getAgenda, EventRecord::setAgenda);

        binder.forField(language)
                .withValidator(value -> !visible.getValue() || value != null,
                        "Please select a language")
                .bind(EventRecord::getLanguage, EventRecord::setLanguage);

        binder.forField(location)
                .withValidator(value -> !visible.getValue() || value != null,
                        "Please select a location")
                .bind(EventRecord::getLocation, EventRecord::setLocation);

        binder.forField(date)
                .withValidator(value -> !visible.getValue() && (value == null || value.isAfter(LocalDateTime.now()))
                                || value != null && value.isAfter(LocalDateTime.now()),
                        "Please enter a date and time in the future")
                .bind(EventRecord::getDate, EventRecord::setDate);

        binder.forField(duration)
                .withValidator(value -> !visible.getValue() || (value != null && value.isAfter(LocalTime.MIN) && value.isBefore(LocalTime.MAX)),
                        "Please enter a duration")
                .bind(EventRecord::getDuration, EventRecord::setDuration);

        binder.forField(visible)
                .bind(EventRecord::getVisible, EventRecord::setVisible);
    }

    private Set<SpeakerRecord> getSpeaker(@NotNull final EventRecord record) {
        return speakers;
    }

    private void setSpeaker(@NotNull final EventRecord record, @Nullable final Set<SpeakerRecord> speakers) {
        this.speakers = speakers != null ? speakers : Set.of();
    }

    @Override
    public void open(@NotNull final EventRecord record, @Nullable final Callback afterSave) {
        speakers = eventSpeakerService.getSpeakersForEvent(record)
                .collect(Collectors.toSet());
        super.open(record, () -> {
            eventSpeakerService.setEventSpeakers(record, speakers);
            if (afterSave != null) {
                afterSave.execute();
            }
        });
    }
}
