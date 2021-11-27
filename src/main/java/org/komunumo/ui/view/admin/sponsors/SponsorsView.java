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

import javax.annotation.security.RolesAllowed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Role;
import org.komunumo.data.entity.SponsorEntity;
import org.komunumo.data.service.SponsorService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.util.FormatterUtil.formatDate;

@Route(value = "admin/sponsors", layout = AdminLayout.class)
@PageTitle("Sponsor Administration")
@CssImport(value = "./themes/komunumo/views/admin/sponsors-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public class SponsorsView extends ResizableView implements HasUrlParameter<String> {

    private final SponsorService sponsorService;
    private final TextField filterField;
    private final Grid<SponsorEntity> grid;

    public SponsorsView(@NotNull final SponsorService sponsorService) {
        this.sponsorService = sponsorService;

        addClassNames("sponsors-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter sponsors by name");

        final var newSponsorButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showSponsorDialog(null));
        newSponsorButton.setTitle("Add a new sponsor");

        final var refreshSpeakersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshSpeakersButton.setTitle("Refresh the list of sponsors");

        final var downloadSponsorsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadSponsors());
        downloadSponsorsButton.setTitle("Download the list of sponsors");

        final var optionBar = new HorizontalLayout(filterField, newSponsorButton, refreshSpeakersButton, downloadSponsorsButton);
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

    private Grid<SponsorEntity> createGrid() {
        final var grid = new Grid<SponsorEntity>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<SponsorEntity>of(
                "<a style=\"font-weight: bold;\" href=\"[[item.website]]\" target=\"_blank\">[[item.name]]</a>")
                .withProperty("name", SponsorEntity::name)
                .withProperty("website", SponsorEntity::website))
                .setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(TemplateRenderer.<SponsorEntity>of(
                "<img style=\"max-width: 100%;\" src=\"[[item.logo]]\" /></span>")
                .withProperty("logo", SponsorEntity::logo))
                .setHeader("Logo").setWidth("96px").setFlexGrow(0);
        grid.addColumn(SponsorEntity::level)
                .setHeader("Level")
                .setAutoWidth(true);
        grid.addColumn(sponsorEntity -> formatDate(sponsorEntity.validFrom()))
                .setHeader("Valid from")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setKey("validFrom");
        grid.addColumn(sponsorEntity -> formatDate(sponsorEntity.validTo()))
                .setHeader("Valid to")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setKey("validTo");

        grid.addColumn(new ComponentRenderer<>(sponsorEntity -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showSponsorDialog(sponsorEntity));
            editButton.setTitle("Edit this sponsor");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteSponsor(sponsorEntity));
            deleteButton.setTitle("Delete this sponsor");
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
        grid.getColumnByKey("validFrom").setVisible(width >= 1200);
        grid.getColumnByKey("validTo").setVisible(width >= 1000);
    }

    private void showSponsorDialog(@Nullable final SponsorEntity sponsorEntity) {
        final var sponsorRecord = sponsorEntity == null || sponsorEntity.id() == null ? sponsorService.newSponsor() :
                sponsorService.getSponsorRecord(sponsorEntity.id()).orElse(sponsorService.newSponsor());
        final var dialog = new SponsorDialog(sponsorRecord.getId() != null ? "Edit Sponsor" : "New Sponsor");
        dialog.open(sponsorRecord, this::reloadGridItems);
    }

    private void deleteSponsor(final SponsorEntity sponsorEntity) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the sponsor \"%s\"?", sponsorEntity.name()),
                "Delete", dialogEvent -> {
            sponsorService.delete(sponsorEntity.id());
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> sponsorService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private void downloadSponsors() {
        final var resource = new StreamResource("sponsors.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "Name", "Website", "Level", "Valid from", "Valid to"
            });
            grid.getGenericDataView()
                    .getItems().map(sponsorEntity -> new String[] {
                    sponsorEntity.id().toString(),
                    sponsorEntity.name(),
                    sponsorEntity.website(),
                    sponsorEntity.level() != null ? sponsorEntity.level().getName() : "",
                    formatDate(sponsorEntity.validFrom()),
                    formatDate(sponsorEntity.validTo())
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }
}
