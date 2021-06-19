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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.data.service.MemberService;

public class MemberDialog extends Dialog {

    private final Focusable<? extends Component> focusField;

    public MemberDialog(@NotNull final MemberRecord member,
                        @NotNull final MemberService memberService) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        final var title = new H2(member.getId() == null ? "New member" : "Edit member");
        title.getStyle().set("margin-top", "0");

        final var firstNameField = new TextField("First name");
        firstNameField.setRequiredIndicatorVisible(true);
        firstNameField.setValue(member.getFirstName());

        final var lastNameField = new TextField("Last name");
        lastNameField.setRequiredIndicatorVisible(true);
        lastNameField.setValue(member.getLastName());

        final var emailField = new EmailField("Email");
        emailField.setValue(member.getEmail());

        final var activeField = new Checkbox("Active");
        activeField.setValue(member.getActive());

        final var addressField = new TextField("Address");
        addressField.setValue(member.getAddress());

        final var zipCodeField = new TextField("Zip code");
        zipCodeField.setValue(member.getZipCode());

        final var cityField = new TextField("City");
        cityField.setValue(member.getCity());

        final var stateField = new TextField("State");
        stateField.setValue(member.getState());

        final var countryField = new TextField("Country");
        countryField.setValue(member.getCountry());

        final var adminField = new Checkbox("Admin");
        adminField.setValue(member.getAdmin());

        final var blockedField = new Checkbox("Blocked");
        blockedField.setValue(member.getBlocked());

        final var blockedReasonField = new TextField("Reason");
        blockedReasonField.setValue(member.getBlockedReason());

        final var form = new FormLayout();
        form.add(firstNameField, lastNameField, emailField, activeField,
                addressField, zipCodeField, cityField, stateField, countryField,
                adminField, blockedField, blockedReasonField);

        final var saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(clickEvent -> {
            if (firstNameField.getValue().isBlank()) {
                Notification.show("Please enter the first name of the member!");
            } else if (lastNameField.getValue().isBlank()) {
                Notification.show("Please enter the last name of the member!");
            } else if (blockedField.getValue() && blockedReasonField.getValue().isBlank()) {
                Notification.show("If you want to block this member, you must enter a reason!");
            } else {
                saveButton.setEnabled(false);
                member.setFirstName(firstNameField.getValue());
                member.setLastName(lastNameField.getValue());
                member.setEmail(emailField.getValue());
                member.setActive(activeField.getValue());
                member.setAddress(addressField.getValue());
                member.setZipCode(zipCodeField.getValue());
                member.setCity(cityField.getValue());
                member.setState(stateField.getValue());
                member.setCountry(countryField.getValue());
                member.setAdmin(adminField.getValue());
                member.setBlocked(blockedField.getValue());
                member.setBlockedReason(blockedReasonField.getValue());
                memberService.store(member);

                Notification.show("Member saved.");
                close();
            }
        });
        saveButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        final var cancelButton = new Button("Cancel", clickEvent -> close());
        final var buttonBar = new HorizontalLayout(saveButton, cancelButton);

        add(title, form, buttonBar);

        focusField = firstNameField;
    }

    @Override
    public void open() {
        super.open();
        focusField.focus();
    }
}
