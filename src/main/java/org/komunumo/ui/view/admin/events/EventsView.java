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

import com.opencsv.CSVWriter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Keyword;
import org.komunumo.data.entity.Speaker;
import org.komunumo.data.service.EventKeywordService;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.KeywordService;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Route(value = "admin/events", layout = AdminLayout.class)
@PageTitle("Event Administration")
@CssImport(value = "./themes/komunumo/views/admin/events-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
public class EventsView extends ResizableView implements HasUrlParameter<String> {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;
    private final MemberService memberService;
    private final EventMemberService eventMemberService;
    private final KeywordService keywordService;
    private final EventKeywordService eventKeywordService;

    private final TextField filterField;
    private final Grid<Event> grid;

    public EventsView(@NotNull final EventService eventService,
                      @NotNull final SpeakerService speakerService,
                      @NotNull final EventSpeakerService eventSpeakerService,
                      @NotNull final MemberService memberService,
                      @NotNull final EventMemberService eventMemberService,
                      @NotNull final KeywordService keywordService,
                      @NotNull final EventKeywordService eventKeywordService) {
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;
        this.memberService = memberService;
        this.eventMemberService = eventMemberService;
        this.keywordService = keywordService;
        this.eventKeywordService = eventKeywordService;

        addClassNames("events-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter events by title or speaker");

        final var newEventButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> newEvent());
        newEventButton.setTitle("Add a new event");

        final var refreshEventsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshEventsButton.setTitle("Refresh the list of events");

        final var downloadEventsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), event -> downloadEvents());
        downloadEventsButton.setTitle("Download the list of events");

        final var optionBar = new HorizontalLayout(filterField, newEventButton, refreshEventsButton, downloadEventsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    @Override
    public void setParameter(@NotNull final BeforeEvent beforeEvent,
                             @Nullable @OptionalParameter String parameter) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private Grid<Event> createGrid() {
        final var grid = new Grid<Event>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<Event>of("<span style=\"font-weight: bold;\">[[item.title]]</span><br/><span inner-h-t-m-l=\"[[item.speaker]]\"></span>")
                .withProperty("title", Event::getTitle)
                .withProperty("speaker", this::renderSpeakerLinks))
                .setHeader("Title & Speaker").setFlexGrow(1);

        final var dateRenderer = TemplateRenderer.<Event>of(
                "[[item.date]]<br/>[[item.location]]")
                .withProperty("date", event -> formatDateTime(event.getDate()))
                .withProperty("location", Event::getLocation);
        grid.addColumn(dateRenderer).setHeader("Date & Location").setAutoWidth(true).setFlexGrow(0).setKey("dateLocation");

        grid.addColumn(Event::getAttendeeCount).setHeader("Attendees").setAutoWidth(true).setFlexGrow(0).setKey("attendees");

        grid.addColumn(new ComponentRenderer<>(event -> new Icon(event.getVisible() ? VaadinIcon.EYE : VaadinIcon.EYE_SLASH)))
                .setHeader("Visible")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(event -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEventDialog(event));
            editButton.setTitle("Edit this event");
            final var copyButton = new EnhancedButton(new Icon(VaadinIcon.COPY), clickEvent -> copyEvent(event));
            copyButton.setTitle("Copy this event");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteEvent(event));
            deleteButton.setTitle("Delete this event");
            return new HorizontalLayout(editButton, copyButton, deleteButton);

        }))
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    @Override
    protected void onResize(final int width) {
        grid.getColumnByKey("attendees").setVisible(width >= 1300);
        grid.getColumnByKey("dateLocation").setVisible(width >= 1100);
    }

    private String renderSpeakerLinks(@NotNull Event event) {
        final var speakers = event.getSpeakers();
        if (speakers == null || speakers.isEmpty()) {
            return "";
        }
        return speakers.stream()
                .map(Speaker::getFullName)
                .map(String::trim)
                .map(s -> String.format("<a href=\"/admin/speakers?filter=%s\">%s</a>", URLEncoder.encode(s, UTF_8), s))
                .collect(Collectors.joining(", "));
    }

    private void newEvent() {
        showEventDialog(eventService.newEvent());
    }

    private void showEventDialog(@NotNull final Event event) {
        new EventDialog(event.getId() != null ? "Edit Event" : "New Event",
                eventService,
                speakerService, eventSpeakerService,
                memberService, eventMemberService,
                keywordService, eventKeywordService)
                .open(event, this::reloadGridItems);
    }

    private void copyEvent(@NotNull final Event event) {
        showEventDialog(eventService.copyEvent(event));
    }

    private void deleteEvent(@NotNull final Event event) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the event \"%s\"?", event.getTitle()),
                "Delete", dialogEvent -> {
            eventService.deleteEvent(event);
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> eventService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadEvents() {
        final var resource = new StreamResource("events.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[]{
                    "ID", "Title", "Subtitle", "Speaker", "Description", "Keywords", "Agenda", "Level", "Language", "Location", "Date", "Visible"
            });
            grid.getGenericDataView()
                    .getItems().map(event -> new String[]{
                    event.getId().toString(),
                    event.getTitle(),
                    event.getSubtitle(),
                    event.getSpeakers().stream().map(Speaker::getFullName).collect(Collectors.joining(", ")),
                    event.getDescription(),
                    event.getKeywords().stream().map(Keyword::getKeyword).collect(Collectors.joining(", ")),
                    event.getAgenda(),
                    event.getLevel() != null ? event.getLevel().toString() : null,
                    event.getLanguage() != null ? event.getLanguage().toString() : null,
                    event.getLocation(),
                    event.getDate() != null ? event.getDate().toString() : null,
                    event.getVisible().toString()
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
