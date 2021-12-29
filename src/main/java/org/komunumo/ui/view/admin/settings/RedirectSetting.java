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
import org.komunumo.ApplicationServiceInitListener;
import org.komunumo.data.db.tables.records.RedirectRecord;
import org.komunumo.data.service.RedirectService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

@CssImport(value = "./themes/komunumo/views/admin/redirect-setting.css")
public class RedirectSetting extends ResizableView {

    private final RedirectService redirectService;
    private final ApplicationServiceInitListener applicationServiceInitListener;

    private final TextField filterField;
    private final Grid<RedirectRecord> grid;

    public RedirectSetting(@NotNull final RedirectService redirectService,
                           @NotNull final ApplicationServiceInitListener applicationServiceInitListener) {
        this.redirectService = redirectService;
        this.applicationServiceInitListener = applicationServiceInitListener;

        addClassNames("redirect-setting", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter redirect");

        final var newRedirectButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showEditDialog(null));
        newRedirectButton.setTitle("Add a new redirect");

        final var refreshRedirectsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshRedirectsButton.setTitle("Refresh the list of redirects");

        final var downloadRedirectsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadRedirects());
        downloadRedirectsButton.setTitle("Download the list of redirects");

        final var optionBar = new HorizontalLayout(filterField, newRedirectButton, refreshRedirectsButton, downloadRedirectsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private Grid<RedirectRecord> createGrid() {
        final var grid = new Grid<RedirectRecord>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(RedirectRecord::getOldUrl)
                .setHeader("Old URL").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(RedirectRecord::getNewUrl)
                .setHeader("New URL").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(redirectRecord -> {
                    final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEditDialog(redirectRecord));
                    editButton.setTitle("Edit this location color setting");
                    final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteRedirect(redirectRecord));
                    deleteButton.setTitle("Delete this location color setting");
                    return new HorizontalLayout(editButton, deleteButton);
                }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    private void showEditDialog(@Nullable final RedirectRecord redirectRecord) {
        final var dialog = new RedirectDialog(redirectRecord != null ? "Edit Redirect" : "New Redirect", applicationServiceInitListener);
        dialog.open(redirectRecord != null ? redirectRecord : redirectService.newRedirect(), this::reloadGridItems);
    }

    private void deleteRedirect(@NotNull final RedirectRecord redirectRecord) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the redirect from \"%s\" to \"%s\"?",
                        redirectRecord.getOldUrl(), redirectRecord.getNewUrl()),
                "Delete", dialogEvent -> {
            redirectRecord.delete();
            applicationServiceInitListener.reloadRedirects();
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> redirectService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
        grid.recalculateColumnWidths();
    }

    private void downloadRedirects() {
        final var resource = new StreamResource("redirects.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] { "Old URL", "New URL" });
            grid.getGenericDataView()
                    .getItems().map(redirectRecord -> new String[] {
                            redirectRecord.getOldUrl(),
                            redirectRecord.getNewUrl()
                    }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

}
