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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventService;
import org.springframework.stereotype.Component;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    private final EventService eventService;
    private final Map<String, Long> eventRedirectUrls = new HashMap<>();

    public ApplicationServiceInitListener(@NotNull final EventService eventService) {
        this.eventService = eventService;
        eventService.getEventRedirectUrls().forEach(record -> eventRedirectUrls.put(record.getUrlJug(), record.getEventId()));
    }

    @Override
    public void serviceInit(@NotNull final ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.addRequestHandler((session, request, response) -> {
            if (request instanceof HttpServletRequest httpServletRequest) {
                final var uri = httpServletRequest.getRequestURI();
                if (eventRedirectUrls.containsKey(uri) && response instanceof HttpServletResponse httpServletResponse) {
                    final var event = eventService.get(eventRedirectUrls.get(uri));
                    if (event.isPresent()) {
                        httpServletResponse.setHeader("Location", event.get().getCompleteEventUrl());
                        httpServletResponse.setStatus(301);
                        return true;
                    }
                }
            }
            return false;
        });
    }

}
