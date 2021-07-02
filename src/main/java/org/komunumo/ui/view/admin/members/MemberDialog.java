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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.ui.component.KomunumoEditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class MemberDialog extends KomunumoEditDialog<MemberRecord> {

    public MemberDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm() {
        final var firstName = new TextField("First name");
        final var lastName = new TextField("Last name");
        final var email = new EmailField("Email");
        final var active = new Checkbox("Active");
        final var address = new TextField("Address");
        final var zipCode = new TextField("Zip code");
        final var city = new TextField("City");
        final var state = new TextField("State");
        final var country = new TextField("Country");
        final var admin = new Checkbox("Admin");
        final var blocked = new Checkbox("Blocked");
        final var blockedReason = new TextField("Reason");

        firstName.setRequiredIndicatorVisible(true);
        firstName.setValueChangeMode(EAGER);
        lastName.setRequiredIndicatorVisible(true);
        lastName.setValueChangeMode(EAGER);
        blocked.addValueChangeListener(event -> {
            blockedReason.setRequiredIndicatorVisible(event.getValue());
            blockedReason.focus();
            binder.validate();
        });
        blockedReason.setValueChangeMode(EAGER);

        formLayout.add(firstName, lastName, email, active,
                address, zipCode, city, state, country,
                admin, blocked, blockedReason);

        binder.forField(firstName)
                .withValidator(new StringLengthValidator(
                        "Please enter the first name of the member", 1, null))
                .bind(MemberRecord::getFirstName, MemberRecord::setFirstName);

        binder.forField(lastName)
                .withValidator(new StringLengthValidator(
                        "Please enter the last name of the member", 1, null))
                .bind(MemberRecord::getLastName, MemberRecord::setLastName);

        binder.forField(email)
                .withValidator(new EmailValidator(
                        "Please enter a correct email address or leave this field empty", true))
                .bind(MemberRecord::getEmail, MemberRecord::setEmail);

        binder.forField(active)
                .bind(MemberRecord::getActive, MemberRecord::setActive);

        binder.forField(address)
                .bind(MemberRecord::getAddress, MemberRecord::setAddress);

        binder.forField(zipCode)
                .bind(MemberRecord::getZipCode, MemberRecord::setZipCode);

        binder.forField(city)
                .bind(MemberRecord::getCity, MemberRecord::setCity);

        binder.forField(state)
                .bind(MemberRecord::getState, MemberRecord::setState);

        binder.forField(country)
                .bind(MemberRecord::getCountry, MemberRecord::setCountry);

        binder.forField(admin)
                .bind(MemberRecord::getAdmin, MemberRecord::setAdmin);

        binder.forField(blocked)
                .bind(MemberRecord::getBlocked, MemberRecord::setBlocked);

        binder.forField(blockedReason)
                .withValidator(value -> !blocked.getValue() || blocked.getValue() && !value.isBlank(),
                        "If you want to block this member, you must enter a reason")
                .bind(MemberRecord::getBlockedReason, MemberRecord::setBlockedReason);
    }

}
