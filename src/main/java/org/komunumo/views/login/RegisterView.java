package org.komunumo.views.login;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.komunumo.data.service.AuthService;

@Route("register")
@PageTitle("Register")
public class RegisterView extends Div {

    public RegisterView(final AuthService authService) {
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
                    Notification.show("Registration succeeded.");
                })
        );
    }

}
