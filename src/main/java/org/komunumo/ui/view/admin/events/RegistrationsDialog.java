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

package org.komunumo.ui.view.admin.events;

import com.opencsv.CSVWriter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.Callback;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.RegistrationListEntity;
import org.komunumo.data.entity.reports.RegistrationListEntityWrapper;
import org.komunumo.data.service.RegistrationService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.EnhancedDialog;
import org.komunumo.ui.component.FilterField;
import org.komunumo.util.FormatterUtil;
import org.vaadin.reports.PrintPreviewReport;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@CssImport(value = "./themes/komunumo/views/admin/registration-dialog.css")
public class RegistrationsDialog extends EnhancedDialog {

    private final RegistrationService registrationService;
    private final Event event;
    private final Callback afterChangeCallback;
    private final TextField filterField;
    private final Grid<RegistrationListEntity> grid;

    public RegistrationsDialog(@NotNull final RegistrationService registrationService,
                               @NotNull final Event event,
                               @Nullable final Callback afterChangeCallback) {
        super("Event registrations for \"%s\"".formatted(event.getTitle()));
        this.registrationService = registrationService;
        this.event = event;
        this.afterChangeCallback = afterChangeCallback;

        setWidth("800px");
        setHeight("600px");
        setResizable(true);

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(valueChangeEvent -> reloadGridItems());
        filterField.setTitle("Filter registrations");

        final var newRegistrationButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> showRegisterDialog());
        newRegistrationButton.setTitle("Add a new registration");
        newRegistrationButton.setEnabled(event.getDate() != null && event.getDate().isAfter(LocalDateTime.now()));

        final var refreshRegistrationsButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshRegistrationsButton.setTitle("Refresh the list of registrations");

        final var downloadRegistrationsButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadRegistrations());
        downloadRegistrationsButton.setTitle("Download the list of registrations");

        final var printRegistrationsButton = new EnhancedButton(new Icon(VaadinIcon.PRINT), clickEvent -> printRegistrations());
        printRegistrationsButton.setTitle("Print the list of registrations (generates a PDF)");

        final var optionBar = new HorizontalLayout(filterField, newRegistrationButton, refreshRegistrationsButton,
                downloadRegistrationsButton, printRegistrationsButton);
        optionBar.setPadding(true);

        addToContent(optionBar, grid);
        reloadGridItems();
        filterField.focus();
    }

    private Grid<RegistrationListEntity> createGrid() {
        final var grid = new Grid<RegistrationListEntity>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("registration-dialog");

        grid.addColumn(new ComponentRenderer<>(registrationListEntity ->
                        new Anchor("mailto:" + registrationListEntity.email(), registrationListEntity.fullName())))
                .setHeader("Attendee").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new LocalDateTimeRenderer<>(RegistrationListEntity::date, FormatterUtil.dateTimeFormatter()))
                .setHeader("Registration date").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(RegistrationListEntity::source)
                .setHeader("Source").setAutoWidth(true).setFlexGrow(0);

        if (event.getDate().isBefore(LocalDateTime.now())) {
            grid.addColumn(new ComponentRenderer<>(registrationListEntity ->
                            new Icon(registrationListEntity.noShow() ? VaadinIcon.CLOSE_CIRCLE_O : VaadinIcon.CHECK_CIRCLE_O)))
                    .setHeader("No show")
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setAutoWidth(true)
                    .setFlexGrow(0);
        }

        grid.addColumn(new ComponentRenderer<>(registrationListEntity -> {
                    final var deregisterButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deregister(registrationListEntity));
                    deregisterButton.setTitle("Deregister this attendee");
                    deregisterButton.setEnabled(event.getDate() != null && event.getDate().isAfter(LocalDateTime.now()));
                    return deregisterButton;
                }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setWidthFull();
        grid.setHeightFull();

        return grid;
    }

    private void reloadGridItems() {
        grid.setItems(query -> registrationService.find(event.getId(), query.getOffset(), query.getLimit(), filterField.getValue()));
        grid.recalculateColumnWidths();
    }

    private void showRegisterDialog() {
        new AddRegistrationDialog(registrationService, event, () -> {
            reloadGridItems();
            if (afterChangeCallback != null) {
                afterChangeCallback.execute();
            }
        }).open();
    }

    private void deregister(@NotNull final RegistrationListEntity registrationListEntity) {
        new ConfirmDialog("Confirm deregistration",
                String.format("Are you sure you want to deregister \"%s\"?", registrationListEntity.fullName()),
                "Deregister", dialogEvent -> {
            registrationService.deregister(event.getId(), registrationListEntity.memberId());
            reloadGridItems();
            if (afterChangeCallback != null) {
                afterChangeCallback.execute();
            }
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void downloadRegistrations() {
        final var resource = new StreamResource("registrations.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] { "First name", "Last name", "Email", "Date", "Source", "No show" });
            grid.getGenericDataView()
                    .getItems().map(registrationListEntity -> new String[] {
                            registrationListEntity.firstName(),
                            registrationListEntity.lastName(),
                            registrationListEntity.email(),
                            registrationListEntity.date().toString(),
                            registrationListEntity.source(),
                            Boolean.toString(registrationListEntity.noShow())
                    }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

    private void printRegistrations() {
        final var report = new PrintPreviewReport<>(RegistrationListEntityWrapper.class, "attendee", "company", "city", "present");
        report.setItems(getRegistrationsForReport());
        final var resource = report.getStreamResource(
                "registrations.pdf",
                this::getRegistrationsForReport,
                PrintPreviewReport.Format.PDF);
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

    private List<RegistrationListEntityWrapper> getRegistrationsForReport() {
        return grid.getGenericDataView().getItems()
                .map(RegistrationListEntityWrapper::new)
                .toList();
    }

}
