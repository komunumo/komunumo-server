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

package org.komunumo.views.login;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.komunumo.data.service.AuthService;
import org.komunumo.data.service.AuthService.AccessDeniedException;

@Route(value = "login")
@PageTitle("Login")
public class LoginView extends VerticalLayout {

    public LoginView(final AuthService authService) {
        addClassName("login-view");

        final var email = new EmailField("Email");
        email.setRequiredIndicatorVisible(true);
        email.setAutofocus(true);
        email.setValue("");
        final var emailBinder = new Binder<String>();
        emailBinder.forField(email)
                .withValidator(new EmailValidator("Please enter your email address.", false))
                .bind(o -> null, (o, o2) -> { });
        email.addBlurListener(event -> emailBinder.validate());

        final var password = new PasswordField("Password");
        password.setRequired(true);
        password.setValue("");
        final var passwordBinder = new Binder<String>();
        passwordBinder.forField(password)
                .withValidator(new StringLengthValidator("Please enter your password.", 1, 255))
                .bind(o -> null, (o, o2) -> { });
        password.addBlurListener(event -> passwordBinder.validate());

        final var login = new Button("Login", event -> {
            emailBinder.validate();
            passwordBinder.validate();
            if (emailBinder.isValid() && passwordBinder.isValid()) {
                try {
                    authService.authenticate(email.getValue(), password.getValue());
                    UI.getCurrent().navigate("dashboard");
                } catch (final AccessDeniedException e) {
                    Notification.show("Wrong credentials.");
                }
            }
        });
        login.addClickShortcut(Key.ENTER);
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var register = new RouterLink("Register as a new member", RegisterView.class);
        register.addClassName("register-link");

        add(
                new FormLayout(
                        new H1("Login"),
                        email, password,
                        login, register
                )
        );

        setSizeFull();
        setAlignItems(Alignment.CENTER); //horizontal center
        setJustifyContentMode(JustifyContentMode.CENTER); //vertical center
    }

}
