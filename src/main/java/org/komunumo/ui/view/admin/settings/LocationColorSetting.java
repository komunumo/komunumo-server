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

package org.komunumo.ui.view.admin.settings;

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
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.tables.records.LocationColorRecord;
import org.komunumo.data.service.LocationColorService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

@CssImport(value = "./themes/komunumo/views/admin/location-color-setting.css")
public class LocationColorSetting extends ResizableView {

    private final LocationColorService locationColorService;
    private final TextField filterField;
    private final Grid<LocationColorRecord> grid;

    public LocationColorSetting(@NotNull final LocationColorService locationColorService) {
        this.locationColorService = locationColorService;

        addClassNames("location-color-setting", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter location");

        final var newKeywordButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showLocationColorDialog(null));
        newKeywordButton.setTitle("Add a new location color setting");

        final var refreshKeywordsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshKeywordsButton.setTitle("Refresh the list of keywords");

        final var downloadKeywordsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadLocationColors());
        downloadKeywordsButton.setTitle("Download the list of keywords");

        final var optionBar = new HorizontalLayout(filterField, newKeywordButton, refreshKeywordsButton, downloadKeywordsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private Grid<LocationColorRecord> createGrid() {
        final var grid = new Grid<LocationColorRecord>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(LocationColorRecord::getLocation)
                .setHeader("Location").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(
                        locationColorRecord -> {
                            final var icon = new Icon(VaadinIcon.CIRCLE);
                            icon.setSize("16px");
                            icon.setColor(locationColorRecord.getColor());
                            icon.getElement().setAttribute("title", locationColorRecord.getColor());
                            return icon;
                        })).setHeader("Color")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(locationColorRecord -> {
                    final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showLocationColorDialog(locationColorRecord));
                    editButton.setTitle("Edit this location color setting");
                    final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteLocationColor(locationColorRecord));
                    deleteButton.setTitle("Delete this location color setting");
                    return new HorizontalLayout(editButton, deleteButton);
                }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    private void showLocationColorDialog(@Nullable final LocationColorRecord locationColorRecord) {
        final var dialog = new LocationColorDialog(locationColorRecord != null ? "Edit Keyword" : "New Keyword");
        dialog.open(locationColorRecord != null ? locationColorRecord : locationColorService.newRecord(), this::reloadGridItems);
    }

    private void deleteLocationColor(@NotNull final LocationColorRecord locationColorRecord) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the color for the location \"%s\"?", locationColorRecord.getLocation()),
                "Delete", dialogEvent -> {
            locationColorRecord.delete();
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> locationColorService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadLocationColors() {
        final var resource = new StreamResource("location-colors.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] { "Location", "Color" });
            grid.getGenericDataView()
                    .getItems().map(locationColorRecord -> new String[] {
                            locationColorRecord.getLocation(),
                            locationColorRecord.getColor()
                    }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

}
