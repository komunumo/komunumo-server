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

package org.komunumo.data;

import com.vaadin.flow.spring.annotation.SpringComponent;

import java.time.LocalDateTime;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.AuthService;
import org.komunumo.data.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class DataInitializer {

    @Value("${komunumo.admin.email:}")
    private String adminEmail;

    @Bean
    public CommandLineRunner initializeData(
            @NotNull final MemberService memberService,
            @NotNull final AuthService authService) {
        return args -> {
            if (adminEmail != null && !adminEmail.isBlank()) {
                final var member = memberService.getByEmail(adminEmail);
                if (member.isPresent()) {
                    final var admin = member.get();
                    if (!admin.getAdmin()) {
                        admin.setAdmin(true);
                        memberService.store(admin);
                    }
                } else {
                    final var admin = memberService.newRecord();
                    admin.setFirstName("Admin");
                    admin.setLastName("Admin");
                    admin.setEmail(adminEmail);
                    admin.setAdmin(true);
                    admin.setActive(true);
                    admin.setMemberSince(LocalDateTime.now());
                    memberService.store(admin);
                    authService.resetPassword(adminEmail);
                }
            }
        };
    }

}
