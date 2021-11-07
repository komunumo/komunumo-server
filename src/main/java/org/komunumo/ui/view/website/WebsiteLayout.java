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

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import org.jetbrains.annotations.NotNull;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.service.StatisticService;

@CssImport(value = "./themes/komunumo/views/website/website-layout.css")
public class WebsiteLayout extends VerticalLayout implements RouterLayout {

    private final Main main;

    public WebsiteLayout(@NotNull final Configuration configuration,
                         @NotNull final StatisticService statisticService) {
        add(new CookieConsent());
        add(new WebsiteHeader(configuration, statisticService));

        main = new Main();
        main.addClassName("website");

        final var twitterFeed = new TwitterFeed();

        final var mainLayout = new HorizontalLayout(main, twitterFeed);
        mainLayout.setId("main-layout");
        add(mainLayout);
    }

    public void showRouterLayoutContent(@NotNull HasElement content) {
        main.removeAll();
        main.add(content.getElement().getComponent()
                .orElseThrow(() -> new IllegalArgumentException(
                        "WebsiteLayout content must be a Component")));
    }

    public void removeRouterLayoutContent(@NotNull HasElement oldContent) {
        main.removeAll();
    }

}
