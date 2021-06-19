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
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.data.service.MemberService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.view.admin.AdminView;

import java.util.List;

@Route(value = "admin/members", layout = AdminView.class)
@PageTitle("Member Administration")
public class MembersView extends Div implements HasUrlParameter<String> {

    private final MemberService memberService;
    private final TextField filterField;
    private final Grid<MemberRecord> grid;

    public MembersView(@NotNull final MemberService memberService) {
        this.memberService = memberService;

        addClassNames("members-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter members by name");

        final var newMemberButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), event -> editMember(memberService.newMember()));
        newMemberButton.setTitle("Add a new member");
        final var refreshMembersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), event -> reloadGridItems());
        refreshMembersButton.setTitle("Refresh the list of members");
        final var optionBar = new HorizontalLayout(filterField, newMemberButton, refreshMembersButton);
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

    private Grid<MemberRecord> createGrid() {
        final var grid = new Grid<MemberRecord>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(TemplateRenderer.<MemberRecord>of("<span style=\"font-weight: bold;\">[[item.firstName]] [[item.lastName]]</span>")
                .withProperty("firstName", MemberRecord::getFirstName)
                .withProperty("lastName", MemberRecord::getLastName))
                .setHeader("Name").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<MemberRecord>of("<a href=\"mailto:[[item.email]]\" target=\"_blank\">[[item.email]]</a>")
                .withProperty("email", MemberRecord::getEmail))
                .setHeader("Email").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<MemberRecord>of(
                "<iron-icon hidden='[[!item.admin]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.admin]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("admin", MemberRecord::getAdmin))
                .setHeader("Admin").setAutoWidth(true);
        grid.addColumn(TemplateRenderer.<MemberRecord>of(
                "<iron-icon hidden='[[!item.blocked]]' icon='vaadin:ban' title='[[item.blockedReason]]' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-error-text-color);'></iron-icon><iron-icon hidden='[[item.blocked]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("blocked", MemberRecord::getBlocked)
                .withProperty("blockedReason", MemberRecord::getBlockedReason))
                .setHeader("Blocked").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(record -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), event -> editMember(record));
            editButton.setTitle("Edit this member");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), event -> deleteMember(record));
            deleteButton.setTitle("Delete this member");
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setFlexGrow(0)
                .setFrozen(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        return grid;
    }

    private void editMember(@NotNull final MemberRecord memberRecord) {
        final var dialog = new MemberDialog(memberRecord, memberService);
        dialog.addOpenedChangeListener(changeEvent -> { if (!changeEvent.isOpened()) { reloadGridItems(); } } );
        dialog.open();
    }

    private void deleteMember(@NotNull final MemberRecord member) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the member \"%s\"?", getFullName(member)),
                "Delete", (dialogEvent) -> {
                    memberService.delete(member);
                    reloadGridItems();
                    dialogEvent.getSource().close();
                },
                "Cancel", (dialogEvent) -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> memberService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
    }

}
