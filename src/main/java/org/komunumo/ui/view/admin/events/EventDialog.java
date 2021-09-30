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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Keyword;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Speaker;
import org.komunumo.data.service.EventKeywordService;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.KeywordService;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.DateTimePicker;
import org.komunumo.ui.component.EditDialog;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class EventDialog extends EditDialog<Event> {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;
    private final MemberService memberService;
    private final EventMemberService eventMemberService;
    private final KeywordService keywordService;
    private final EventKeywordService eventKeywordService;

    private Set<Speaker> speakers;
    private Set<Member> organizers;
    private Set<Keyword> keywords;
    private Callback afterOpen;

    public EventDialog(@NotNull final String title,
                       @NotNull final EventService eventService,
                       @NotNull final SpeakerService speakerService,
                       @NotNull final EventSpeakerService eventSpeakerService,
                       @NotNull final MemberService memberService,
                       @NotNull final EventMemberService eventMemberService,
                       @NotNull final KeywordService keywordService,
                       @NotNull final EventKeywordService eventKeywordService) {
        super(title);
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;
        this.memberService = memberService;
        this.eventMemberService = eventMemberService;
        this.keywordService = keywordService;
        this.eventKeywordService = eventKeywordService;
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<Event> binder) {
        final var title = new TextField("Title");
        final var subtitle = new TextField("Subtitle");
        final var speaker = new MultiselectComboBox<Speaker>("Speaker");
        final var organizer = new MultiselectComboBox<Member>("Organizer");
        final var description = new TextArea("Description");
        final var keyword = new MultiselectComboBox<Keyword>("Keyword");
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
        organizer.setOrdered(true);
        organizer.setItemLabelGenerator(value -> String.format("%s %s", value.getFirstName(), value.getLastName()));
        organizer.setItems(memberService.getAllAdmins());
        organizer.setRequiredIndicatorVisible(true);
        keyword.setOrdered(true);
        keyword.setItemLabelGenerator(Keyword::getKeyword);
        keyword.setItems(keywordService.getAllKeywords());
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
            description.setRequiredIndicatorVisible(value);
            language.setRequiredIndicatorVisible(value);
            location.setRequiredIndicatorVisible(value);
            date.setRequiredIndicatorVisible(value);
            duration.setRequiredIndicatorVisible(value);
            binder.validate();
        });

        formLayout.add(title, subtitle, speaker, organizer, level, description, keyword, agenda,
                language, location, date, duration, visible);

        binder.forField(title)
                .withValidator(new StringLengthValidator(
                        "Please enter a title (max. 255 chars)", 1, 255))
                .bind(Event::getTitle, Event::setTitle);

        binder.forField(subtitle)
                .withValidator(new StringLengthValidator(
                        "The subtitle is too long (max. 255 chars)", 0, 255))
                .bind(Event::getSubtitle, Event::setSubtitle);

        binder.forField(speaker)
                .withValidator(value -> !visible.getValue() || !value.isEmpty(),
                        "Please select at least one speaker")
                .bind(this::getSpeakers, this::setSpeakers);

        binder.forField(organizer)
                .withValidator(value -> !value.isEmpty(),
                        "Please select at least one organizer")
                .bind(this::getOrganizer, this::setOrganizer);

        binder.forField(level)
                .withValidator(value -> !visible.getValue() || value != null,
                        "Please select a level")
                .bind(Event::getLevel, Event::setLevel);

        binder.forField(description)
                .withValidator(value -> !visible.getValue() || value != null && !value.isBlank(),
                        "Please enter a description")
                .bind(Event::getDescription, Event::setDescription);

        binder.forField(keyword)
                .bind(this::getKeyword, this::setKeyword);

        binder.forField(agenda)
                .bind(Event::getAgenda, Event::setAgenda);

        binder.forField(language)
                .withValidator(value -> !visible.getValue() || value != null,
                        "Please select a language")
                .bind(Event::getLanguage, Event::setLanguage);

        binder.forField(location)
                .withValidator(value -> !visible.getValue() || value != null,
                        "Please select a location")
                .bind(Event::getLocation, Event::setLocation);

        binder.forField(date)
                .withValidator(value -> isPastEvent(date) || !visible.getValue() && (value == null || value.isAfter(LocalDateTime.now()))
                                || value != null && value.isAfter(LocalDateTime.now()),
                        "Please enter a date and time in the future")
                .bind(Event::getDate, Event::setDate);

        binder.forField(duration)
                .withValidator(value -> !visible.getValue() || (value != null && value.isAfter(LocalTime.MIN) && value.isBefore(LocalTime.MAX)),
                        "Please enter a duration")
                .bind(Event::getDuration, Event::setDuration);

        binder.forField(visible)
                .bind(Event::getVisible, Event::setVisible);

        afterOpen = () -> {
            if (isPastEvent(date)) {
                binder.setReadOnly(true);
                binder.setValidatorsDisabled(true);
            }
        };
    }

    private boolean isPastEvent(@NotNull final DateTimePicker date) {
        return date.getValue() != null && date.getValue().isBefore(LocalDateTime.now());
    }

    private Set<Speaker> getSpeakers(@NotNull final Event event) {
        return speakers;
    }

    private void setSpeakers(@NotNull final Event event, @Nullable final Set<Speaker> speakers) {
        this.speakers = speakers != null ? speakers : Set.of();
    }

    private Set<Member> getOrganizer(@NotNull final Event event) {
        return organizers;
    }

    private void setOrganizer(@NotNull final Event event, @Nullable final Set<Member> organizers) {
        this.organizers = organizers != null ? organizers : Set.of();
    }

    private Set<Keyword> getKeyword(@NotNull final Event event) {
        return keywords;
    }

    private void setKeyword(@NotNull final Event event, @Nullable final Set<Keyword> keywords) {
        this.keywords = keywords != null ? keywords : Set.of();
    }

    @Override
    public void open(@NotNull final Event event, @Nullable final Callback afterSave) {
        speakers = eventSpeakerService.getSpeakersForEvent(event)
                .collect(Collectors.toSet());
        organizers = eventMemberService.getOrganizersForEvent(event)
                .collect(Collectors.toSet());
        keywords = eventKeywordService.getKeywordsForEvent(event)
                .collect(Collectors.toSet());
        super.open(event,
                () -> {
                    if (afterOpen != null) {
                        afterOpen.execute();
                    }
                },
                () -> {
                    eventSpeakerService.setEventSpeakers(event, speakers);
                    eventMemberService.setEventOrganizers(event, organizers);
                    eventKeywordService.setEventKeywords(event, keywords);
                    if (afterSave != null) {
                        afterSave.execute();
                    }
                }
        );
    }
}
