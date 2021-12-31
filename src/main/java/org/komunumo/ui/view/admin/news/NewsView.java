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

package org.komunumo.ui.view.admin.news;

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
import org.komunumo.data.entity.NewsEntity;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.NewsService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import javax.annotation.security.RolesAllowed;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Route(value = "admin/news", layout = AdminLayout.class)
@PageTitle("News Administration")
@CssImport(value = "./themes/komunumo/views/admin/news-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public class NewsView extends ResizableView implements HasUrlParameter<String> {

    private final NewsService newsService;
    private final TextField filterField;
    private final Grid<NewsEntity> grid;

    public NewsView(@NotNull final NewsService newsService) {
        this.newsService = newsService;

        addClassNames("news-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter news by name");

        final var newNewsButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showNewsDialog(null));
        newNewsButton.setTitle("Add news");

        final var refreshNewsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshNewsButton.setTitle("Refresh the list of news");

        final var downloadNewsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadNews());
        downloadNewsButton.setTitle("Download the list of news");

        final var optionBar = new HorizontalLayout(filterField, newNewsButton, refreshNewsButton, downloadNewsButton);
        optionBar.setPadding(true);

        add(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }


    @Override
    public void setParameter(@NotNull final BeforeEvent beforeEvent,
                             @Nullable @OptionalParameter String parameter) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var filterValue = parameters.getOrDefault("filter", List.of("")).get(0);
        filterField.setValue(filterValue);
    }

    private Grid<NewsEntity> createGrid() {
        final var grid = new Grid<NewsEntity>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<NewsEntity>of(
                "<span style=\"font-weight: bold;\">[[item.title]]</span><br/><span>[[item.subtitle]]</span>")
                .withProperty("title", NewsEntity::title)
                .withProperty("subtitle", NewsEntity::subtitle))
                .setHeader("Title").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(newsEntity -> formatDateTime(newsEntity.showFrom()))
                .setHeader("Show from")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setKey("showFrom");
        grid.addColumn(newsEntity -> formatDateTime(newsEntity.showTo()))
                .setHeader("Show to")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setKey("showTo");

        grid.addColumn(new ComponentRenderer<>(newsEntity -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showNewsDialog(newsEntity));
            editButton.setTitle("Edit news");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteNews(newsEntity));
            deleteButton.setTitle("Delete news");
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
        grid.getColumnByKey("showFrom").setVisible(width >= 1200);
        grid.getColumnByKey("showTo").setVisible(width >= 1000);
    }

    private void showNewsDialog(@Nullable final NewsEntity newsEntity) {
        final var newsRecord = newsEntity == null || newsEntity.id() == null ? newsService.newNews() :
                newsService.getNewsRecord(newsEntity.id()).orElse(newsService.newNews());
        final var dialog = new NewsDialog(newsRecord.getId() != null ? "Edit News" : "New News");
        dialog.open(newsRecord, this::reloadGridItems);
    }

    private void deleteNews(final NewsEntity newsEntity) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the news \"%s\"?", newsEntity.title()),
                "Delete", dialogEvent -> {
            newsService.deleteNews(newsEntity.id());
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> newsService.findNews(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadNews() {
        final var resource = new StreamResource("news.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "Title", "Subtitle", "Show from", "Show to"
            });
            grid.getGenericDataView()
                    .getItems().map(newsEntity -> new String[] {
                    newsEntity.id().toString(),
                    newsEntity.title(),
                    newsEntity.subtitle(),
                    formatDateTime(newsEntity.showFrom()),
                    formatDateTime(newsEntity.showTo())
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
