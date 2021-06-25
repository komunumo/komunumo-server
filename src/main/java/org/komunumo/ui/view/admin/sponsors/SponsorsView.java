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

package org.komunumo.ui.view.admin.sponsors;

import com.opencsv.CSVWriter;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.StreamResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.service.SponsorService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.view.admin.AdminLayout;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.data.db.tables.Sponsor.SPONSOR;

@Route(value = "admin/sponsors", layout = AdminLayout.class)
@PageTitle("Sponsor Administration")
public class SponsorsView extends Div implements HasUrlParameter<String> {

    private final SponsorService sponsorService;
    private final TextField filterField;
    private final Grid<Record> grid;

    public SponsorsView(@NotNull final SponsorService sponsorService) {
        this.sponsorService = sponsorService;

        addClassNames("sponsors-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter sponsors by name");

        final var newSponsorButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> newSponsor());
        newSponsorButton.setTitle("Add a new sponsor");

        final var refreshSpeakersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshSpeakersButton.setTitle("Refresh the list of sponsors");

        final var downloadSponsorsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD));
        downloadSponsorsButton.setTitle("Download the list of sponsors");
        final var downloadSponsorsButtonWrapper = new FileDownloadWrapper(downloadSponsors());
        downloadSponsorsButtonWrapper.wrapComponent(downloadSponsorsButton);

        final var optionBar = new HorizontalLayout(filterField, newSponsorButton, refreshSpeakersButton, downloadSponsorsButtonWrapper);
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

    private Grid<Record> createGrid() {
        final var grid = new Grid<Record>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<Record>of(
                "<a style=\"font-weight: bold;\" href=\"[[item.website]]\" target=\"_blank\">[[item.name]]</a>")
                .withProperty("name", record -> record.get(SPONSOR.NAME))
                .withProperty("website", record -> record.get(SPONSOR.WEBSITE)))
                .setHeader("Name").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<Record>of(
                "<img style=\"max-width: 100%;\" src=\"[[item.logo]]\" /></span>")
                .withProperty("logo", record -> record.get(SPONSOR.LOGO)))
                .setHeader("Logo").setWidth("96px").setFlexGrow(0);
        grid.addColumn(record -> record.get(SPONSOR.LEVEL)).setHeader("Level").setAutoWidth(true);
        grid.addColumn(record -> record.get(SPONSOR.VALID_FROM)).setHeader("Valid from").setAutoWidth(true).setKey("validFrom");
        grid.addColumn(record -> record.get(SPONSOR.VALID_TO)).setHeader("Valid to").setAutoWidth(true).setKey("validTo");

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), event -> editSponsor(record.get(SPONSOR.ID)));
            editButton.setTitle("Edit this sponsor");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), event -> deleteSponsor(record.get(SPONSOR.ID)));
            deleteButton.setTitle("Delete this sponsor");
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setFlexGrow(0)
                .setFrozen(true);

        grid.setHeightFull();

        final var page = UI.getCurrent().getPage();
        page.retrieveExtendedClientDetails(extendedClientDetails -> showHideGridColumns(grid, extendedClientDetails.getBodyClientWidth()));
        page.addBrowserWindowResizeListener(event -> showHideGridColumns(grid, event.getWidth()));

        return grid;
    }

    private void showHideGridColumns(@NotNull final Grid<Record> grid, final int clientWidth) {
        grid.getColumnByKey("validFrom").setVisible(clientWidth >= 1200);
        grid.getColumnByKey("validTo").setVisible(clientWidth >= 1000);
    }

    private void newSponsor() {
        showSponsorDialog(sponsorService.newSponsor());
    }

    private void editSponsor(@NotNull final Long sponsorId) {
        final var sponsor = sponsorService.get(sponsorId);
        if (sponsor.isPresent()) {
            showSponsorDialog(sponsor.get());
        } else {
            Notification.show("This sponsor does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void showSponsorDialog(@NotNull final SponsorRecord sponsor) {
        final var dialog = new SponsorDialog(sponsor, sponsorService);
        dialog.addOpenedChangeListener(changeEvent -> {
            if (!changeEvent.isOpened()) {
                reloadGridItems();
            }
        });
        dialog.open();
    }

    private void deleteSponsor(@NotNull final Long sponsorId) {
        final var sponsor = sponsorService.get(sponsorId);
        if (sponsor.isPresent()) {
            new ConfirmDialog("Confirm deletion",
                    String.format("Are you sure you want to permanently delete the sponsor \"%s\"?", sponsor.get().getName()),
                    "Delete", (dialogEvent) -> {
                sponsorService.delete(sponsor.get());
                reloadGridItems();
                dialogEvent.getSource().close();
            },
                    "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
            ).open();
        } else {
            Notification.show("This sponsor does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void reloadGridItems() {
        grid.setItems(query -> sponsorService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private StreamResource downloadSponsors() {
        return new StreamResource("sponsors.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "Name", "Website", "Level", "Valid from", "Valid to"
            });
            grid.getGenericDataView()
                    .getItems().map(record -> new String[] {
                    record.get(SPONSOR.ID).toString(),
                    record.get(SPONSOR.NAME),
                    record.get(SPONSOR.WEBSITE),
                    record.get(SPONSOR.LEVEL) != null ? record.get(SPONSOR.LEVEL).toString() : "",
                    record.get(SPONSOR.VALID_FROM) != null ? record.get(SPONSOR.VALID_FROM).toString() : "",
                    record.get(SPONSOR.VALID_TO) != null ? record.get(SPONSOR.VALID_TO).toString() : ""
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
    }
}
