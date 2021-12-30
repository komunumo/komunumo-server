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
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
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

import java.time.LocalDate;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.MemberService;
import org.komunumo.data.service.SponsorService;
import org.komunumo.ui.component.EnhancedButton;
import org.komunumo.ui.component.FilterField;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import javax.annotation.security.RolesAllowed;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.komunumo.util.FormatterUtil.formatDate;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Route(value = "admin/members", layout = AdminLayout.class)
@PageTitle("Member Administration")
@CssImport(value = "./themes/komunumo/views/admin/members-view.css")
@CssImport(value = "./themes/komunumo/views/admin/komunumo-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@RolesAllowed(Role.Type.ADMIN)
public class MembersView extends ResizableView implements HasUrlParameter<String> {

    private final MemberService memberService;
    private final SponsorService sponsorService;
    private final TextField filterField;
    private final Grid<Member> grid;

    public MembersView(@NotNull final MemberService memberService,
                       @NotNull final SponsorService sponsorService) {
        this.memberService = memberService;
        this.sponsorService = sponsorService;

        addClassNames("members-view", "flex", "flex-col", "h-full");

        grid = createGrid();
        filterField = new FilterField();
        filterField.addValueChangeListener(event -> reloadGridItems());
        filterField.setTitle("Filter members by name or email");

        final var newMemberButton = new EnhancedButton(new Icon(VaadinIcon.FILE_ADD), clickEvent -> newMember());
        newMemberButton.setTitle("Add a new member");

        final var refreshMembersButton = new EnhancedButton(new Icon(VaadinIcon.REFRESH), clickEvent -> reloadGridItems());
        refreshMembersButton.setTitle("Refresh the list of members");

        final var downloadMembersButton = new EnhancedButton(new Icon(VaadinIcon.DOWNLOAD), clickEvent -> downloadMembers());
        downloadMembersButton.setTitle("Download the list of members");

        final var optionBar = new HorizontalLayout(filterField, newMemberButton, refreshMembersButton, downloadMembersButton);
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

    private Grid<Member> createGrid() {
        final var sponsorDomains = sponsorService.getActiveSponsorDomains();

        final var grid = new Grid<Member>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TemplateRenderer.<Member>of("<span style=\"font-weight: bold;\">[[item.fullName]]</span><br/><span>[[item.company]]</span>")
                .withProperty("fullName", Member::getFullName)
                .withProperty("company", Member::getCompany))
                .setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(TemplateRenderer.<Member>of("<a href=\"mailto:[[item.email]]\" target=\"_blank\">[[item.email]]</a>")
                .withProperty("email", Member::getEmail))
                .setHeader("Email").setAutoWidth(true).setKey("email").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(member -> new Text(getMembershipText(member, sponsorDomains))))
                .setHeader("Membership").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(member -> new Icon(member.getAdmin() ? VaadinIcon.CHECK : VaadinIcon.MINUS)))
                .setHeader("Admin").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(member -> {
                    final var icon = new Icon(member.getAccountBlocked() ? VaadinIcon.BAN : VaadinIcon.MINUS);
                    icon.getElement().setAttribute("title", member.getAccountBlockedReason());
                    return icon;
                }))
                .setHeader("Blocked").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(member -> {
            final var editButton = new EnhancedButton(new Icon(VaadinIcon.EDIT), clickEvent -> showMemberDialog(member));
            editButton.setTitle("Edit this member");
            final var deleteButton = new EnhancedButton(new Icon(VaadinIcon.TRASH), clickEvent -> deleteMember(member));
            deleteButton.setTitle("Delete this member");
            return new HorizontalLayout(editButton, deleteButton);
        }))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setHeightFull();

        return grid;
    }

    @SuppressWarnings("ConstantConditions") // for better readability
    private String getMembershipText(@NotNull final Member member, @NotNull Set<String> sponsorDomains) {
        final var begin = member.getMembershipBegin();
        final var end = member.getMembershipEnd();

        // is active member
        if (begin != null && end == null) {
            return "since %d" .formatted(begin.getYear());
        }
        if (begin != null && end != null && end.isAfter(LocalDate.now().minusDays(1))) {
            return "from %d to %d".formatted(begin.getYear(), end.getYear());
        }
        if (begin == null && end != null && end.isAfter(LocalDate.now().minusDays(1))) {
            return "until %d".formatted(end.getYear());
        }

        // is active sponsor member
        if (!member.getEmail().isBlank()) {
            final var emailParts = member.getEmail().split("@", 2);
            if (emailParts.length == 2) {
                final var emailDomain = emailParts[1];
                if (sponsorDomains.contains(emailDomain)) {
                    return "sponsored";
                }
            }
        }

        // no member
        if (begin == null && end == null) {
            return "no";
        } else if (begin != null && end != null) {
            return "from %d to %d".formatted(begin.getYear(), end.getYear());
        } else if (begin == null && end != null) {
            return "until %d".formatted(end.getYear());
        }

        return "unknown";
    }

    @Override
    protected void onResize(final int width) {
        grid.getColumnByKey("email").setVisible(width >= 1100);
    }

    private void newMember() {
        showMemberDialog(memberService.newMember());
    }

    private void showMemberDialog(@NotNull final Member member) {
        final var dialog = new MemberDialog(member.getId() != null ? "Edit Member" : "New Member");
        dialog.open(member, this::reloadGridItems);
    }

    private void deleteMember(final Member member) {
        new ConfirmDialog("Confirm deletion",
                String.format("Are you sure you want to permanently delete the member \"%s\"?", member.getFullName()),
                "Delete", dialogEvent -> {
            memberService.delete(member);
            reloadGridItems();
            dialogEvent.getSource().close();
        },
                "Cancel", dialogEvent -> dialogEvent.getSource().close()
        ).open();
    }

    private void reloadGridItems() {
        grid.setItems(query -> memberService.find(query.getOffset(), query.getLimit(), filterField.getValue()));
        grid.recalculateColumnWidths();
    }

    private void downloadMembers() {
        final var resource = new StreamResource("members.csv", () -> {
            final var stringWriter = new StringWriter();
            final var csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(new String[] {
                    "ID", "First Name", "Last Name", "E-Mail",
                    "Address", "Zip Code", "City", "State", "Country",
                    "Registration Date", "Membership Begin", "Membership End",
                    "Admin", "Account Active", "Account Blocked", "Account Blocked reason"
            });
            grid.getGenericDataView()
                    .getItems().map(member -> new String[] {
                    member.getId().toString(),
                    member.getFirstName(),
                    member.getLastName(),
                    member.getEmail(),
                    member.getAddress(),
                    member.getZipCode(),
                    member.getCity(),
                    member.getState(),
                    member.getCountry(),
                    formatDateTime(member.getRegistrationDate()),
                    formatDate(member.getMembershipBegin()),
                    formatDate(member.getMembershipEnd()),
                    member.getAdmin().toString(),
                    member.getAccountActive().toString(),
                    member.getAccountBlocked().toString(),
                    member.getAccountBlockedReason()
            }).forEach(csvWriter::writeNext);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(UTF_8));
        });
        final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    }

}
