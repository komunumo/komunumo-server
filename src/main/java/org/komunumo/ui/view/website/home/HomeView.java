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

package org.komunumo.ui.view.website.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.website.WebsiteLayout;
import org.komunumo.ui.view.website.home.component.EventPreview;

@Route(value = "", layout = WebsiteLayout.class)
@PageTitle("Home")
public class HomeView extends Div {

    private final EventService eventService;


    public HomeView(EventService eventService) {
        this.eventService = eventService;

        add(
                createEventPreviews()
        );
    }

    private Component createEventPreviews() {
        final var div = new Div();
        eventService.upcomingEvents()
                .map(EventPreview::new)
                .forEach(div::add);
        return div;
    }
}
