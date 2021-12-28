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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.komunumo.ApplicationServiceInitListener;
import org.komunumo.data.entity.Role;
import org.komunumo.data.importer.bigmarker.BigMarkerRegistration;
import org.komunumo.data.importer.bigmarker.BigMarkerReport;
import org.komunumo.data.importer.clubdesk.ClubDeskFile;
import org.komunumo.data.importer.clubdesk.ClubDeskMember;
import org.komunumo.data.importer.jugs.JUGSImporter;
import org.komunumo.data.service.EventKeywordService;
import org.komunumo.data.service.EventOrganizerService;
import org.komunumo.data.service.FaqService;
import org.komunumo.data.service.RedirectService;
import org.komunumo.data.service.RegistrationService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.EventSpeakerService;
import org.komunumo.data.service.KeywordService;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.NewsService;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.data.service.SponsorService;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.NoSuchElementException;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

@Route(value = "admin/imports", layout = AdminLayout.class)
@PageTitle("Imports")
@CssImport(value = "./themes/komunumo/views/admin/imports-view.css")
@RolesAllowed(Role.Type.ADMIN)
public class ImportsView extends ResizableView {

    private final DSLContext dsl;
    private final SponsorService sponsorService;
    private final MemberService memberService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final SpeakerService speakerService;
    private final EventSpeakerService eventSpeakerService;
    private final EventOrganizerService eventOrganizerService;
    private final KeywordService keywordService;
    private final EventKeywordService eventKeywordService;
    private final FaqService faqService;
    private final NewsService newsService;
    private final RedirectService redirectService;
    private final ApplicationServiceInitListener applicationServiceInitListener;

    public ImportsView(
            @NotNull final DSLContext dsl,
            @NotNull final SponsorService sponsorService,
            @NotNull final MemberService memberService,
            @NotNull final EventService eventService,
            @NotNull final RegistrationService registrationService,
            @NotNull final SpeakerService speakerService,
            @NotNull final EventSpeakerService eventSpeakerService,
            @NotNull final EventOrganizerService eventOrganizerService,
            @NotNull final KeywordService keywordService,
            @NotNull final EventKeywordService eventKeywordService,
            @NotNull final FaqService faqService,
            @NotNull final NewsService newsService,
            @NotNull final RedirectService redirectService,
            @NotNull final ApplicationServiceInitListener applicationServiceInitListener) {
        this.dsl = dsl;
        this.sponsorService = sponsorService;
        this.memberService = memberService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.speakerService = speakerService;
        this.eventSpeakerService = eventSpeakerService;
        this.eventOrganizerService = eventOrganizerService;
        this.keywordService = keywordService;
        this.eventKeywordService = eventKeywordService;
        this.faqService = faqService;
        this.newsService = newsService;
        this.redirectService = redirectService;
        this.applicationServiceInitListener = applicationServiceInitListener;

        addClassName("imports-view");
        add(
                new H2("BigMarker"),
                createImportBigMarkerRegistrations(),
                createImportBigMarkerRegistrationsMulti(),
                new H2("ClubDesk"),
                createImportClubDeskMembers(),
                new H2("Java User Group Switzerland"),
                createImportJavaUserGroupSwitzerland()
        );
    }

