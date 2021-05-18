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
