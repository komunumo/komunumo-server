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

package org.komunumo;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.RedirectService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    private final RedirectService redirectService;
    private final Map<String, String> redirects = new HashMap<>();

    public ApplicationServiceInitListener(@NotNull final RedirectService redirectService) {
        this.redirectService = redirectService;
        reloadRedirects();
    }

    @Override
    public void serviceInit(@NotNull final ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.addRequestHandler((session, request, response) -> {
            if (request instanceof HttpServletRequest httpServletRequest) {
                final var uri = httpServletRequest.getRequestURI();
                if (redirects.containsKey(uri) && response instanceof HttpServletResponse httpServletResponse) {
                    httpServletResponse.setHeader("Location", redirects.get(uri));
                    httpServletResponse.setStatus(301);
                    return true;
                }
            }
            return false;
        });
    }

    public void reloadRedirects() {
        redirects.clear();
        redirectService.getAllRedirects().forEach(record -> redirects.put(record.getOldUrl(), record.getNewUrl()));
    }

}
