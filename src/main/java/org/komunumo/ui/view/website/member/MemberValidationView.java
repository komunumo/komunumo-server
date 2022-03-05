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

package org.komunumo.ui.view.website.member;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.komunumo.security.SecurityService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.ui.view.website.home.HomeView;
import org.springframework.security.authentication.BadCredentialsException;

@AnonymousAllowed
@PageTitle("Validation")
@Route(value = "member/validate", layout = WebsiteLayout.class)
@CssImport("./themes/komunumo/views/website/member-validation-view.css")
public final class MemberValidationView extends ContentBlock implements BeforeEnterObserver {

    @Serial
    private static final long serialVersionUID = -679013607285455434L;
    private final SecurityService securityService;

    public MemberValidationView(@NotNull final SecurityService securityService) {
        super("Member");
        addClassName("member-validation-view");
        this.securityService = securityService;
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent event) {
        Paragraph message;

        try {
            final var params = event.getLocation().getQueryParameters().getParameters();
            final var email = params.get("email").get(0);
            final var code = params.get("code").get(0);
            securityService.activate(email, code);
            message = new Paragraph("Your email address was successfully validated.");
            message.addClassName("successful");
        } catch (final BadCredentialsException e) {
            message = new Paragraph("The link was invalid. Please check your email for a working validation link.");
            message.addClassName("failed");
        }

        setContent(new Div(new H2("Validation"), message, new RouterLink("Back to home page", HomeView.class)));
    }
}
