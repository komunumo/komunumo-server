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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.service.SponsorService;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.view.admin.AdminView;

import java.util.List;

@Route(value = "admin/sponsors", layout = AdminView.class)
@PageTitle("Sponsor Administration")
public class SponsorsView extends Div implements HasUrlParameter<String> {

    private final SponsorService sponsorService;
    private final TextField filterField;
    private final Grid<SponsorRecord> grid;

    public SponsorsView(@NotNull final SponsorService sponsorService) {
        this.sponsorService = sponsorService;

        addClassNames("sponsors-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());

        final var newSponsorButton = new Button(new Icon(VaadinIcon.FILE_ADD), event -> editSponsor(sponsorService.newSponsor()));
        final var refreshSpeakersButton = new Button(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        final var optionBar = new HorizontalLayout(filterField, newSponsorButton, refreshSpeakersButton);
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

    private Grid<SponsorRecord> createGrid() {
        final var grid = new Grid<SponsorRecord>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(TemplateRenderer.<SponsorRecord>of(
                "<a style=\"font-weight: bold;\" href=\"[[item.website]]\" target=\"_blank\">[[item.name]]</a>")
                .withProperty("name", SponsorRecord::getName)
                .withProperty("website", SponsorRecord::getWebsite))
                .setHeader("Name").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<SponsorRecord>of(
                "<img style=\"max-width: 100%;\" src=\"[[item.logo]]\" /></span>")
                .withProperty("logo", SponsorRecord::getLogo))
                .setHeader("Logo").setWidth("96px").setFlexGrow(0);
        grid.addColumn(SponsorRecord::getLevel).setHeader("Level").setAutoWidth(true);
        grid.addColumn(SponsorRecord::getValidFrom).setHeader("Valid from").setAutoWidth(true);
        grid.addColumn(SponsorRecord::getValidTo).setHeader("Valid to").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new Button(new Icon(VaadinIcon.EDIT), event -> editSponsor(record));
            final var deleteButton = new Button(new Icon(VaadinIcon.TRASH), event -> deleteSponsor(record));
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setFlexGrow(0)
                .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private void editSponsor(@NotNull final SponsorRecord sponsor) {
        final var dialog = new SponsorDialog(sponsor, sponsorService);
        dialog.addOpenedChangeListener(changeEvent -> { if (!changeEvent.isOpened()) { reloadGridItems(); } } );
        dialog.open();
    }

    private void deleteSponsor(@NotNull final SponsorRecord sponsor) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the sponsor \"%s\"?", sponsor.getName()),
                "Delete", (dialogEvent) -> {
                    sponsorService.delete(sponsor);
                    reloadGridItems();
                    dialogEvent.getSource().close();
                },
                "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> sponsorService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