    private Component createImportBigMarkerRegistrations() {
        final var title = new H3("Import registrations");

        final var buffer = new MemoryBuffer();
        final var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(succeededEvent -> {
            try {
                final var report = new BigMarkerReport(buffer.getInputStream());
                final var registrations = report.getBigMarkerRegistrations();

                final var grid = new Grid<BigMarkerRegistration>();
                grid.addColumn(BigMarkerRegistration::firstName)
                        .setHeader("First Name")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::lastName)
                        .setHeader("Last Name")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::email)
                        .setHeader("Email")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::registrationDate)
                        .setHeader("Registration Date")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::unsubscribed)
                        .setHeader("Unsubscribed")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::attendedLive)
                        .setHeader("Attended Live")
                        .setAutoWidth(true);
                grid.addColumn(BigMarkerRegistration::membership)
                        .setHeader("Membership")
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
                        report.importRegistrations(eventService, registrationService, memberService);
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

    private Component createImportBigMarkerRegistrationsMulti() {
        final var title = new H3("Import multiple registrations");

        final var buffer = new MultiFileMemoryBuffer();
        final var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(succeededEvent -> {
            try {
                final var report = new BigMarkerReport(buffer.getInputStream(succeededEvent.getFileName()));
                final var registrations = report.getBigMarkerRegistrations();

                try {
                    report.importRegistrations(eventService, registrationService, memberService);
                    Notification.show(String.format("Successfully imported %d registrations from excel file '%s'.",
                            registrations.size(), succeededEvent.getFileName()));
                } catch (final NoSuchElementException e) {
                    Notification.show(e.getMessage());
                }
            } catch (final IOException e) {
                Notification.show(e.getMessage());
            }
        });
        upload.addFileRejectedListener(event -> Notification.show(event.getErrorMessage()));

        return new Div(
                title, upload
        );
    }

    private Component createImportClubDeskMembers() {
        final var title = new H3("Import members");

        final var buffer = new MemoryBuffer();
        final var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(succeededEvent -> {
            try {
                final var clubDeskFile = new ClubDeskFile(buffer.getInputStream());
                final var members = clubDeskFile.getMembers();

                final var grid = new Grid<ClubDeskMember>();
                grid.addColumn(ClubDeskMember::membershipBeginDate)
                        .setHeader("Membership Begin Date")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::membershipEndDate)
                        .setHeader("Membership End Date")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::membershipId)
                        .setHeader("Membership ID")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::firstName)
                        .setHeader("First Name")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::lastName)
                        .setHeader("Last Name")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::company)
                        .setHeader("Company")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::email)
                        .setHeader("Email")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::address)
                        .setHeader("Address")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::zipCode)
                        .setHeader("Zip Code")
                        .setAutoWidth(true);
                grid.addColumn(ClubDeskMember::city)
                        .setHeader("City")
                        .setAutoWidth(true);
                grid.setItems(members);
                upload.getElement().getParent().appendChild(grid.getElement());

                final var importButton = new Button("Start Import");
                final var cancelButton = new Button("Cancel");

                importButton.setDisableOnClick(true);
                importButton.setEnabled(false);
                importButton.addClickListener(buttonClickEvent -> {
                    try {
                        cancelButton.setEnabled(false);
                        clubDeskFile.importMembers(memberService);
                        importButton.getElement().removeFromParent();
                        cancelButton.getElement().removeFromParent();
                        grid.getElement().removeFromParent();
                        upload.getElement().setPropertyJson("files", Json.createArray());
                        Notification.show(String.format("Successfully imported %d members.", members.size()));
                    } catch (final NoSuchElementException e) {
                        Notification.show(e.getMessage());
                        importButton.setText("Retry Import");
                        importButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                });
                importButton.setEnabled(!members.isEmpty());
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

    private Component createImportJavaUserGroupSwitzerland() {
        final var dbURL = new TextField("Database URL");
        final var dbUser = new TextField("Database User");
        final var dbPass = new PasswordField("Database Password");
        final var importButton = new Button("Start Import");

        dbURL.setValueChangeMode(EAGER);
        dbUser.setValueChangeMode(EAGER);
        dbPass.setValueChangeMode(EAGER);

        dbURL.addValueChangeListener(changeEvent -> importButton.setEnabled(
                !dbURL.isEmpty() && !dbUser.isEmpty() && !dbPass.isEmpty()
        ));
        dbUser.addValueChangeListener(changeEvent -> importButton.setEnabled(
                !dbURL.isEmpty() && !dbUser.isEmpty() && !dbPass.isEmpty()
        ));
        dbPass.addValueChangeListener(changeEvent -> importButton.setEnabled(
                !dbURL.isEmpty() && !dbUser.isEmpty() && !dbPass.isEmpty()
        ));

        importButton.setDisableOnClick(true);
        importButton.setEnabled(!dbURL.isEmpty() && !dbUser.isEmpty() && !dbPass.isEmpty());
        importButton.addClickListener(buttonClickEvent -> {
            final var importer = new JUGSImporter(dsl, sponsorService, memberService, eventService, registrationService, speakerService,
                    eventSpeakerService, eventOrganizerService, keywordService, eventKeywordService, faqService, newsService, redirectService,
                    applicationServiceInitListener);
            importer.importFromJavaUserGroupSwitzerland(dbURL.getValue(), dbUser.getValue(), dbPass.getValue());
        });

        final var dbForm = new FormLayout(dbURL, dbUser, dbPass);
        dbForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("800px", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        return new Div(dbForm, importButton);
    }

}
