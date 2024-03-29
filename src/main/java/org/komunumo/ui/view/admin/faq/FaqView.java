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

package org.komunumo.ui.view.admin.faq;

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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.tables.records.FaqRecord;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "admin/faq", layout = AdminLayout.class)
@PageTitle("FAQ Administration")
@CssImport(value = "./themes/komunumo/views/admin/faq-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public class FaqView extends ResizableView {

    private final DatabaseService databaseService;
    private final TextField filterField;
    private final Grid<FaqRecord> grid;

    public FaqView(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;

        addClassNames("faq-view", "flex", "flex-col", "h-full");

        grid = new Grid<>();
        configureGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter FAQ list");

        final var newEntryButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showEditDialog(null));
        newEntryButton.setTitle("Add a new entry");

        final var refreshListButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshListButton.setTitle("Refresh the FAQ list");

        final var downloadEntriesButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadEntries());
        downloadEntriesButton.setTitle("Download the list of entries");

        final var optionBar = new HorizontalLayout(filterField, newEntryButton, refreshListButton, downloadEntriesButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private void configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(FaqRecord::getQuestion)
                .setHeader("Question").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(faqRecord -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEditDialog(faqRecord));
            editButton.setTitle("Edit this entry");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteEntry(faqRecord));
            deleteButton.setTitle("Delete this entry");
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();
    }

    private void showEditDialog(@Nullable final FaqRecord faqRecord) {
        final var dialog = new FaqDialog(faqRecord != null ? "Edit FAQ Entry" : "New FAQ Entry");
        dialog.open(faqRecord != null ? faqRecord : databaseService.newFaqRecord(), this::reloadGridItems);
    }

    private void deleteEntry(@NotNull final FaqRecord faqRecord) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the FAQ entry \"%s\"?", faqRecord.getQuestion()),
                "Delete", dialogEvent -> {
            faqRecord.delete();
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> databaseService.findFaqRecords(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadEntries() {
        final var resource = new StreamResource("faq.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "Question", "Answer"
            });
            grid.getGenericDataView()
                    .getItems().map(faqRecord -> new String[] {
                            faqRecord.getId().toString(),
                            faqRecord.getQuestion(),
                            faqRecord.getAnswer()
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

}
