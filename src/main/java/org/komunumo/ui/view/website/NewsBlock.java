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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.NewsEntity;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.More;

@CssImport("./themes/komunumo/views/website/news-block.css")
public class NewsBlock extends ContentBlock {

    public NewsBlock(@NotNull final DatabaseService databaseService) {
        super("News");
        addClassName("news-block");

        final var newsEntity = databaseService.getLatestNews();
        if (newsEntity == null) {
            setContent(createNewsletterForm(databaseService));
        } else {
            setContent(new HorizontalLayout(createNewsContent(newsEntity), createNewsletterForm(databaseService)));
        }
    }

    private Component createNewsContent(@NotNull final NewsEntity newsEntity) {
        final var detailLink = "/news/%d".formatted(newsEntity.id());

        final var container = new Div();
        container.add(new Anchor(detailLink, new H2(newsEntity.title())));
        if (!newsEntity.subtitle().isBlank()) {
            container.add(new Anchor(detailLink, new H3(newsEntity.subtitle())));
        }
        container.add(new Html("<div>%s</div>".formatted(newsEntity.teaser())));
        container.add(new More(detailLink));
        return container;
    }

    private Component createNewsletterForm(@NotNull final DatabaseService databaseService) {
        final var emailField = new EmailField();
        emailField.setPlaceholder("Your email address");
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        emailField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        final var message = new Div();
        message.setId("newsletter-message");

        final var subscribeButton = new Button("Subscribe", (clickEvent) -> {
            final var emailAddress = emailField.getValue().trim();
            if (!emailAddress.isBlank()) {
                final var subscriptionStatus = databaseService.addSubscription(emailAddress);
                final var infoMessage = switch (subscriptionStatus) {
                    case PENDING -> "You have been added to the newsletter. Please check your email account for verification (opt-in).";
                    case ACTIVE -> "You are already subscribed to the newsletter. If you don't receive it, check your spam folder.";
                };
                UI.getCurrent().access(() -> {
                    emailField.setValue("");
                    message.add(new Paragraph(infoMessage));
                });
            }
        });
        subscribeButton.setEnabled(false);
        subscribeButton.setDisableOnClick(true);
        subscribeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        emailField.addValueChangeListener((changeEvent) -> subscribeButton.setEnabled(!emailField.isInvalid()));

        final var container = new Div(
                new H2("Stay informed about events"),
                new Div(
                    new Paragraph("Please register here with your e-mail to receive announcements for upcoming JUG Switzerland events."),
                    emailField, subscribeButton
                ),
                message
        );
        container.addClassName("newsletter-registration");
        return container;
    }

}
