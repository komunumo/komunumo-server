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

package org.komunumo.ui.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.AuthService;

@Route("register")
@PageTitle("Register")
public class RegisterView extends Div {

    public RegisterView(@NotNull final AuthService authService) {
        addClassName("register-view");

        final var firstName = new TextField("First Name");
        final var lastName = new TextField("Last Name");
        final var email = new EmailField("Email");
        final var address = new TextField("Address");
        final var zipCode = new TextField("Zip Code");
        final var city = new TextField("City");
        final var state = new TextField("State");
        final var country = new TextField("Country");

        add(
                new H2("Register"),
                firstName, lastName, email, address, zipCode, city, state, country,
                new Button("Register", event -> {
                    authService.register(
                            firstName.getValue(), lastName.getValue(), email.getValue(),
                            address.getValue(), zipCode.getValue(), city.getValue(),
                            state.getValue(), country.getValue()
                    );
                    Notification.show("Check your email.");
                })
        );
    }

}
