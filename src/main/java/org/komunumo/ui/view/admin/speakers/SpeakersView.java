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

package org.komunumo.ui.view.admin.speakers;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.view.admin.AdminView;

import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.data.db.tables.Speaker.SPEAKER;

@Route(value = "admin/speakers", layout = AdminView.class)
@PageTitle("Speaker Administration")
public class SpeakersView extends Div implements HasUrlParameter<String> {

    private final SpeakerService speakerService;
    private final TextField filterField;
    private final Grid<Record> grid;

    public SpeakersView(@NotNull final SpeakerService speakerService) {
        this.speakerService = speakerService;

        addClassNames("speakers-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter speakers by name, company, email, or twitter");

        final var newSpeakerButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> newSpeaker());
        newSpeakerButton.setTitle("Add a new speaker");
        final var refreshSpeakersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshSpeakersButton.setTitle("Refresh the list of speakers");
        final var optionBar = new HorizontalLayout(filterField, newSpeakerButton, refreshSpeakersButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    @Override
    public void setParameter(@NotNull final BeforeEvent event,
                             @Nullable @OptionalParameter String parameter) {
        final var location = event.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private String getFullName(@NotNull final Record record) {
        return String.format("%s %s", record.get(SPEAKER.FIRST_NAME), record.get(SPEAKER.LAST_NAME));
    }

    private Grid<Record> createGrid() {
        final var grid = new Grid<Record>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<Record>of("<span style=\"font-weight: bold;\">[[item.firstName]] [[item.lastName]]</span>")
                .withProperty("firstName", record -> record.get(SPEAKER.FIRST_NAME))
                .withProperty("lastName", record -> record.get(SPEAKER.LAST_NAME)))
                .setHeader("Name").setAutoWidth(true);
        grid.addColumn(record -> record.get(SPEAKER.COMPANY)).setHeader("Company").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<Record>of("<a href=\"mailto:[[item.email]]\" target=\"_blank\">[[item.email]]</a>")
                .withProperty("email", record -> record.get(SPEAKER.EMAIL)))
                .setHeader("Email").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<Record>of("<a href=\"https://twitter.com/[[item.twitter]]\" target=\"_blank\">[[item.twitter]]</a>")
                .withProperty("twitter", record -> record.get(SPEAKER.TWITTER)))
                .setHeader("Twitter").setAutoWidth(true);

        final var eventCountRenderer = TemplateRenderer.<Record>of(
                "<a href=\"/admin/events?filter=[[item.filterValue]]\">[[item.eventCount]]</a>")
                .withProperty("eventCount", this::getEventCount)
                .withProperty("filterValue", record -> URLEncoder.encode(getFullName(record), UTF_8));
        grid.addColumn(eventCountRenderer).setHeader("Events").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), event -> editSpeaker(record.get(SPEAKER.ID)));
            editButton.setTitle("Edit this speaker");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), event -> deleteSpeaker(record.get(SPEAKER.ID)));
            deleteButton.setTitle("Delete this speaker");
            deleteButton.setEnabled(getEventCount(record) == 0);
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setFlexGrow(0)
                .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private long getEventCount(@NotNull final Record record) {
        final var eventCount = record.get("event_count", Long.class);
        return eventCount == null ? 0 : eventCount;
    }

    private void newSpeaker() {
        showSpeakerDialog(speakerService.newSpeaker());
    }

    private void editSpeaker(@NotNull final Long speakerId) {
        final var speaker = speakerService.get(speakerId);
        if (speaker.isPresent()) {
            showSpeakerDialog(speaker.get());
        } else {
            Notification.show("This speaker does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void showSpeakerDialog(@NotNull final SpeakerRecord speaker) {
        final var dialog = new SpeakerDialog(speaker, speakerService);
        dialog.addOpenedChangeListener(changeEvent -> {
            if (!changeEvent.isOpened()) {
                reloadGridItems();
            }
        });
        dialog.open();
    }

    private void deleteSpeaker(@NotNull final Long speakerId) {
        final var speaker = speakerService.get(speakerId);
        if (speaker.isPresent()) {
            new ConfirmDialog("Confirm deletion",
                    String.format("Are you sure you want to permanently delete the speaker \"%s\"?", getFullName(speaker.get())),
                    "Delete", (dialogEvent) -> {
                speakerService.delete(speaker.get());
                reloadGridItems();
                dialogEvent.getSource().close();
            },
                    "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
            ).open();
        } else {
            Notification.show("This speaker does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void reloadGridItems() {
        grid.setItems(query -> speakerService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
