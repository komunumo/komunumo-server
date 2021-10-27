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
import org.komunumo.data.entity.Speaker;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;
import org.komunumo.util.FormatterUtil;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "admin/speakers", layout = AdminLayout.class)
@PageTitle("Speaker Administration")
@CssImport(value = "./themes/komunumo/views/admin/speakers-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
public class SpeakersView extends ResizableView implements HasUrlParameter<String> {

    private final SpeakerService speakerService;
    private final TextField filterField;
    private final Grid<Speaker> grid;

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
                             @Nullable @OptionalParameter String parameter) {
        final var location = event.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private Grid<Speaker> createGrid() {
        final var grid = new Grid<Speaker>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<Speaker>of("<span style=\"font-weight: bold;\">[[item.firstName]] [[item.lastName]]</span><br/><a href=\"[[item.website]]\" target=\"_blank\" title=\"[[item.title]]\">[[item.company]]</a>")
                .withProperty("firstName", Speaker::getFirstName)
                .withProperty("lastName", Speaker::getLastName)
                .withProperty("company", speaker -> FormatterUtil.formatString(speaker.getCompany(), 50))
                .withProperty("title", Speaker::getCompany)
                .withProperty("website", Speaker::getWebsite))
                .setHeader("Name & Company").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(TemplateRenderer.<Speaker>of("<a href=\"mailto:[[item.email]]\" target=\"_blank\">[[item.email]]</a>")
                .withProperty("email", Speaker::getEmail))
                .setHeader("Email").setAutoWidth(true).setFlexGrow(0).setKey("email");
        grid.addColumn(TemplateRenderer.<Speaker>of("<a href=\"https://twitter.com/[[item.twitter]]\" target=\"_blank\">[[item.twitter]]</a>")
                .withProperty("twitter", Speaker::getTwitter))
                .setHeader("Twitter").setAutoWidth(true).setFlexGrow(0).setKey("twitter");

        final var eventCountRenderer = TemplateRenderer.<Speaker>of(
                "<a href=\"/admin/events?filter=[[item.filterValue]]\">[[item.eventCount]]</a>")
                .withProperty("eventCount", Speaker::getEventCount)
                .withProperty("filterValue", speaker -> URLEncoder.encode(speaker.getFullName(), UTF_8));
        grid.addColumn(eventCountRenderer).setHeader("Events").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(speaker -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showSpeakerDialog(speaker));
            editButton.setTitle("Edit this speaker");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteSpeaker(speaker));
            deleteButton.setTitle("Delete this speaker");
            deleteButton.setEnabled(speaker.getEventCount() == 0);
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    @Override
    protected void onResize(final int width) {
        grid.getColumnByKey("twitter").setVisible(width >= 1300);
        grid.getColumnByKey("email").setVisible(width >= 1200);
    }

    private void newSpeaker() {
        showSpeakerDialog(speakerService.newSpeaker());
    }

    private void showSpeakerDialog(@NotNull final Speaker speaker) {
        final var dialog = new SpeakerDialog(speaker.getId() != null ? "Edit Speaker" : "New Speaker");
        dialog.open(speaker, this::reloadGridItems);
    }

    private void deleteSpeaker(final Speaker speaker) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the speaker \"%s\"?", speaker.getFullName()),
                "Delete", dialogEvent -> {
            speakerService.delete(speaker);
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> speakerService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadSpeakers() {
        final var resource = new StreamResource("speakers.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "First name", "Last name", "Company", "Bio",
                    "Email", "Twitter", "LinkedIn", "Website",
                    "Address", "Zip code", "City", "State", "Country",
                    "Event count"
            });
            grid.getGenericDataView()
                    .getItems().map(speaker -> new String[] {
                    speaker.getId().toString(),
                    speaker.getFirstName(),
                    speaker.getLastName(),
                    speaker.getCompany(),
                    speaker.getBio(),
                    speaker.getEmail(),
                    speaker.getTwitter(),
                    speaker.getLinkedin(),
                    speaker.getWebsite(),
                    speaker.getAddress(),
                    speaker.getZipCode(),
                    speaker.getCity(),
                    speaker.getState(),
                    speaker.getCountry(),
                    Long.toString(speaker.getEventCount())
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
