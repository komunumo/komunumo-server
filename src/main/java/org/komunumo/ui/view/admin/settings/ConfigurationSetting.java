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
import org.komunumo.data.db.tables.records.ConfigurationRecord;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

@CssImport(value = "./themes/komunumo/views/admin/configuration-setting.css")
public class ConfigurationSetting extends ResizableView {

    private final DatabaseService databaseService;

    private final TextField filterField;
    private final Grid<ConfigurationRecord> grid;

    public ConfigurationSetting(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;

        addClassNames("configuration-setting", "flex", "flex-col", "h-full");

        grid = new Grid<>();
        configureGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter configuration");

        final var newButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showEditDialog(null));
        newButton.setTitle("Add a new configuration setting");

        final var refreshButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshButton.setTitle("Refresh the list of configuration settings");

        final var downloadButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadConfigurations());
        downloadButton.setTitle("Download the list of configuration settings");

        final var optionBar = new HorizontalLayout(filterField, newButton, refreshButton, downloadButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private void configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(ConfigurationRecord::getConfKey)
                .setHeader("Key").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(ConfigurationRecord::getConfValue)
                .setHeader("Value").setAutoWidth(false).setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(configurationRecord -> {
                    final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEditDialog(configurationRecord));
                    editButton.setTitle("Edit this configuration setting");
                    final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteConfiguration(configurationRecord));
                    deleteButton.setTitle("Delete this configuration setting");
                    return new HorizontalLayout(editButton, deleteButton);
                }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();
    }

    private void showEditDialog(@Nullable final ConfigurationRecord configurationRecord) {
        final var dialog = new ConfigurationDialog(configurationRecord != null ? "Edit Configuration Setting" : "New ConfigurationSetting",
                databaseService);
        dialog.open(configurationRecord != null ? configurationRecord : databaseService.newConfiguration(), this::reloadGridItems);
    }

    private void deleteConfiguration(@NotNull final ConfigurationRecord configurationRecord) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the configuration setting \"%s\"?", configurationRecord.getConfKey()),
                "Delete", dialogEvent -> {
            configurationRecord.delete();
            databaseService.reloadConfiguration();
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> databaseService.findConfiguration(query.getOffset(), query.getLimit(), filterField.getValue()));
        grid.recalculateColumnWidths();
    }

    private void downloadConfigurations() {
        final var resource = new StreamResource("configurations.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "Key", "Value"
            });
            grid.getGenericDataView()
                    .getItems().map(configurationRecord -> new String[] {
                            configurationRecord.getConfKey(),
                            configurationRecord.getConfValue()
                    }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

}
