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
import org.jetbrains.annotations.NotNull;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.service.MemberService;
import org.komunumo.security.SecurityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class DataInitializer {

    @Bean
    public CommandLineRunner createAdminAccount(
            @NotNull final Configuration configuration,
            @NotNull final SecurityService securityService,
            @NotNull final MemberService memberService) {
        return args -> {
            final var admin = configuration.getAdmin();
            if (admin != null && admin.getEmail() != null) {
                final var member = memberService.getByEmail(admin.getEmail());
                if (member.isPresent()) {
                    final var record = member.get();
                    if (!record.getAdmin()) {
                        record.setAdmin(true);
                        memberService.store(record);
                    }
                } else {
                    final var record = memberService.newMember();
                    record.setFirstName("Admin");
                    record.setLastName("Admin");
                    record.setEmail(admin.getEmail());
                    record.setAdmin(true);
                    record.setAccountActive(true);
                    memberService.store(record);
                    securityService.resetPassword(admin.getEmail());
                }
            }
        };
    }

}
