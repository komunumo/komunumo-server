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

package org.komunumo.views.admin.speakers;

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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Speaker;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.views.admin.AdminView;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "admin/speakers/", layout = AdminView.class)
@PageTitle("Speaker Administration")
public class SpeakersView extends Div {

    private final SpeakerService speakerService;

    private final TextField filterField;
    private final Grid<Speaker> grid;

    public SpeakersView(@NotNull final SpeakerService speakerService) {
        this.speakerService = speakerService;

        addClassNames("speakers-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = createFilter();

        final var newSpeakerButton = new Button(new Icon(VaadinIcon.FILE_ADD), event -> editSpeaker(speakerService.newSpeaker()));
        final var refreshSpeakersButton = new Button(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        final var optionBar = new HorizontalLayout(filterField, newSpeakerButton, refreshSpeakersButton);
        optionBar.setPadding(true);

        add(optionBar, grid);

        reloadGridItems();
    }

    private TextField createFilter() {
        final var filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.focus();
        filter.addValueChangeListener(event -> reloadGridItems());
        return filter;
    }

    private Grid<Speaker> createGrid() {
        final var grid = new Grid<Speaker>();
        grid.addColumn(Speaker::getFirstName).setHeader("First name").setAutoWidth(true);
        grid.addColumn(Speaker::getLastName).setHeader("Last name").setAutoWidth(true);
        grid.addColumn(Speaker::getCompany).setHeader("Company").setAutoWidth(true);
        grid.addColumn(Speaker::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Speaker::getTwitter).setHeader("Twitter").setAutoWidth(true);

        final var eventCountRenderer = TemplateRenderer.<Speaker>of(
                "<a href=\"/admin/events?filter=[[item.filterValue]]\">[[item.eventCount]]</a>")
                .withProperty("eventCount", Speaker::getEventCount)
                .withProperty("filterValue", (speaker) -> URLEncoder.encode(speaker.getFullName(), UTF_8));
        grid.addColumn(eventCountRenderer).setHeader("Events").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new Button(new Icon(VaadinIcon.EDIT), event -> editSpeaker(record));
            final var deleteButton = new Button(new Icon(VaadinIcon.TRASH), event -> deleteSpeaker(record));
            deleteButton.setEnabled(record.getEventCount() == 0);
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setFlexGrow(0)
                .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private void editSpeaker(@NotNull final Speaker speaker) {
        final var dialog = new SpeakerDialog(speaker, speakerService);
        dialog.addOpenedChangeListener(event -> { if (!event.isOpened()) { reloadGridItems(); } } );
        dialog.open();
    }

    private void deleteSpeaker(@NotNull final Speaker speaker) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the speaker \"%s\"?", speaker.getFullName()),
                "Delete", (dialogEvent) -> {
                    speakerService.delete(speaker);
                    reloadGridItems();
                    dialogEvent.getSource().close();
                },
                "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> speakerService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }
}
