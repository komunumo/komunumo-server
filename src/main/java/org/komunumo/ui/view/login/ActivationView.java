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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.security.SecurityService;
import org.springframework.security.authentication.BadCredentialsException;

@Route("activate")
@PageTitle("Activation")
@AnonymousAllowed
public class ActivationView extends Composite<Component> implements BeforeEnterObserver {

    private final SecurityService securityService;

    private VerticalLayout layout;

    public ActivationView(@NotNull final SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    protected VerticalLayout initContent() {
        layout = new VerticalLayout();
        return layout;
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent event) {
        try {
            final var params = event.getLocation().getQueryParameters().getParameters();
            final var email = params.get("email").get(0);
            final var code = params.get("code").get(0);
            securityService.activate(email, code);
            layout.add(
                    new Text("Account activated."),
                    new RouterLink("Login", LoginView.class)
            );
        } catch (final BadCredentialsException e) {
            layout.add(new Text("Invalid link."));
        }
    }
}
