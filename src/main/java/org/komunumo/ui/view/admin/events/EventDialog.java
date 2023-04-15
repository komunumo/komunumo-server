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

import com.vaadin.componentfactory.multiselect.MultiComboBox;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.Callback;
import org.komunumo.data.db.enums.EventLanguage;
import org.komunumo.data.db.enums.EventLevel;
import org.komunumo.data.db.enums.EventType;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.EventSpeakerEntity;
import org.komunumo.data.entity.KeywordEntity;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.security.AuthenticatedUser;
import org.komunumo.ui.component.CustomDateTimePicker;
import org.komunumo.ui.component.CustomLabel;
import org.komunumo.ui.component.EditDialog;
import org.komunumo.util.URLUtil;
import org.vaadin.addons.jhoffmann99.TrixEditor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class EventDialog extends EditDialog<Event> {

    private final AuthenticatedUser authenticatedUser;
    private final DatabaseService databaseService;

    private Set<EventSpeakerEntity> speakers;
    private Set<Member> organizers;
    private Set<KeywordEntity> keywords;
    private Callback afterOpen;

    public EventDialog(@NotNull final String title,
                       @NotNull final AuthenticatedUser authenticatedUser,
                       @NotNull final DatabaseService databaseService) {
        super(title);
        this.authenticatedUser = authenticatedUser;
        this.databaseService = databaseService;
    }

    @SuppressWarnings("checkstyle:MethodLength") // just a lot of fields for the form
    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<Event> binder) {
        final var type = new Select<EventType>();
        final var title = new TextField("Title");
        final var subtitle = new TextField("Subtitle");
        final var speaker = new MultiComboBox<EventSpeakerEntity>("Speaker");
        final var organizer = new MultiComboBox<Member>("Organizer");
        final var description = new TrixEditor("");
        final var keyword = new MultiComboBox<KeywordEntity>("Keyword");
        final var agenda = new TrixEditor("");
        final var level = new Select<EventLevel>();
        final var language = new Select<EventLanguage>();
        final var room = new TextField("Room");
        final var travelInstructions = new TextField("Travel instructions");
        final var location = new ComboBox<String>("Location");
        final var webinarUrl = new TextField("Webinar URL");
        final var youtTube = new TextField("YouTube");
        final var date = new CustomDateTimePicker("Date & Time");
        final var duration = new TimePicker("Duration");
        final var eventUrl = new TextField("Event URL");
        final var attendeeLimit = new IntegerField("Attendee limit (0 = no limit)");
        final var membersOnly = new Checkbox("Members only");
        final var published = new Checkbox("Published");

        type.setLabel("Type");
        type.setRequiredIndicatorVisible(true);
        type.setItems(EventType.values());
        title.setRequiredIndicatorVisible(true);
        title.setValueChangeMode(EAGER);
        title.addValueChangeListener(changeEvent -> {
            if (eventUrl.getValue().equals(URLUtil.createReadableUrl(changeEvent.getOldValue()))) {
                eventUrl.setValue(URLUtil.createReadableUrl(changeEvent.getValue()));
            }
        });
        subtitle.setValueChangeMode(EAGER);
        speaker.setItemLabelGenerator(EventSpeakerEntity::fullName);
        speaker.setItems(databaseService.getAllEventSpeakers());
        organizer.setItemLabelGenerator(value -> String.format("%s %s", value.getFirstName(), value.getLastName()));
        organizer.setItems(databaseService.getAllAdmins());
        organizer.setRequiredIndicatorVisible(true);
        keyword.setItemLabelGenerator(KeywordEntity::keyword);
        keyword.setItems(databaseService.getAllKeywords());
        level.setLabel("Level");
        level.setItems(EventLevel.values());
        language.setLabel("Language");
        language.setItems(EventLanguage.values());
        location.setItems(databaseService.getAllEventLocations());
        location.setAllowCustomValue(true);
        location.addValueChangeListener(changeEvent -> {
            final var value = changeEvent.getValue();
            final var isOnline = "Online".equalsIgnoreCase(value);
            webinarUrl.setEnabled(isOnline);
            webinarUrl.setRequiredIndicatorVisible(published.getValue() && isOnline);
            room.setRequiredIndicatorVisible(!isOnline);
            updateEventUrlPrefix(location, date, eventUrl);
            binder.validate();
        });
        room.setValueChangeMode(EAGER);
        travelInstructions.setValueChangeMode(EAGER);
        webinarUrl.setValueChangeMode(EAGER);
        date.setMin(LocalDateTime.now());
        date.addValueChangeListener(changeEvent -> updateEventUrlPrefix(location, date, eventUrl));
        duration.setStep(Duration.ofMinutes(15));
        duration.setMin(LocalTime.of(1, 0));
        duration.setMax(LocalTime.of(3, 0));
        eventUrl.setValueChangeMode(EAGER);
        updateEventUrlPrefix(location, date, eventUrl);
        attendeeLimit.setMin(0);
        attendeeLimit.setMax(500);
        attendeeLimit.setStep(10);
        attendeeLimit.setStepButtonsVisible(true);
        published.addValueChangeListener(changeEvent -> {
            final var value = changeEvent.getValue();
            speaker.setRequiredIndicatorVisible(value);
            level.setRequiredIndicatorVisible(value);
            description.setRequiredIndicatorVisible(value);
            language.setRequiredIndicatorVisible(value);
            location.setRequiredIndicatorVisible(value);
            room.setRequiredIndicatorVisible(!"Online".equalsIgnoreCase(location.getValue()));
            date.setRequiredIndicatorVisible(value);
            duration.setRequiredIndicatorVisible(value);
            eventUrl.setRequiredIndicatorVisible(value);
            binder.validate();
        });

        formLayout.add(type, title, subtitle, speaker, organizer, level, new CustomLabel("Description"), description, keyword,
                new CustomLabel("Agenda"), agenda, language, location, room, travelInstructions, webinarUrl, youtTube,
                date, duration, eventUrl, attendeeLimit, membersOnly, published);

        binder.forField(type)
                .withValidator(value -> !published.getValue() || value != null,
                        "Please select a type")
                .bind(Event::getType, Event::setType);

        binder.forField(title)
                .withValidator(new StringLengthValidator(
                        "Please enter a title (max. 255 chars)", 1, 255))
                .bind(Event::getTitle, Event::setTitle);

        binder.forField(subtitle)
                .withValidator(new StringLengthValidator(
                        "The subtitle is too long (max. 255 chars)", 0, 255))
                .bind(Event::getSubtitle, Event::setSubtitle);

        binder.forField(speaker)
                .withValidator(value -> !published.getValue() || !value.isEmpty(),
                        "Please select at least one speaker")
                .bind(this::getSpeakers, this::setSpeakers);

        binder.forField(organizer)
                .withValidator(value -> !value.isEmpty(),
                        "Please select at least one organizer")
                .bind(this::getOrganizer, this::setOrganizer);

        binder.forField(level)
                .withValidator(value -> !published.getValue() || value != null,
                        "Please select a level")
                .bind(Event::getLevel, Event::setLevel);

        binder.forField(description)
                .withValidator(value -> !published.getValue() || value != null && !value.isBlank(),
                        "Please enter a description")
                .bind(Event::getDescription, Event::setDescription);

        binder.forField(keyword)
                .bind(this::getKeyword, this::setKeyword);

        binder.forField(agenda)
                .bind(Event::getAgenda, Event::setAgenda);

        binder.forField(language)
                .withValidator(value -> !published.getValue() || value != null,
                        "Please select a language")
                .bind(Event::getLanguage, Event::setLanguage);

        binder.forField(location)
                .withValidator(value -> !published.getValue() || value != null,
                        "Please select a location")
                .bind(Event::getLocation, Event::setLocation);

        binder.forField(room)
                .withValidator(value -> !published.getValue() || "Online".equalsIgnoreCase(location.getValue()) || !value.isBlank(),
                        "Please enter a room")
                .bind(Event::getRoom, Event::setRoom);

        binder.forField(travelInstructions)
                .withValidator(value -> value.isBlank() || URLUtil.isValid(value),
                        "Please enter a valid URL")
                .bind(Event::getTravelInstructions, Event::setTravelInstructions);

        binder.forField(webinarUrl)
                .withValidator(value -> !published.getValue() || !"Online".equalsIgnoreCase(location.getValue()) || URLUtil.isValid(value),
                        "Please enter a valid URL")
                .bind(Event::getWebinarUrl, Event::setWebinarUrl);

        binder.forField(youtTube)
                .bind(Event::getYoutube, Event::setYoutube);

        binder.forField(date)
                .withValidator(value -> isPastEvent(date) || !published.getValue() && (value == null || value.isAfter(LocalDateTime.now()))
                                || value != null && value.isAfter(LocalDateTime.now()),
                        "Please enter a date and time in the future")
                .bind(Event::getDate, Event::setDate);

        binder.forField(duration)
                .withValidator(value -> !published.getValue() || (value != null && value.isAfter(LocalTime.MIN) && value.isBefore(LocalTime.MAX)),
                        "Please enter a duration")
                .bind(Event::getDuration, Event::setDuration);

        binder.forField(eventUrl) // TODO check for duplicates
                .withValidator(value -> !published.getValue() || value != null && !value.isBlank(),
                        "Please enter a valid event URL")
                .bind(Event::getEventUrl, Event::setEventUrl);

        binder.forField(attendeeLimit)
                .bind(Event::getAttendeeLimit, Event::setAttendeeLimit);

        binder.forField(membersOnly)
                .bind(Event::getMembersOnly, Event::setMembersOnly);

        binder.forField(published)
                .bind(Event::getPublished, Event::setPublished);

        afterOpen = () -> {
            webinarUrl.setEnabled("Online".equalsIgnoreCase(location.getValue()));
            if (isPastEvent(date)) {
                binder.setReadOnly(true);
                binder.setValidatorsDisabled(true);
                youtTube.setReadOnly(false);
            }
        };
    }

    private void updateEventUrlPrefix(@NotNull final ComboBox<String> location,
                                      @NotNull final CustomDateTimePicker date,
                                      @NotNull final TextField eventUrl) {
        final var locationValue = location.getValue();
        final var dateValue = date.getValue();
        final var locationText = locationValue == null || locationValue.isBlank() ? "{location}" : URLUtil.createReadableUrl(locationValue);
        final var year = dateValue == null ? "{year}" : Year.from(dateValue).toString();
        eventUrl.setPrefixComponent(new Span("%s/%s/".formatted(locationText, year)));
    }

    private boolean isPastEvent(@NotNull final CustomDateTimePicker date) {
        return date.getValue() != null && date.getValue().isBefore(LocalDateTime.now());
    }

    private Set<EventSpeakerEntity> getSpeakers(@NotNull final Event event) {
        return speakers;
    }

    private void setSpeakers(@NotNull final Event event, @Nullable final Set<EventSpeakerEntity> eventSpeakerEntities) {
        this.speakers = eventSpeakerEntities != null ? eventSpeakerEntities : Set.of();
    }

    private Set<Member> getOrganizer(@NotNull final Event event) {
        return organizers;
    }

    @SuppressWarnings("checkstyle:HiddenField") // setter
    private void setOrganizer(@NotNull final Event event, @Nullable final Set<Member> organizers) {
        this.organizers = organizers != null ? organizers : Set.of();
    }

    private Set<KeywordEntity> getKeyword(@NotNull final Event event) {
        return keywords;
    }

    @SuppressWarnings("checkstyle:HiddenField") // setter
    private void setKeyword(@NotNull final Event event, @Nullable final Set<KeywordEntity> keywords) {
        this.keywords = keywords != null ? keywords : Set.of();
    }

    @Override
    public void open(@NotNull final Event event, @Nullable final Callback afterSave) {
        speakers = Set.copyOf(event.getSpeakers());
        organizers = databaseService.getOrganizersForEvent(event)
                .collect(Collectors.toSet());
        final var organizer = authenticatedUser.get();
        if (organizers.isEmpty() && event.getId() == null && organizer.isPresent()) {
            organizers.add(organizer.get());
        }
        keywords = Set.copyOf(event.getKeywords());
        super.open(event,
                () -> {
                    if (afterOpen != null) {
                        afterOpen.execute();
                    }
                },
                () -> {
                    databaseService.setEventSpeakers(event, speakers);
                    databaseService.setEventOrganizers(event, organizers);
                    databaseService.setEventKeywords(event, keywords);
                    if (afterSave != null) {
                        afterSave.execute();
                    }
                }
        );
    }
}
