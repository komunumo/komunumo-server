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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.EventSpeakerEntity;
import org.komunumo.data.entity.KeywordEntity;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.security.AuthenticatedUser;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Route(value = "admin/events", layout = AdminLayout.class)
@PageTitle("Event Administration")
@CssImport(value = "./themes/komunumo/views/admin/events-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public final class EventsView extends ResizableView implements HasUrlParameter<String> {

    @Serial
    private static final long serialVersionUID = -7178074280442155494L;
    private final AuthenticatedUser authenticatedUser;
    private final DatabaseService databaseService;

    private final TextField filterField;
    private final Grid<Event> grid;

    public EventsView(@NotNull final AuthenticatedUser authenticatedUser,
                      @NotNull final DatabaseService databaseService) {
        this.authenticatedUser = authenticatedUser;
        this.databaseService = databaseService;

        addClassNames("events-view", "flex", "flex-col", "h-full");

        grid = new Grid<>();
        configureGrid();
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
                             @Nullable @OptionalParameter final String parameter) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private void configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(new ComponentRenderer<>(
                event -> {
                    final var icon = switch (event.getType()) {
                        case Talk -> new Icon(VaadinIcon.PRESENTATION);
                        case Workshop -> new Icon(VaadinIcon.LAPTOP);
                        case Meetup -> new Icon(VaadinIcon.GROUP);
                        case Sponsored -> new Icon(VaadinIcon.MONEY);
                    };
                    icon.setSize("16px");
                    icon.getElement().setAttribute("title", event.getType().toString());
                    return icon;
                })).setHeader("Type")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(0);

        grid.addColumn(LitRenderer.<Event>of(
                "<span class=\"${item.eventClass}\">${item.title}</span><br/><span .innerHTML=\"${item.speaker}\"></span>")
                .withProperty("eventClass", this::getEventClass)
                .withProperty("title", Event::getTitle)
                .withProperty("speaker", this::renderSpeakerLinks))
                .setHeader("Title & Speaker").setFlexGrow(1);

        final var dateRenderer = LitRenderer.<Event>of(
                "${item.date}<br/>${item.location}")
                .withProperty("date", event -> formatDateTime(event.getDate()))
                .withProperty("location", Event::getLocation);
        grid.addColumn(dateRenderer).setHeader("Date & Location").setAutoWidth(true).setFlexGrow(0).setKey("dateLocation");

        grid.addColumn(new ComponentRenderer<>(event -> {
                    final var button = new EnhancedButton(Integer.toString(event.getAttendeeCount()),
                            clickEvent -> new RegistrationsDialog(databaseService, event, this::reloadGridItems).open()
                    );
                    button.setTitle("Manage registrations for this event");
                    return button;
                }))
                .setHeader("Attendees")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(0)
                .setKey("attendees");

        grid.addColumn(new ComponentRenderer<>(this::createPublishStateIcon))
                .setHeader("Published")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(event -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEventDialog(event));
            editButton.setTitle("Edit this event");
            final var copyButton = new EnhancedButton(new Icon(VaadinIcon.COPY), clickEvent -> copyEvent(event));
            copyButton.setTitle("Copy this event");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteEvent(event));
            deleteButton.setTitle("Delete this event");
            deleteButton.setEnabled(!event.getPublished() && event.getAttendeeCount() == 0);
            return new HorizontalLayout(editButton, copyButton, deleteButton);
        }))
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.setHeightFull();
    }

    private Component createPublishStateIcon(@NotNull final Event event) {
        if (!event.getPublished()) {
            final var missingFields = new ArrayList<String>();
            if (event.getLocation().isBlank()) {
                missingFields.add("location");
            }
            if (event.getDate() == null) {
                missingFields.add("date & time");
            }
            if (event.getLevel() == null) {
                missingFields.add("level");
            }
            if (!missingFields.isEmpty()) {
                final var warning = new Icon(VaadinIcon.WARNING);
                warning.getElement().setAttribute("title", "Missing data: %s".formatted(join(", ", missingFields)));
                return warning;
            }
        }
        final var url = event.getPublished() ? event.getCompleteEventUrl() : event.getCompleteEventPreviewUrl();
        final var anchor = new Anchor(url, new Icon(event.getPublished() ? VaadinIcon.EYE : VaadinIcon.EYE_SLASH));
        anchor.setTitle(event.getPublished() ? "Show the event on the website" : "Show the event preview");
        return anchor;
    }

    private String getEventClass(@NotNull final Event event) {
        return event.getDate() == null || event.getDate().isAfter(LocalDateTime.now()) ? "upcoming-event" : "past-event";
    }

    @Override
    protected void onResize(final int width) {
        grid.getColumnByKey("attendees").setVisible(width >= 1300);
        grid.getColumnByKey("dateLocation").setVisible(width >= 1100);
    }

    private String renderSpeakerLinks(@NotNull final Event event) {
        final var eventSpeakerEntities = event.getSpeakers();
        if (eventSpeakerEntities == null || eventSpeakerEntities.isEmpty()) {
            return "";
        }
        return eventSpeakerEntities.stream()
                .map(EventSpeakerEntity::fullName)
                .map(String::trim)
                .map(s -> String.format("<a href=\"/admin/speakers?filter=%s\">%s</a>", URLEncoder.encode(s, UTF_8), s))
                .collect(Collectors.joining(", "));
    }

    private void newEvent() {
        showEventDialog(databaseService.newEvent());
    }

    private void showEventDialog(@NotNull final Event event) {
        new EventDialog(event.getId() != null ? "Edit Event" : "New Event",
                authenticatedUser, databaseService)
                .open(event, this::reloadGridItems);
    }

    private void copyEvent(@NotNull final Event event) {
        showEventDialog(databaseService.copyEvent(event));
    }

    private void deleteEvent(@NotNull final Event event) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the event \"%s\"?", event.getTitle()),
                "Delete", dialogEvent -> {
            databaseService.deleteEvent(event);
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> databaseService.findEvents(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadEvents() {
        final var resource = new StreamResource("events.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[]{
                    "ID", "Title", "Subtitle", "Speaker", "Description", "Keywords", "Agenda", "Level", "Language", "Location", "Date", "Published"
            });
            grid.getGenericDataView()
                    .getItems().map(event -> new String[]{
                    event.getId().toString(),
                    event.getTitle(),
                    event.getSubtitle(),
                    event.getSpeakers().stream().map(EventSpeakerEntity::fullName).collect(Collectors.joining(", ")),
                    event.getDescription(),
                    event.getKeywords().stream().map(KeywordEntity::keyword).collect(Collectors.joining(", ")),
                    event.getAgenda(),
                    event.getLevel() != null ? event.getLevel().toString() : null,
                    event.getLanguage() != null ? event.getLanguage().toString() : null,
                    event.getLocation(),
                    event.getDate() != null ? event.getDate().toString() : null,
                    event.getPublished().toString()
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
