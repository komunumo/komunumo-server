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

import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.ui.view.website.members.MembersView;
import org.komunumo.ui.view.website.sponsors.SponsorsView;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;

@Component
public final class ApplicationServiceInitListener implements VaadinServiceInitListener {

    @Serial
    private static final long serialVersionUID = -238825369839138574L;
    private final DatabaseService databaseService;
    private final Map<String, String> redirects = new HashMap<>();

    public ApplicationServiceInitListener(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;
        reloadRedirects();
    }

    @Override
    public void serviceInit(@NotNull final ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.addRequestHandler((session, request, response) -> {
            if (request instanceof HttpServletRequest httpServletRequest) {
                final var uri = httpServletRequest.getRequestURI();
                if (redirects.containsKey(uri) && response instanceof HttpServletResponse httpServletResponse) {
                    httpServletResponse.setHeader("Location", redirects.get(uri));
                    httpServletResponse.setStatus(SC_MOVED_PERMANENTLY);
                    return true;
                }
            }
            return false;
        });

        databaseService.getPages(PageParent.Members).forEach(
                page -> RouteConfiguration.forApplicationScope().setRoute(
                        page.getCompletePageUrl(), MembersView.class, List.of(WebsiteLayout.class)));
        databaseService.getPages(PageParent.Sponsors).forEach(
                page -> RouteConfiguration.forApplicationScope().setRoute(
                        page.getCompletePageUrl(), SponsorsView.class, List.of(WebsiteLayout.class)));
    }

    public void reloadRedirects() {
        redirects.clear();
        databaseService.getAllRedirects().forEach(record -> redirects.put(record.getOldUrl(), record.getNewUrl()));
    }

}
