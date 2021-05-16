package org.komunumo.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.komunumo.data.service.AuthService;
import org.komunumo.data.service.AuthService.AuthException;
import org.komunumo.views.main.MainView;

@Route(value = "login")
@PageTitle("Login")
public class LoginView extends Div {

    public LoginView(final AuthService authService) {
        addClassName("login-view");

        final var email = new TextField("Email");
        final var password = new PasswordField("Password");

        add(
                new H1("Welcome"),
                email,
                password,
                new Button("Login", event -> {
                    try {
                        authService.authenticate(email.getValue(), password.getValue());
                        UI.getCurrent().navigate("dashboard");
                    } catch (final AuthException e) {
                        Notification.show("Wrong credentials.");
                    }
                }),
                new RouterLink("Register", RegisterView.class)
        );
    }

}
