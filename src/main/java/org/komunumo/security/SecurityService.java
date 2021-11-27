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

package org.komunumo.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.MemberService;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityService implements UserDetailsService {

    private final MemberService memberService;
    private final Configuration configuration;
    private final MailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUser authenticatedUser;

    public SecurityService(@NotNull final MemberService memberService,
                           @NotNull final Configuration configuration,
                           @NotNull final MailSender mailSender,
                           @NotNull final PasswordEncoder passwordEncoder,
                           @NotNull final AuthenticatedUser authenticatedUser) {
        this.memberService = memberService;
        this.configuration = configuration;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public UserDetails loadUserByUsername(@NotNull final String email) throws UsernameNotFoundException {
        final var optionalMember = memberService.getByEmail(email);
        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("No member present with email: " + email);
        } else {
            final var member = optionalMember.get();
            return new User(member.getEmail(), member.getPasswordHash(), getAuthorities(member));
        }
    }

    private static List<GrantedAuthority> getAuthorities(@NotNull final Member member) {
        return member.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

    }

    public void resetPassword(@NotNull final String email) {
        final var member = memberService.getByEmail(email);
        if (member.isPresent()) {
            final var record = member.get();
            if (record.getAccountActive()) {
                final var password = RandomStringUtils.randomAscii(32);
                final var passwordHash = passwordEncoder.encode(password);
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
                               @NotNull final String newPassword) {
        final var member = authenticatedUser.get()
                .orElseThrow(() -> new InsufficientAuthenticationException("Password change denied!"));
        if (passwordEncoder.matches(oldPassword, member.getPasswordHash())) {
            final var newPasswordHash = passwordEncoder.encode(newPassword);
            member.setPasswordHash(newPasswordHash);
            member.setPasswordChange(false);
            memberService.store(member);
        } else {
            throw new BadCredentialsException("Password change denied!");
        }
    }

    public void activate(@NotNull final String email,
                         @NotNull final String activationCode) {
        final var member = memberService.getByEmail(email).orElse(null);
        if (member != null && member.getActivationCode().equals(activationCode)) {
            member.setAccountActive(true);
            memberService.store(member);
        } else {
            throw new BadCredentialsException("Activation failed");
        }
    }

}
