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

package org.komunumo.ui.view.website.newsletter;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.service.SubscriptionService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.WebsiteLayout;

import java.util.List;

import org.komunumo.ui.view.website.home.HomeView;

@Route(value = "newsletter/subscription/validation", layout = WebsiteLayout.class)
@PageTitle("Newsletter Subscription Validation")
@CssImport("./themes/komunumo/views/website/newsletter-subscription-view.css")
@AnonymousAllowed
public class NewsletterSubscriptionValidationView extends ContentBlock implements HasUrlParameter<String> {

    private final SubscriptionService subscriptionService;

    public NewsletterSubscriptionValidationView(@NotNull final SubscriptionService subscriptionService) {
        super("News");
        this.subscriptionService = subscriptionService;
        addClassName("newsletter-subscription-validation");
    }

    @Override
    public void setParameter(@NotNull final BeforeEvent beforeEvent,
                             @Nullable @OptionalParameter final String parameter) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parameters = queryParameters.getParameters();
        final var emailAddress = parameters.getOrDefault("email", List.of("")).get(0);
        final var validationCode = parameters.getOrDefault("code", List.of("")).get(0);

        final var validationOkay = subscriptionService.validateSubscription(emailAddress, validationCode);

        final var content = new Div();
        content.add(new H3("Newsletter Subscription Validation"));
        content.add(new Html(
                validationOkay ? "<p class=\"successful\">You successfully subscribed to our newsletter.</p>" :
                        "<p class=\"failed\">Your subscription was not successful.<br/>Check the link in the email you received shortly.</p>"
        ));
        content.add(new RouterLink("Back to home page", HomeView.class));
        setContent(content);
    }

}
