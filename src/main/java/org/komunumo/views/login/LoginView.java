package org.komunumo.views.login;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.komunumo.data.service.AuthService;
import org.komunumo.data.service.AuthService.AuthException;

@Route(value = "login")
@PageTitle("Login")
public class LoginView extends Div {

    public LoginView(final AuthService authService) {
        addClassName("login-view");

        final var email = new EmailField("Email");
        email.setRequiredIndicatorVisible(true);
        email.setErrorMessage("Please enter your email address");
        email.setPreventInvalidInput(true);
        email.setAutofocus(true);

        final var password = new PasswordField("Password");
        password.setRequired(true);
        password.setErrorMessage("Please enter your password");
        password.setPreventInvalidInput(true);

        final var login = new Button("Login", event -> {
            if (email.isInvalid() || password.isInvalid()) {
                Notification.show("Please enter your email address and your password to login.");
            } else {
                try {
                    authService.authenticate(email.getValue(), password.getValue());
                    UI.getCurrent().navigate("dashboard");
                } catch (final AuthException e) {
                    Notification.show("Wrong credentials.");
                }
            }
        });
        login.addClickShortcut(Key.ENTER);
        login.setThemeName("primary");

        final var register = new RouterLink("Register", RegisterView.class);

        add(
                new H1("Welcome"),
                email, password,
                login, register
        );
    }

}