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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.MemberService;

import static org.komunumo.util.FormatterUtil.formatDate;

@CssImport("./themes/komunumo/views/website/event-registration-form.css")
public class EventRegistrationForm extends Div {

    public EventRegistrationForm(@NotNull final MemberService memberService,
                                 @NotNull final EventMemberService eventMemberService,
                                 @NotNull final Event event) {
        addClassName("event-registration-form");

        final var emailField = new EmailField();
        emailField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        emailField.setPlaceholder("Your email address");
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        final var verifyButton = new Button("Verify");
        verifyButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        verifyButton.setEnabled(!emailField.getValue().isBlank() && !emailField.isInvalid());
        verifyButton.setDisableOnClick(true);
        emailField.addValueChangeListener(valueChangeEvent ->
                verifyButton.setEnabled(!valueChangeEvent.getValue().isBlank() && !emailField.isInvalid()));

        final var eventTitle = new Span(event.getTitle());
        eventTitle.addClassName("event-title");

        final var emailForm = new Div();
        emailForm.add(new Paragraph(
                new Span("I want to register for the event «"), eventTitle,
                new Span("» on %s in %s:".formatted(formatDate(event.getDate().toLocalDate()), event.getLocation()))));
        emailForm.add(new HorizontalLayout(emailField, verifyButton));


        add(emailForm);

        verifyButton.addClickListener(verifyButtonClickEvent -> {
            final var registrationForm = new Div();
            final var memberFound = memberService.getByEmail(emailField.getValue().trim());

            final var foundMessage = new Paragraph();
            foundMessage.addClassName("found-message");
            registrationForm.add(foundMessage);

            if (memberFound.isPresent()) {
                final var member = memberFound.get();
                foundMessage.add(new Text("Yes! We found you in our database, %s.".formatted(member.getFullName())), new Html("<br/>"));
                foundMessage.add(new Text("Please complete your registration by sending off this form. Thank you very much!"));

                final var source = new RadioButtonGroup<String>();
                source.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
                source.setLabel("I heard of this event through");
                source.setItems("jug.ch mailing", "jug.ch homepage", "Twitter", "mailing of another association", "a friend", "company",
                        "techup.ch", "search engine", "other");
                registrationForm.add(source);

                final var otherSource = new TextField();
                otherSource.addClassName("other-source");
                otherSource.addThemeVariants(TextFieldVariant.LUMO_SMALL);
                otherSource.setEnabled(source.getValue() != null && source.getValue().equalsIgnoreCase("other"));
                otherSource.setValueChangeMode(ValueChangeMode.EAGER);
                registrationForm.add(otherSource);

                final var registerButton = new Button("Register");
                registerButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                registerButton.setEnabled(false);
                registerButton.setDisableOnClick(true);
                registrationForm.add(registerButton);

                source.addValueChangeListener(valueChangeEvent -> {
                    registerButton.setEnabled(
                            valueChangeEvent.getValue() != null && (!valueChangeEvent.getValue().equalsIgnoreCase("other") ||
                            (valueChangeEvent.getValue().equalsIgnoreCase("other") && !otherSource.getValue().isBlank())));
                    if (valueChangeEvent.getValue() != null && valueChangeEvent.getValue().equalsIgnoreCase("other")) {
                        otherSource.setEnabled(true);
                        otherSource.focus();
                    } else {
                        otherSource.setEnabled(false);
                    }
                });
                otherSource.addValueChangeListener(valueChangeEvent -> registerButton.setEnabled(!valueChangeEvent.getValue().isBlank()));

                registerButton.addClickListener(registerButtonClickEvent -> {
                    final var sourceValue = source.getValue().equalsIgnoreCase("other") ?
                            otherSource.getValue() : source.getValue();
                    eventMemberService.registerForEvent(event, member, sourceValue);
                    final var registrationInfo = new Paragraph("Thank you for your registration! Within the next few minutes " +
                            "you will receive a copy of your registration and a reminder will follow shortly before the event.");
                    registrationInfo.addClassName("registration-info");
                    replace(registrationForm, registrationInfo);
                });
            } else {
                foundMessage.add("Sorry, we did not found you in our database. This event is for members only.");
            }

            replace(emailForm, registrationForm);
        });
    }
}
