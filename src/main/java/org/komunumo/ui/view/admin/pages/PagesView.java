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

package org.komunumo.ui.view.admin.pages;

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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Page;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import javax.annotation.security.RolesAllowed;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.ui.view.website.members.MembersView;
import org.komunumo.ui.view.website.sponsors.SponsorsView;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "admin/pages", layout = AdminLayout.class)
@PageTitle("Pages Administration")
@CssImport(value = "./themes/komunumo/views/admin/pages-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public final class PagesView extends ResizableView implements HasUrlParameter<String> {

    private final DatabaseService databaseService;
    private final TextField filterField;
    private final Grid<Page> grid;

    public PagesView(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;

        addClassNames("news-view", "flex", "flex-col", "h-full");

        grid = new Grid<>();
        configureGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter pages");

        final var addButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showEditDialog(null));
        addButton.setTitle("Add page");

        final var refreshButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshButton.setTitle("Refresh the list of pages");

        final var downloadButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadPages());
        downloadButton.setTitle("Download the list of pages");

        final var optionBar = new HorizontalLayout(filterField, addButton, refreshButton, downloadButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }


    @Override
    public void setParameter(@NotNull final BeforeEvent beforeEvent,
                             @Nullable @OptionalParameter final String parameter) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private void configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Page::getParent)
                .setHeader("Parent").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Page::getPageUrl)
                .setHeader("URL").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Page::getTitle)
                .setHeader("Title").setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(page -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showEditDialog(page));
            editButton.setTitle("Edit page");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deletePage(page));
            deleteButton.setTitle("Delete page");
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();
    }

    private void showEditDialog(@Nullable final Page page) {
        final var oldURL = page != null ? page.getCompletePageUrl() : null;
        final var dialog = new PageDialog(page != null ? "Edit Page" : "New Page");
        final var pageToEdit = page != null ? page : databaseService.newPage();
        dialog.open(pageToEdit, () -> afterSave(oldURL, pageToEdit));
    }

    private void afterSave(@Nullable final String oldURL, @NotNull final Page page) {
        reloadGridItems();
        if (!page.getCompletePageUrl().equals(oldURL)) {
            if (oldURL != null) {
                RouteConfiguration.forApplicationScope().removeRoute(oldURL);
            }
            final var clazz = switch (page.getParent()) {
                case Members -> MembersView.class;
                case Sponsors -> SponsorsView.class;
            };
            RouteConfiguration.forApplicationScope().setRoute(
                    page.getCompletePageUrl(), clazz, List.of(WebsiteLayout.class));
        }
    }

    private void deletePage(final Page page) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the page \"%s\"?", page.getTitle()),
                "Delete", dialogEvent -> {
            RouteConfiguration.forApplicationScope().removeRoute(page.getCompletePageUrl());
            page.delete();
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> databaseService.findPages(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadPages() {
        final var resource = new StreamResource("pages.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "Parent", "Page URL", "Title", "Content"
            });
            grid.getGenericDataView()
                    .getItems().map(page -> new String[] {
                    page.getId().toString(),
                    page.getParent().getLiteral(),
                    page.getPageUrl(),
                    page.getTitle(),
                    page.getContent()
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
