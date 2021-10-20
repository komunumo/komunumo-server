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

package org.komunumo.ui.view.admin.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.importer.bigmarker.BigMarkerRegistration;
import org.komunumo.data.importer.bigmarker.BigMarkerReport;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.MemberService;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.IOException;
import java.util.NoSuchElementException;

@Route(value = "admin/imports", layout = AdminLayout.class)
@PageTitle("Imports")
@CssImport(value = "./themes/komunumo/views/admin/imports-view.css")
public class ImportsView extends ResizableView {

    private final MemberService memberService;
    private final EventService eventService;
    private final EventMemberService eventMemberService;

    public ImportsView(
            @NotNull final MemberService memberService,
            @NotNull final EventService eventService,
            @NotNull final EventMemberService eventMemberService) {
        this.memberService = memberService;
        this.eventService = eventService;
        this.eventMemberService = eventMemberService;

        addClassName("imports-view");
        add(
                new H2("BigMarker"),
                createImportRegistrationsComponents()
        );
    }

    private Component createImportRegistrationsComponents() {
        final var title = new H3("Import registrations");

        final var buffer = new MemoryBuffer();
        final var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(succeededEvent -> {
            try {
                final var report = new BigMarkerReport(buffer.getInputStream());
                final var registrations = report.getRegistrations();

                final var grid = new Grid<BigMarkerRegistration>();
                grid.addColumn(BigMarkerRegistration::getFirstName)
                        .setHeader("First Name")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::getLastName)
                        .setHeader("Last Name")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::getEmail)
                        .setHeader("Email")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::getRegistrationDate)
                        .setHeader("Registration Date")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::hasUnsubscribed)
                        .setHeader("Unsubscribed")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::hasAttendedLive)
                        .setHeader("Attended Live")
                        .setAutoWidth(true);
                grid.setItems(registrations);
                upload.getElement().getParent().appendChild(grid.getElement());

                final var importButton = new Button("Start Import");
                final var cancelButton = new Button("Cancel");

                importButton.setDisableOnClick(true);
                importButton.setEnabled(false);
                importButton.addClickListener(buttonClickEvent -> {
                    try {
                        cancelButton.setEnabled(false);
                        report.importRegistrations(eventService, eventMemberService, memberService);
                        importButton.getElement().removeFromParent();
                        cancelButton.getElement().removeFromParent();
                        grid.getElement().removeFromParent();
                        upload.getElement().setPropertyJson("files", Json.createArray());
                        Notification.show(String.format("Successfully imported %d registrations.", registrations.size()));
                    } catch (final NoSuchElementException e) {
                        Notification.show(e.getMessage());
                        importButton.setText("Retry Import");
                        importButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                });
                importButton.setEnabled(!registrations.isEmpty());
                upload.getElement().getParent().appendChild(importButton.getElement());

                cancelButton.addClickListener(buttonClickEvent -> {
                    importButton.getElement().removeFromParent();
                    cancelButton.getElement().removeFromParent();
                    grid.getElement().removeFromParent();
                    upload.getElement().setPropertyJson("files", Json.createArray());
                });
                upload.getElement().getParent().appendChild(cancelButton.getElement());

                Notification.show("Excel file successfully parsed.");
            } catch (final IOException e) {
                Notification.show(e.getMessage());
            }
        });
        upload.addFileRejectedListener(event -> Notification.show(event.getErrorMessage()));

        return new Div(
                title, upload
        );
    }

}
