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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.AuthService;
import org.komunumo.data.service.AuthService.AccessDeniedException;
import org.komunumo.views.admin.dashboard.DashboardView;

@Route(value = "login")
@PageTitle("Login")
public class LoginView extends LoginOverlay implements AfterNavigationObserver, BeforeEnterObserver {

    private final AuthService authService;

    public LoginView(@NotNull final AuthService authService) {
        this.authService = authService;

        final var i18n = LoginI18n.createDefault();

        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Komunumo");
        i18n.getHeader().setDescription("Java User Group Switzerland");
        i18n.setAdditionalInformation(null);

        i18n.setForm(new LoginI18n.Form());
        i18n.getForm().setSubmit("Login");
        i18n.getForm().setTitle("Login");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setForgotPassword("I forgot my password");

        i18n.getErrorMessage().setTitle("Incorrect email or password");
        i18n.getErrorMessage().setMessage("Check that you have entered the correct email and password and try again.");

        setI18n(i18n);

        setForgotPasswordButtonVisible(true);

        addLoginListener(event -> {
            try {
                authService.authenticate(event.getUsername(), event.getPassword());
            } catch (final AccessDeniedException e) {
                setError(true);
                Notification.show(e.getMessage());
            }

        });

        addForgotPasswordListener(event -> UI.getCurrent().getPage().executeJs(
            "var field = document.getElementById('vaadinLoginUsername'); if (field !== null) { return field.value; } else { return null; }")
            .then(String.class, email -> {
                if (email.isBlank()) {
                    Notification.show("Please enter your email address first.");
                    UI.getCurrent().getPage().executeJs(
                            "var field = document.getElementById('vaadinLoginUsername'); if (field !== null) { field.focus(); }");
                } else {
                    authService.resetPassword(email);
                    Notification.show("Please check your email account for further instructions.");
                    UI.getCurrent().getPage().executeJs(
                            "var field = document.getElementById('vaadinLoginPassword'); if (field !== null) { field.focus(); }");
                }
            }
        ));

        UI.getCurrent().getPage().executeJs(
                "var field = document.getElementById('vaadinLoginUsername'); if (field !== null) { field.focus(); }");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent event) {
        if (authService.isUserLoggedIn()) {
            event.forwardTo(DashboardView.class);
        } else {
            setOpened(true);
        }
    }

    @Override
    public void afterNavigation(@NotNull final AfterNavigationEvent event) {
        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }

}
