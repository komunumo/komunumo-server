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
import java.time.LocalDateTime;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.views.admin.dashboard.DashboardView;
import org.komunumo.views.admin.events.EventsView;
import org.komunumo.views.admin.members.MembersView;
import org.komunumo.views.admin.speakers.SpeakersView;
import org.komunumo.views.admin.sponsors.SponsorsView;
import org.komunumo.views.login.ActivationView;
import org.komunumo.views.login.LoginView;
import org.komunumo.views.login.ChangePasswordView;
import org.komunumo.views.logout.LogoutView;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

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

    private final MemberService memberService;
    private final MailSender mailSender;

    public AuthService(final MemberService memberService, final MailSender mailSender) {
        this.memberService = memberService;
        this.mailSender = mailSender;
    }

    public void authenticate(final String email, final String password) throws AccessDeniedException {
        final var member = memberService.getByEmail(email).orElse(null);
        if (member != null && member.getActive() && checkPassword(member, password)) {
            VaadinSession.getCurrent().setAttribute(MemberRecord.class, member);
            if (member.getPasswordChange()) {
                UI.getCurrent().navigate(ChangePasswordView.class);
            } else {
                UI.getCurrent().getPage().reload();
            }
        } else {
            throw new AccessDeniedException("Access denied.");
        }
    }

    private boolean checkPassword(final MemberRecord member, final String password) {
        return getPasswordHash(password, member.getPasswordSalt()).equals(member.getPasswordHash());
    }

    public void register(final String firstName, final String lastName, final String email,
                         final String address, final String zipCode, final String city,
                         final String state, final String country) {
        final var member = new MemberRecord();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setAddress(address);
        member.setZipCode(zipCode);
        member.setCity(city);
        member.setState(state);
        member.setCountry(country);
        member.setMemberSince(LocalDateTime.now());
        member.setAdmin(false);
        member.setActive(false);
        member.setActivationCode(RandomStringUtils.randomAlphanumeric(32));
        memberService.store(member);

        final var text = String.format("http://localhost:8080/activate?email=%s&code=%s", // TODO configure server URL
                member.getEmail(), member.getActivationCode());
        final var message = new SimpleMailMessage();
        message.setTo(member.getEmail());
        message.setFrom("noreply@example.com"); // TODO configurable: info@jug.ch
        message.setSubject("Activate your account");
        message.setText(text);
        mailSender.send(message);
    }

    public void activate(final String email, final String activationCode) throws AccessDeniedException {
        final var member = memberService.getByEmail(email).orElse(null);
        if (member != null && member.getActivationCode().equals(activationCode)) {
            member.setActive(true);
            memberService.store(member);
        } else {
            throw new AccessDeniedException("Activation failed");
        }
    }

    public void resetPassword(final String email) {
        final var member = memberService.getByEmail(email);
        if (member.isPresent()) {
            final var record = member.get();
            if (record.getActive()) {
                final var password = RandomStringUtils.randomAscii(32);
                final var passwordSalt = createPasswordSalt();
                final var passwordHash = getPasswordHash(password, passwordSalt);
                record.setPasswordSalt(passwordSalt);
                record.setPasswordHash(passwordHash);
                record.setPasswordChange(true);
                memberService.store(record);

                final var message = new SimpleMailMessage();
                message.setTo(email);
                message.setFrom("noreply@example.com"); // TODO configurable: info@jug.ch
                message.setSubject("Reset your password");
                message.setText("To reset your password, use the following one time password to login: " + password);
                mailSender.send(message);
            }
        }
    }

    public void changePassword(final String oldPassword, final String newPassword) throws AccessDeniedException {
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
        return VaadinSession.getCurrent().getAttribute(MemberRecord.class) != null;
    }

    public MemberRecord getCurrentUser() {
        return VaadinSession.getCurrent().getAttribute(MemberRecord.class);
    }

    public boolean isAccessGranted(final Class<?> navigationTarget) {
        final var member = VaadinSession.getCurrent().getAttribute(MemberRecord.class);

        // restrict to members
        if (member != null && member.getActive()) {
            if (navigationTarget == DashboardView.class // TODO only for admins / use profile page for members instead
                    || navigationTarget == ChangePasswordView.class
                    || navigationTarget == LogoutView.class) {
                return true;
            }

            // restrict to admins
            if (member.getAdmin()) {
                if (navigationTarget == EventsView.class
                        || navigationTarget == MembersView.class
                        || navigationTarget == SpeakersView.class
                        || navigationTarget == SponsorsView.class) {
                    return true;
                }
            }
        } else {
            if (navigationTarget == ActivationView.class) {
                return true;
            }
        }

        if (navigationTarget == LoginView.class) {
            return true;
        }

        // deny access to all other Komunumo views
        return !navigationTarget.getPackageName().startsWith("org.komunumo");
    }

    @Override
    public void serviceInit(final ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final var ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter);
        });
    }

    private void beforeEnter(final BeforeEnterEvent event) {
        final boolean accessGranted = isAccessGranted(event.getNavigationTarget());
        if (!accessGranted) {
            if (isUserLoggedIn()) {
                event.rerouteToError(AccessDeniedException.class);
            } else {
                event.rerouteTo(LoginView.class);
            }
        }
    }

    public String createPasswordSalt() {
        return RandomStringUtils.randomAscii(32);
    }

    public String getPasswordHash(final String password, final String passwordSalt) {
        return DigestUtils.sha1Hex(password + passwordSalt);
    }

    public void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        VaadinSession.getCurrent().close();
        UI.getCurrent().getPage().setLocation("login");
    }

}
