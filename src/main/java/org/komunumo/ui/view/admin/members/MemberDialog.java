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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Member;
import org.komunumo.ui.component.EditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class MemberDialog extends EditDialog<Member> {

    public MemberDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<Member> binder) {
        final var firstName = new TextField("First name");
        final var lastName = new TextField("Last name");
        final var company = new TextField("Company");
        final var email = new EmailField("Email");
        final var active = new Checkbox("Active");
        final var address = new TextField("Address");
        final var zipCode = new TextField("Zip code");
        final var city = new TextField("City");
        final var state = new TextField("State");
        final var country = new TextField("Country");
        final var admin = new Checkbox("Member is Admin");
        final var blocked = new Checkbox("Account Blocked");
        final var blockedReason = new TextField("Reason");
        final var comment = new TextArea("Comment");

        firstName.setRequiredIndicatorVisible(true);
        firstName.setValueChangeMode(EAGER);
        lastName.setRequiredIndicatorVisible(true);
        lastName.setValueChangeMode(EAGER);
        blocked.addValueChangeListener(event -> {
            final var isBlocked = event.getValue();
            blockedReason.setEnabled(isBlocked);
            blockedReason.setRequiredIndicatorVisible(isBlocked);
            if (isBlocked) {
                blockedReason.focus();
            } else {
                blockedReason.setValue("");
            }
            binder.validate();
        });
        blockedReason.setValueChangeMode(EAGER);
        blockedReason.setEnabled(false);

        formLayout.add(firstName, lastName, company, email, active,
                address, zipCode, city, state, country,
                admin, blocked, blockedReason, comment);

        binder.forField(firstName)
                .withValidator(new StringLengthValidator(
                        "Please enter the first name of the member (max. 255 chars)", 1, 255))
                .bind(Member::getFirstName, Member::setFirstName);

        binder.forField(lastName)
                .withValidator(new StringLengthValidator(
                        "Please enter the last name of the member (max. 255 chars)", 1, 255))
                .bind(Member::getLastName, Member::setLastName);

        binder.forField(company)
                .bind(Member::getCompany, Member::setCompany);

        binder.forField(email)
                .withValidator(new EmailValidator(
                        "Please enter a correct email address or leave this field empty", true))
                .withValidator(new StringLengthValidator(
                        "The email address is too long (max. 255 chars)", 0, 255))
                .bind(Member::getEmail, Member::setEmail);

        binder.forField(active)
                .bind(Member::getAccountActive, Member::setAccountActive);

        binder.forField(address)
                .withValidator(new StringLengthValidator(
                        "The address is too long (max. 255 chars)", 0, 255))
                .bind(Member::getAddress, Member::setAddress);

        binder.forField(zipCode)
                .withValidator(new StringLengthValidator(
                        "The zip code is too long (max. 255 chars)", 0, 255))
                .bind(Member::getZipCode, Member::setZipCode);

        binder.forField(city)
                .withValidator(new StringLengthValidator(
                        "The city is too long (max. 255 chars)", 0, 255))
                .bind(Member::getCity, Member::setCity);

        binder.forField(state)
                .withValidator(new StringLengthValidator(
                        "The state is too long (max. 255 chars)", 0, 255))
                .bind(Member::getState, Member::setState);

        binder.forField(country)
                .withValidator(new StringLengthValidator(
                        "The country is too long (max. 255 chars)", 0, 255))
                .bind(Member::getCountry, Member::setCountry);

        binder.forField(admin)
                .bind(Member::getAdmin, Member::setAdmin);

        binder.forField(blocked)
                .bind(Member::getAccountBlocked, Member::setAccountBlocked);

        binder.forField(blockedReason)
                .withValidator(value -> !blocked.getValue() || blocked.getValue() && !value.isBlank(),
                        "If you want to block this member, you must enter a reason")
                .withValidator(new StringLengthValidator(
                        "The reason is too long (max. 255 chars)", 0, 255))
                .bind(Member::getAccountBlockedReason, Member::setAccountBlockedReason);

        binder.forField(comment)
                .bind(Member::getComment, Member::setComment);
    }

}
