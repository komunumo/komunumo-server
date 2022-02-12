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
import org.komunumo.data.db.tables.records.MailTemplateRecord;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

@CssImport(value = "./themes/komunumo/views/admin/mail-template-setting.css")
public class MailTemplateSetting extends ResizableView {

    private final DatabaseService databaseService;

    private final TextField filterField;
    private final Grid<MailTemplateRecord> grid;

    public MailTemplateSetting(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;

        addClassNames("mail-template-setting", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter mail templates");

        final var newMailTemplatesButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showEditDialog(null));
        newMailTemplatesButton.setTitle("Add a new mail template");

        final var refreshMailTemplatesButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshMailTemplatesButton.setTitle("Refresh the list of mail templates");

        final var downloadMailTemplatesButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadMailTemplates());
        downloadMailTemplatesButton.setTitle("Download the list of mail templates");

        final var optionBar = new HorizontalLayout(filterField, newMailTemplatesButton, refreshMailTemplatesButton, downloadMailTemplatesButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private Grid<MailTemplateRecord> createGrid() {
        final var grid = new Grid<MailTemplateRecord>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(MailTemplateRecord::getId)
                .setHeader("ID").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(MailTemplateRecord::getSubject)
                .setHeader("Subject").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(mailTemplateRecord -> {
                    final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEditDialog(mailTemplateRecord));
                    editButton.setTitle("Edit this mail template");
                    final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteMailTemplate(mailTemplateRecord));
                    deleteButton.setTitle("Delete this mail template");
                    return new HorizontalLayout(editButton, deleteButton);
                }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    private void showEditDialog(@Nullable final MailTemplateRecord mailTemplateRecord) {
        final var dialog = new MailTemplateDialog(mailTemplateRecord != null ? "Edit Mail Template" : "New Mail Template");
        dialog.open(mailTemplateRecord != null ? mailTemplateRecord : databaseService.newMailTemplate(), this::reloadGridItems);
    }

    private void deleteMailTemplate(@NotNull final MailTemplateRecord mailTemplateRecord) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the mail template \"%s\"?",
                        mailTemplateRecord.getId()),
                "Delete", dialogEvent -> {
            mailTemplateRecord.delete();
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> databaseService.findMailTemplate(query.getOffset(), query.getLimit(), filterField.getValue()));
        grid.recalculateColumnWidths();
    }

    private void downloadMailTemplates() {
        final var resource = new StreamResource("mail-templates.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] { "ID", "Subject", "Content text", "Content HTML" });
            grid.getGenericDataView()
                    .getItems().map(mailTemplateRecord -> new String[] {
                            mailTemplateRecord.getId(),
                            mailTemplateRecord.getSubject(),
                            mailTemplateRecord.getContentText(),
                            mailTemplateRecord.getContentHtml()
                    }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

}
