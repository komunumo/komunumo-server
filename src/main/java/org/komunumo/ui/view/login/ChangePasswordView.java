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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.komunumo.security.AuthenticatedUser;
import org.komunumo.security.SecurityConfiguration;
import org.komunumo.security.SecurityService;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.security.PermitAll;

@Route("change-password")
@PageTitle("Change Password")
@PermitAll
public class ChangePasswordView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = -1548708006113468096L;

    public ChangePasswordView(@NotNull final AuthenticatedUser authenticatedUser,
                              @NotNull final SecurityService securityService) {
        final var title = new H2("Change password");
        final var oldPassword = new PasswordField("Old password (or one time password)");
        oldPassword.setRequired(true);
        final var newPassword = new PasswordField("New password");
        newPassword.setRequired(true);
        final var repeatPassword = new PasswordField("Repeat password");
        repeatPassword.setRequired(true);

        final var saveButton = new Button("Save", event -> {
            if (oldPassword.getValue().isBlank()
                    || newPassword.getValue().isBlank()
                    || repeatPassword.getValue().isBlank()) {
                Notification.show("Please fill out all three password fields!");
            } else if (!newPassword.getValue().equals(repeatPassword.getValue())) {
                Notification.show("You have a typo! Your new password and repeat password does not match.");
            } else if (newPassword.getValue().equals(oldPassword.getValue())) {
                Notification.show("Your new password is not allowed to be equal to your old password.");
            } else {
                try {
                    securityService.changePassword(oldPassword.getValue(), newPassword.getValue());
                    final var okButton = new Button("OK", clickEvent -> authenticatedUser.logout(SecurityConfiguration.LOGIN_URL));
                    okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    final var dialog = new Dialog();
                    dialog.add(
                            new VerticalLayout(
                                    new Text("Your password was changed successfully."),
                                    new Html("<br/>"),
                                    new Text("Please login using your new password."),
                                    okButton
                            )
                    );
                    dialog.setCloseOnEsc(false);
                    dialog.setCloseOnOutsideClick(false);
                    dialog.addDialogCloseActionListener(closeEvent -> authenticatedUser.logout());
                    dialog.open();
                } catch (final AuthenticationException e) {
                    Notification.show(e.getMessage());
                }
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var form = new FormLayout();
        form.add(oldPassword, newPassword, repeatPassword, saveButton);

        add(title, form);
        oldPassword.focus();
    }
}
