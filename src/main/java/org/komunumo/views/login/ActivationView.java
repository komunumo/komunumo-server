package org.komunumo.views.login;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.komunumo.data.service.AuthService;
import org.komunumo.data.service.AuthService.AuthException;

@Route("activate")
@PageTitle("Activation")
public class ActivationView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private final AuthService authService;

    private VerticalLayout layout;

    public ActivationView(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected VerticalLayout initContent() {
        layout = new VerticalLayout();
        return layout;
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        try {
            final var params = event.getLocation().getQueryParameters().getParameters();
            final var email = params.get("email").get(0);
            final var code = params.get("code").get(0);
            authService.activate(email, code);
            layout.add(
                    new Text("Account activated."),
                    new RouterLink("Login", LoginView.class)
            );
        } catch (final AuthException e) {
            layout.add(new Text("Invalid link."));
        }
    }
}
