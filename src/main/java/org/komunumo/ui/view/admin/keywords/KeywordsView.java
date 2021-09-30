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

package org.komunumo.ui.view.admin.keywords;

import com.opencsv.CSVWriter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.KeywordRecord;
import org.komunumo.data.entity.Keyword;
import org.komunumo.data.service.KeywordService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;

@Route(value = "admin/keywords", layout = AdminLayout.class)
@PageTitle("Keyword Administration")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
public class KeywordsView extends ResizableView {

    private final KeywordService keywordService;
    private final TextField filterField;
    private final Grid<Keyword> grid;

    public KeywordsView(@NotNull final KeywordService keywordService) {
        this.keywordService = keywordService;

        addClassNames("keywords-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter keywords");

        final var newKeywordButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> newKeyword());
        newKeywordButton.setTitle("Add a new keyword");

        final var refreshKeywordsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshKeywordsButton.setTitle("Refresh the list of keywords");

        final var downloadKeywordsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), event -> downloadKeywords());
        downloadKeywordsButton.setTitle("Download the list of keywords");

        final var optionBar = new HorizontalLayout(filterField, newKeywordButton, refreshKeywordsButton, downloadKeywordsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private Grid<Keyword> createGrid() {
        final var grid = new Grid<Keyword>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Keyword::getKeyword)
                .setHeader("Keyword").setAutoWidth(true).setFlexGrow(0).setKey("keyword");

        grid.addColumn(Keyword::getEventCount)
                .setHeader("Events").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(keyword -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), event -> editKeyword(keyword.getId()));
            editButton.setTitle("Edit this keyword");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), event -> deleteKeyword(keyword.getId()));
            deleteButton.setTitle("Delete this keyword");
            deleteButton.setEnabled(keyword.getEventCount() == 0);
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    private void newKeyword() {
        showKeywordDialog(keywordService.newKeyword());
    }

    private void editKeyword(@NotNull final Long keywordId) {
        final var keyword = keywordService.get(keywordId);
        if (keyword.isPresent()) {
            showKeywordDialog(keyword.get());
        } else {
            Notification.show("This keyword does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void showKeywordDialog(@NotNull final KeywordRecord keyword) {
        final var dialog = new KeywordDialog(keyword.get(KEYWORD.ID) != null ? "Edit Keyword" : "New Keyword");
        dialog.open(keyword, this::reloadGridItems);
    }

    private void deleteKeyword(@NotNull final Long keywordId) {
        final var keyword = keywordService.get(keywordId);
        if (keyword.isPresent()) {
            new ConfirmDialog("Confirm deletion",
                    String.format("Are you sure you want to permanently delete the keyword \"%s\"?", keyword.get()),
                    "Delete", dialogEvent -> {
                keywordService.delete(keyword.get());
                reloadGridItems();
                dialogEvent.getSource().close();
            },
                    "Cancel", dialogEvent -> dialogEvent.getSource().close()
            ).open();
        } else {
            Notification.show("This keyword does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void reloadGridItems() {
        grid.setItems(query -> keywordService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadKeywords() {
        final var resource = new StreamResource("keywords.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] { "ID", "Keyword", "Event count" });
            grid.getGenericDataView()
                    .getItems().map(keyword -> new String[] {
                            keyword.getId().toString(),
                            keyword.getKeyword(),
                            Long.toString(keyword.getEventCount())
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
