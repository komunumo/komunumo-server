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

package org.komunumo.data.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.entity.Member;
import org.komunumo.ui.view.admin.dashboard.DashboardView;
import org.komunumo.ui.view.admin.events.EventsView;
import org.komunumo.ui.view.admin.imports.ImportsView;
import org.komunumo.ui.view.admin.keywords.KeywordsView;
import org.komunumo.ui.view.admin.members.MembersView;
import org.komunumo.ui.view.admin.speakers.SpeakersView;
import org.komunumo.ui.view.admin.sponsors.SponsorsView;
import org.komunumo.ui.view.login.ActivationView;
import org.komunumo.ui.view.login.BlockedView;
import org.komunumo.ui.view.login.ChangePasswordView;
import org.komunumo.ui.view.login.LoginView;
import org.komunumo.ui.view.logout.LogoutView;
import org.komunumo.ui.view.website.home.HomeView;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService implements VaadinServiceInitListener {

    public static class AccessDeniedException extends Exception {
        @SuppressWarnings("unused") // used by reflection from Vaadin
        public AccessDeniedException() {
            super();
        }

        public AccessDeniedException(final String message) {
            super(message);
        }
    }

    private final Configuration configuration;
    private final MemberService memberService;
    private final MailSender mailSender;

    public AuthService(@NotNull final Configuration configuration,
                       @NotNull final MemberService memberService,
                       @NotNull final MailSender mailSender) {
        this.configuration = configuration;
        this.memberService = memberService;
        this.mailSender = mailSender;
    }

    public void authenticate(@NotNull final String email,
                             @NotNull final String password)
            throws AccessDeniedException {
        final var member = memberService.getByEmail(email).orElse(null);
        if (member != null && member.getAccountActive() && checkPassword(member, password)) {
            VaadinSession.getCurrent().setAttribute(Member.class, member);
            if (member.getAccountBlocked()) {
                UI.getCurrent().navigate(BlockedView.class);
            } else if (member.getPasswordChange()) {
                UI.getCurrent().navigate(ChangePasswordView.class);
            } else {
                UI.getCurrent().getPage().reload();
            }
        } else {
            throw new AccessDeniedException("Access denied.");
        }
    }

    private boolean checkPassword(@NotNull final Member member,
                                  @NotNull final String password) {
        return getPasswordHash(password, member.getPasswordSalt()).equals(member.getPasswordHash());
    }

    public void register(@NotNull final String firstName, @NotNull final String lastName, @NotNull final String email,
                         @Nullable final String address, @Nullable final String zipCode, @Nullable final String city,
                         @Nullable final String state, @Nullable final String country) {
        final var member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setAddress(address);
        member.setZipCode(zipCode);
        member.setCity(city);
        member.setState(state);
        member.setCountry(country);
        member.setRegistrationDate(LocalDateTime.now());
        member.setAdmin(false);
        member.setAccountActive(false);
        member.setActivationCode(RandomStringUtils.randomAlphanumeric(32));
        memberService.store(member);

        final var text = String.format("http://localhost:8080/activate?email=%s&code=%s", // TODO configure server URL
                member.getEmail(), member.getActivationCode());
        final var message = new SimpleMailMessage();
        message.setTo(member.getEmail());
        message.setFrom(configuration.getEmail().getAddress());
        message.setSubject("Activate your account");
        message.setText(text);
        mailSender.send(message);
    }

    public void activate(@NotNull final String email,
                         @NotNull final String activationCode)
            throws AccessDeniedException {
        final var member = memberService.getByEmail(email).orElse(null);
        if (member != null && member.getActivationCode().equals(activationCode)) {
            member.setAccountActive(true);
            memberService.store(member);
        } else {
            throw new AccessDeniedException("Activation failed");
        }
    }

    public void resetPassword(@NotNull final String email) {
        final var member = memberService.getByEmail(email);
        if (member.isPresent()) {
            final var record = member.get();
            if (record.getAccountActive()) {
                final var password = RandomStringUtils.randomAscii(32);
                final var passwordSalt = createPasswordSalt();
                final var passwordHash = getPasswordHash(password, passwordSalt);
                record.setPasswordSalt(passwordSalt);
                record.setPasswordHash(passwordHash);
                record.setPasswordChange(true);
                memberService.store(record);

                final var message = new SimpleMailMessage();
                message.setTo(email);
                message.setFrom(configuration.getEmail().getAddress());
                message.setSubject("Reset your password");
                message.setText("To reset your password, use the following one time password to login: " + password);
                mailSender.send(message);
            }
        }
    }

    public void changePassword(@NotNull final String oldPassword,
                               @NotNull final String newPassword)
            throws AccessDeniedException {
        final var member = getCurrentUser();
        if (checkPassword(member, oldPassword)) {
            final var passwordSalt = createPasswordSalt();
            final var passwordHash = getPasswordHash(newPassword, passwordSalt);
            member.setPasswordSalt(passwordSalt);
            member.setPasswordHash(passwordHash);
            member.setPasswordChange(false);
            memberService.store(member);
        } else {
            throw new AccessDeniedException("Password change denied!");
        }
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public Member getCurrentUser() {
        return VaadinSession.getCurrent().getAttribute(Member.class);
    }

    @SuppressWarnings({"java:S3776"}) // cognitive complexity
    public boolean isAccessGranted(final Class<?> navigationTarget) {
        final var member = getCurrentUser();

        // restrict to members
        if (member != null && member.getAccountActive() && !member.getAccountBlocked()) {
            if (navigationTarget == DashboardView.class // TODO only for admins / use profile page for members instead
                    || navigationTarget == ChangePasswordView.class
                    || navigationTarget == LogoutView.class) {
                return true;
            }

            // restrict to admins
            if (member.getAdmin() && (navigationTarget == EventsView.class
                    || navigationTarget == KeywordsView.class
                    || navigationTarget == MembersView.class
                    || navigationTarget == SpeakersView.class
                    || navigationTarget == SponsorsView.class
                    || navigationTarget == ImportsView.class)) {
                return true;
            }
        } else if (member != null && member.getAccountBlocked()) {
            if (navigationTarget == BlockedView.class) {
                return true;
            }
        } else {
            if (navigationTarget == ActivationView.class) {
                return true;
            }
        }

        if (navigationTarget == HomeView.class
                || navigationTarget == org.komunumo.ui.view.website.events.EventsView.class
                || navigationTarget == LoginView.class) {
            return true;
        }

        // deny access to all other Komunumo views
        return !navigationTarget.getPackageName().startsWith("org.komunumo");
    }

    @Override
    public void serviceInit(@NotNull final ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final var ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter);
        });
    }

    private void beforeEnter(@NotNull final BeforeEnterEvent event) {
        final boolean accessGranted = isAccessGranted(event.getNavigationTarget());
        if (!accessGranted) {
            if (isUserLoggedIn()) {
                event.rerouteToError(AccessDeniedException.class); // TODO redirect to default page
            } else {
                event.rerouteTo(LoginView.class);
            }
        }
    }

    public String createPasswordSalt() {
        return RandomStringUtils.randomAscii(32);
    }

    public String getPasswordHash(@NotNull final String password,
                                  @NotNull final String passwordSalt) {
        return DigestUtils.sha1Hex(password + passwordSalt);
    }

    public void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        VaadinSession.getCurrent().close();
        UI.getCurrent().getPage().setLocation("/");
    }

}
