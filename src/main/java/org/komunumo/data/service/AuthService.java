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

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import org.apache.commons.lang3.RandomStringUtils;
import org.komunumo.data.entity.Member;
import org.komunumo.views.dashboard.DashboardView;
import org.komunumo.views.events.EventsView;
import org.komunumo.views.login.ActivationView;
import org.komunumo.views.login.LoginView;
import org.komunumo.views.logout.LogoutView;
import org.komunumo.views.members.MembersView;
import org.komunumo.views.sponsors.SponsorsView;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@SpringComponent
public class AuthService implements VaadinServiceInitListener {

    public static class AccessDeniedException extends Exception {}

    private final MemberRepository memberRepository;
    private final MailSender mailSender;

    public AuthService(final MemberRepository memberRepository, final MailSender mailSender) {
        this.memberRepository = memberRepository;
        this.mailSender = mailSender;
    }

    public void authenticate(final String email, final String password) throws AccessDeniedException {
        final var member = memberRepository.getByEmail(email);
        if (member != null && member.isActive() && member.checkPassword(password)) {
            VaadinSession.getCurrent().setAttribute(Member.class, member);
        } else {
            throw new AccessDeniedException();
        }
    }

    public void register(final String firstName, final String lastName, final String email,
                         final String address, final String zipCode, final String city,
                         final String state, final String country) {
        final var member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setAddress(address);
        member.setZipCode(zipCode);
        member.setCity(city);
        member.setState(state);
        member.setCountry(country);
        member.setMemberSince(LocalDate.now());
        member.setAdmin(false);
        member.setActive(false);
        member.setActivationCode(RandomStringUtils.randomAlphanumeric(32));
        memberRepository.save(member);

        final var text = "http://localhost:8080/activate?email=%s&code=%s"
                .formatted(member.getEmail(), member.getActivationCode());
        final var message = new SimpleMailMessage();
        message.setTo(member.getEmail());
        message.setFrom("noreply@example.com"); // TODO configurable: info@jug.ch
        message.setSubject("Activate your account");
        message.setText(text);
        mailSender.send(message);
    }

    public void activate(final String email, final String activationCode) throws AccessDeniedException {
        final var member = memberRepository.getByEmail(email);
        if (member != null && member.getActivationCode().equals(activationCode)) {
            member.setActive(true);
            memberRepository.save(member);
        } else {
            throw new AccessDeniedException();
        }
    }

    public boolean isUserLoggedIn() {
        return VaadinSession.getCurrent().getAttribute(Member.class) != null;
    }

    public boolean isAccessGranted(final Class<?> navigationTarget) {
        final var member = VaadinSession.getCurrent().getAttribute(Member.class);

        // restrict to members
        if (member != null) {
            if (navigationTarget == DashboardView.class
                    || navigationTarget == LogoutView.class) {
                return true;
            }

            // restrict to admins
            if (member.isAdmin()) {
                if (navigationTarget == EventsView.class
                        || navigationTarget == MembersView.class
                        || navigationTarget == SponsorsView.class) {
                    return true;
                }
            }
        } else {
            if (navigationTarget == ActivationView.class) {
                return true;
            }
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

}
