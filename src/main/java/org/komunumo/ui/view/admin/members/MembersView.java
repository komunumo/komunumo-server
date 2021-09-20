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

package org.komunumo.ui.view.admin.members;

import com.opencsv.CSVWriter;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.data.service.MemberService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.data.db.tables.Member.MEMBER;

@Route(value = "admin/members", layout = AdminLayout.class)
@PageTitle("Member Administration")
public class MembersView extends ResizableView implements HasUrlParameter<String> {

    private final MemberService memberService;
    private final TextField filterField;
    private final Grid<Record> grid;

    public MembersView(@NotNull final MemberService memberService) {
        this.memberService = memberService;

        addClassNames("members-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter members by name or email");

        final var newMemberButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> newMember());
        newMemberButton.setTitle("Add a new member");

        final var refreshMembersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshMembersButton.setTitle("Refresh the list of members");

        final var downloadMembersButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD));
        downloadMembersButton.setTitle("Download the list of members");
        final var downloadMembersButtonWrapper = new FileDownloadWrapper(downloadMembers());
        downloadMembersButtonWrapper.wrapComponent(downloadMembersButton);

        final var optionBar = new HorizontalLayout(filterField, newMemberButton, refreshMembersButton, downloadMembersButtonWrapper);
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

    private String getFullName(@NotNull final MemberRecord member) {
        return String.format("%s %s", member.getFirstName(), member.getLastName());
    }

    private Grid<Record> createGrid() {
        final var grid = new Grid<Record>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<Record>of("<span style=\"font-weight: bold;\">[[item.firstName]] [[item.lastName]]</span>")
                .withProperty("firstName", record -> record.get(MEMBER.FIRST_NAME))
                .withProperty("lastName", record -> record.get(MEMBER.LAST_NAME)))
                .setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(TemplateRenderer.<Record>of("<a href=\"mailto:[[item.email]]\" target=\"_blank\">[[item.email]]</a>")
                .withProperty("email", record -> record.get(MEMBER.EMAIL)))
                .setHeader("Email").setAutoWidth(true).setKey("email").setFlexGrow(0);
        grid.addColumn(TemplateRenderer.<Record>of(
                "<iron-icon hidden='[[!item.admin]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.admin]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("admin", record -> record.get(MEMBER.ADMIN)))
                .setHeader("Admin").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(TemplateRenderer.<Record>of(
                "<iron-icon hidden='[[!item.blocked]]' icon='vaadin:ban' title='[[item.blockedReason]]' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-error-text-color);'></iron-icon><iron-icon hidden='[[item.blocked]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("blocked", record -> record.get(MEMBER.BLOCKED))
                .withProperty("blockedReason", record -> record.get(MEMBER.BLOCKED_REASON)))
                .setHeader("Blocked").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), event -> editMember(record.get(MEMBER.ID)));
            editButton.setTitle("Edit this member");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), event -> deleteMember(record.get(MEMBER.ID)));
            deleteButton.setTitle("Delete this member");
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
        grid.getColumnByKey("email").setVisible(width >= 1100);
    }

    private void newMember() {
        showMemberDialog(memberService.newMember());
    }

    private void editMember(@NotNull final Long memberId) {
        final var member = memberService.get(memberId);
        if (member.isPresent()) {
            showMemberDialog(member.get());
        } else {
            Notification.show("This member does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void showMemberDialog(@NotNull final MemberRecord member) {
        final var dialog = new MemberDialog(member.get(MEMBER.ID) != null ? "Edit Member" : "New Member");
        dialog.open(member, this::reloadGridItems);
    }

    private void deleteMember(@NotNull final Long memberId) {
        final var member = memberService.get(memberId);
        if (member.isPresent()) {
            new ConfirmDialog("Confirm deletion",
                    String.format("Are you sure you want to permanently delete the member \"%s\"?", getFullName(member.get())),
                    "Delete", dialogEvent -> {
                memberService.delete(member.get());
                reloadGridItems();
                dialogEvent.getSource().close();
            },
                    "Cancel", dialogEvent -> dialogEvent.getSource().close()
            ).open();
        } else {
            Notification.show("This member does not exist anymore. Reloading view…");
            reloadGridItems();
        }
    }

    private void reloadGridItems() {
        grid.setItems(query -> memberService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

    private StreamResource downloadMembers() {
        return new StreamResource("members.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "First name", "Last name", "Email",
                    "Address", "Zip code", "City", "State", "Country",
                    "Member since", "Admin", "Active", "Blocked", "Blocked reason"
            });
            grid.getGenericDataView()
                    .getItems().map(record -> new String[] {
                    record.get(MEMBER.ID).toString(),
                    record.get(MEMBER.FIRST_NAME),
                    record.get(MEMBER.LAST_NAME),
                    record.get(MEMBER.EMAIL),
                    record.get(MEMBER.ADDRESS),
                    record.get(MEMBER.ZIP_CODE),
                    record.get(MEMBER.CITY),
                    record.get(MEMBER.STATE),
                    record.get(MEMBER.COUNTRY),
                    record.get(MEMBER.MEMBER_SINCE).toString(),
                    record.get(MEMBER.ADMIN).toString(),
                    record.get(MEMBER.ACTIVE).toString(),
                    record.get(MEMBER.BLOCKED).toString(),
                    record.get(MEMBER.BLOCKED_REASON)
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
    }

}
