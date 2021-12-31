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

package org.komunumo.ui.view.website.events;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.DatabaseService;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;

@Component
public class EventPreviewFilter implements Filter {

    private final DatabaseService databaseService;

    public EventPreviewFilter(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        var stopFilterChain = false;

        if (request instanceof HttpServletRequest httpServletRequest) {
            final var previewCode = httpServletRequest.getParameter("preview");
            if (previewCode != null && !previewCode.isBlank()) {
                final var uri = httpServletRequest.getRequestURI();
                final var uriElements = uri.split("/");
                if (uriElements.length >= 5 && uriElements[1].equals("event")) {
                    final var location = uriElements[2];
                    final var year = uriElements[3];
                    final var url = uriElements[4];
                    final var event = databaseService.getEventByUrl(location, Year.of(Integer.parseInt(year)), url);
                    if (event.isPresent() && event.get().getPublished() && response instanceof HttpServletResponse httpServletResponse) {
                        httpServletResponse.setStatus(301);
                        httpServletResponse.setHeader("Location", event.get().getCompleteEventUrl());
                        stopFilterChain = true;
                    }
                }
            }
        }

        if (!stopFilterChain) {
            chain.doFilter(request, response);
        }
    }

}
