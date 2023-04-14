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

import com.opencsv.CSVWriter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import org.komunumo.data.entity.Role;
import org.komunumo.data.entity.SpeakerListEntity;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;
import org.komunumo.util.FormatterUtil;

import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "admin/speakers", layout = AdminLayout.class)
@PageTitle("Speaker Administration")
@CssImport(value = "./themes/komunumo/views/admin/speakers-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public final class SpeakersView extends ResizableView implements HasUrlParameter<String> {

    @Serial
    private static final long serialVersionUID = 8054964269047997691L;
    private final DatabaseService databaseService;
    private final TextField filterField;
    private final Grid<SpeakerListEntity> grid;

    public SpeakersView(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;

        addClassNames("speakers-view", "flex", "flex-col", "h-full");

        grid = new Grid<>();
        configureGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter speakers by name, company, email, or twitter");

        final var newSpeakerButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> showSpeakerDialog(null));
        newSpeakerButton.setTitle("Add a new speaker");

        final var refreshSpeakersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshSpeakersButton.setTitle("Refresh the list of speakers");

        final var downloadSpeakersButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), event -> downloadSpeakers());
        downloadSpeakersButton.setTitle("Download the list of speakers");

        final var optionBar = new HorizontalLayout(filterField, newSpeakerButton, refreshSpeakersButton, downloadSpeakersButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    @Override
    public void setParameter(@NotNull final BeforeEvent event,
                             @Nullable @OptionalParameter final String parameter) {
        final var location = event.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private void configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(LitRenderer.<SpeakerListEntity>of(
                "<span style=\"font-weight: bold;\">${item.fullName}</span><br/>"
                        + "<a href=\"${item.website}\" target=\"_blank\" title=\"${item.title}\">${item.company}</a>")
                .withProperty("fullName", SpeakerListEntity::fullName)
                .withProperty("company", speakerListEntity -> FormatterUtil.formatString(speakerListEntity.company(), 50))
                .withProperty("title", SpeakerListEntity::company)
                .withProperty("website", SpeakerListEntity::website))
                .setHeader("Name & Company").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(LitRenderer.<SpeakerListEntity>of(
                "<a href=\"mailto:${item.email}\" target=\"_blank\">${item.email}</a>")
                .withProperty("email", SpeakerListEntity::email))
                .setHeader("Email").setAutoWidth(true).setFlexGrow(0).setKey("email");
        grid.addColumn(LitRenderer.<SpeakerListEntity>of(
                "<a href=\"https://twitter.com/${item.twitter}\" target=\"_blank\" title=\"${item.twitter}\">${item.twitter}</a>")
                .withProperty("twitter", SpeakerListEntity::twitter))
                .setHeader("Twitter").setAutoWidth(true).setFlexGrow(0).setKey("twitter");

        final var eventCountRenderer = LitRenderer.<SpeakerListEntity>of(
                "<a href=\"/admin/events?filter=${item.filterValue}\">${item.eventCount}</a>")
                .withProperty("eventCount", SpeakerListEntity::eventCount)
                .withProperty("filterValue", speakerListEntity -> URLEncoder.encode(speakerListEntity.fullName(), UTF_8));
        grid.addColumn(eventCountRenderer).setHeader("Events").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(speakerListEntity -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showSpeakerDialog(speakerListEntity));
            editButton.setTitle("Edit this speaker");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteSpeaker(speakerListEntity));
            deleteButton.setTitle("Delete this speaker");
            deleteButton.setEnabled(speakerListEntity.eventCount() == 0);
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();
    }

    @Override
    protected void onResize(final int width) {
        grid.getColumnByKey("twitter").setVisible(width >= 1300);
        grid.getColumnByKey("email").setVisible(width >= 1200);
    }

    private void showSpeakerDialog(@Nullable final SpeakerListEntity speakerListEntity) {
        final var speakerRecord = speakerListEntity == null || speakerListEntity.id() == null ? databaseService.newSpeaker()
                : databaseService.getSpeakerRecord(speakerListEntity.id()).orElse(databaseService.newSpeaker());
        final var dialog = new SpeakerDialog(speakerRecord.getId() != null ? "Edit Speaker" : "New Speaker");
        dialog.open(speakerRecord, this::reloadGridItems);
    }

    private void deleteSpeaker(final SpeakerListEntity speakerListEntity) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the speaker \"%s\"?", speakerListEntity.fullName()),
                "Delete", dialogEvent -> {
            databaseService.deleteSpeaker(speakerListEntity.id());
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> databaseService.findSpeakers(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadSpeakers() {
        final var resource = new StreamResource("speakers.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "First name", "Last name", "Company",
                    "Email", "Twitter", "Website", "Event count"
            });
            grid.getGenericDataView()
                    .getItems().map(speakerListEntity -> new String[] {
                    speakerListEntity.id().toString(),
                    speakerListEntity.firstName(),
                    speakerListEntity.lastName(),
                    speakerListEntity.company(),
                    speakerListEntity.email(),
                    speakerListEntity.twitter(),
                    speakerListEntity.website(),
                    speakerListEntity.eventCount().toString()
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
