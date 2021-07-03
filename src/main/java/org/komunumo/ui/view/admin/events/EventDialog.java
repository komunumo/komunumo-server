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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.EventLocation;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.KomunumoDateTimePicker;
import org.komunumo.ui.component.KomunumoEditDialog;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class EventDialog extends KomunumoEditDialog<EventRecord> {

    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;

    private Set<SpeakerRecord> speakers;

    public EventDialog(@NotNull final String title,
                       @NotNull final SpeakerService speakerService,
                       @NotNull final EventSpeakerService eventSpeakerService) {
        super(title);
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;
    }

    @Override
    public void createForm() {
        final var title = new TextField("Title");
        final var subtitle = new TextField("Subtitle");
        final var speaker = new MultiselectComboBox<SpeakerRecord>("Speaker");
        final var abstrakt = new TextArea("Abstract");
        final var agenda = new TextArea("Agenda");
        final var level = new Select<>(EventLevel.values());
        final var language = new Select<>(EventLanguage.values());
        final var location = new Select<>(EventLocation.values());
        final var date = new KomunumoDateTimePicker("Date & Time");
        final var visible = new Checkbox("Visible");

        title.setRequiredIndicatorVisible(true);
        speaker.setOrdered(true);
        speaker.setItemLabelGenerator(value -> String.format("%s %s", value.getFirstName(), value.getLastName()));
        speaker.setItems(speakerService.getAllSpeakers());
        level.setLabel("Level");
        language.setLabel("Language");
        location.setLabel("Location");
        date.setMin(LocalDateTime.now());
        visible.addValueChangeListener(changeEvent -> {
            final var value = changeEvent.getValue();
            speaker.setRequiredIndicatorVisible(value);
            level.setRequiredIndicatorVisible(value);
            abstrakt.setRequiredIndicatorVisible(value);
            language.setRequiredIndicatorVisible(value);
            location.setRequiredIndicatorVisible(value);
            date.setRequiredIndicatorVisible(value);
            binder.validate();
        });

        formLayout.add(title, subtitle, speaker, level, abstrakt, agenda,
                language, location, date, visible);

        binder.forField(title)
                .withValidator(new StringLengthValidator(
                        "Please enter the title of the event (max. 255 chars)", 1, 255))
                .bind(EventRecord::getTitle, EventRecord::setTitle);

        binder.forField(subtitle)
                .withValidator(new StringLengthValidator(
                        "The subtitle is too long (max. 255 chars)", 0, 255))
                .bind(EventRecord::getSubtitle, EventRecord::setSubtitle);

        binder.forField(speaker)
                .withValidator(value -> !visible.getValue() || value.size() >= 1,
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
