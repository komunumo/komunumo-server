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

package org.komunumo.ui.view.website;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jetbrains.annotations.NotNull;
import org.komunumo.configuration.Configuration;

public class WebsiteFooter extends Footer {

    private final Configuration configuration;

    public WebsiteFooter(@NotNull Configuration configuration) {
        this.configuration = configuration;

        setId("website-footer");

        add(
                createAbout()
        );
    }

    private Component createAbout() {
        final var layout = new HorizontalLayout();
        layout.setId("website-footer-about");

        final var title = new Div(new H2("About"));
        final var about = new Html("<div>%s</div>".formatted(configuration.getClient().getAbout()));
        layout.add(new HorizontalLayout(title, about));

        return layout;
    }

}
