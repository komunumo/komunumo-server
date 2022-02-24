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

package org.komunumo.ui.view.website.members;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.SubMenu;
import org.komunumo.ui.view.website.SubMenuItem;
import org.komunumo.ui.view.website.WebsiteLayout;

@Route(value = "members", layout = WebsiteLayout.class)
@RouteAlias(value = "members/feedback", layout = WebsiteLayout.class)
@CssImport("./themes/komunumo/views/website/members-view.css")
@AnonymousAllowed
public final class MembersView extends ContentBlock implements BeforeEnterObserver, AfterNavigationObserver {

    private final DatabaseService databaseService;

    public MembersView(@NotNull final DatabaseService databaseService) {
        super("Members");
        this.databaseService = databaseService;
        addClassName("members-view");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation().getPath().equalsIgnoreCase("members")) {
            beforeEnterEvent.forwardTo("/members/general");
        }
    }

    @Override
    public void afterNavigation(@NotNull final AfterNavigationEvent afterNavigationEvent) {
        final var url = afterNavigationEvent.getLocation().getPath();
        final var subMenu = new SubMenu();
        databaseService.getPages(PageParent.Members).forEach(page ->
                subMenu.add(new SubMenuItem(page.getCompletePageUrl(), page.getTitle(), url.equals(page.getCompletePageUrl()))));
        subMenu.add(new SubMenuItem("/members/feedback", "Feedback", url.equals("members/feedback")));
        setSubMenu(subMenu);
        if (url.equals("members/feedback")) {
            setContent(createFeedbackForm());
        } else if (url.contains("/")) {
            final var page = loadPage(databaseService, url);
            this.getUI().ifPresent(ui -> ui.getPage().setTitle("%s: %s"
                    .formatted(databaseService.configuration().getWebsiteName(), page != null ? page.getTitle() : "Members")));
        }
    }

    private Component createFeedbackForm() {
        final var div = new Div();
        div.addClassName("feedback-form");
        div.add(new H2("Your Feedback, suggestion, idea..."));

        final var firstName = new TextField("First name");
        firstName.setMinLength(1);
        firstName.setMaxLength(2_000);
        final var lastName = new TextField("Last name");
        lastName.setMinLength(1);
        lastName.setMaxLength(2_000);
        final var email = new EmailField("Email");
        email.setMinLength(1);
        email.setMaxLength(2_000);
        final var feedback = new TextArea("Feedback");
        feedback.setMinLength(1);
        feedback.setMaxLength(2_000);
        final var submit = new Button("Send");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.setEnabled(false);
        submit.setDisableOnClick(true);

        List.of(firstName, lastName, email, feedback).forEach(field -> {
            field.setRequiredIndicatorVisible(true);
            field.setValueChangeMode(ValueChangeMode.EAGER);
            field.addValueChangeListener(valueChangeEvent -> submit.setEnabled(
                    !firstName.getValue().isBlank()
                    && !lastName.getValue().isBlank()
                    && !email.isInvalid()
                    && !feedback.getValue().isBlank()));
        });

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.add(firstName, lastName, email, feedback, submit);
        div.add(form);

        submit.addClickListener(buttonClickEvent -> {
            databaseService.receiveFeedback(firstName.getValue(), lastName.getValue(), email.getValue(), feedback.getValue());
            div.replace(form, new Paragraph("We have received your feedback, thank you very much!"));
        });

        firstName.focus();

        return div;
    }

}
