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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import org.jetbrains.annotations.NotNull;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.data.service.SponsorService;
import org.komunumo.data.service.StatisticService;
import org.komunumo.security.AuthenticatedUser;

@CssImport(value = "./themes/komunumo/views/website/website-layout.css")
public class WebsiteLayout extends Div implements RouterLayout {

    private final Main main;
    private final TwitterFeed twitterFeed;

    public WebsiteLayout(@NotNull final AuthenticatedUser authenticatedUser,
                         @NotNull final DatabaseService databaseService) {
        addClassName("website-container");

        final var website = new VerticalLayout();
        website.addClassName("website");
        add(website);

        website.add(new CookieConsent());
        website.add(new WebsiteMenu(authenticatedUser));
        website.add(new WebsiteHeader(databaseService));

        main = new Main();

        twitterFeed = new TwitterFeed();

        final var mainLayout = new HorizontalLayout(
                new VerticalLayout(
                        main,
                        new SponsorBlock(databaseService)),
                twitterFeed);
        mainLayout.setId("main-layout");
        website.add(mainLayout);
        website.add(new WebsiteFooter(databaseService));

        final var page = UI.getCurrent().getPage();
        page.retrieveExtendedClientDetails(extendedClientDetails -> pageResized(extendedClientDetails.getBodyClientWidth()));
        page.addBrowserWindowResizeListener(event -> pageResized(event.getWidth()));
    }

    private void pageResized(final int width) {
        twitterFeed.setVisible(width >= 1350);
    }

    @Override
    public void showRouterLayoutContent(@NotNull HasElement content) {
        main.removeAll();
        main.add(content.getElement().getComponent()
                .orElseThrow(() -> new IllegalArgumentException(
                        "WebsiteLayout content must be a Component")));
    }

    @Override
    public void removeRouterLayoutContent(@NotNull HasElement oldContent) {
        main.removeAll();
    }

}
