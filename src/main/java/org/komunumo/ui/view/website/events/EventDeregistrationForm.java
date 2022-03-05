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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.DatabaseService;

import static org.komunumo.util.FormatterUtil.formatDate;

@CssImport("./themes/komunumo/views/website/event-deregistration-form.css")
public class EventDeregistrationForm extends Div {

    @Serial
    private static final long serialVersionUID = 4831112167632344866L;

    public EventDeregistrationForm(@NotNull final DatabaseService databaseService,
                                   @NotNull final Event event,
                                   @NotNull final String deregisterCode) {
        addClassName("event-deregistration-form");

        final var eventTitle = new Span(event.getTitle());
        eventTitle.addClassName("event-title");

        final var registration = databaseService.getRegistration(deregisterCode);
        if (registration != null) {
            final var deregisterButton = new Button("Deregister");
            deregisterButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            deregisterButton.setDisableOnClick(true);

            final var deregisterForm = new Div();
            deregisterForm.add(new Paragraph(
                    new Span("I want to deregister from the event «"), eventTitle,
                    new Span("» on %s in %s:".formatted(formatDate(event.getDate().toLocalDate()), event.getLocation()))));
            deregisterForm.add(deregisterButton);

            add(deregisterForm);

            deregisterButton.addClickListener(clickEvent -> {
                final var message = new Div();
                if (databaseService.deregisterFromEvent(deregisterCode)) {
                    final var success = new Paragraph("You successfully deregistered from this event.");
                    success.addClassName("success");
                    message.add(success);
                } else {
                    final var error = new Paragraph("Sorry, there was a problem deregistering you from this event. Please try again later.");
                    error.addClassName("error");
                    message.add(error);
                }
                replace(deregisterForm, message);
            });
        } else {
            final var error = new Paragraph("The link you have used is invalid. "
                    + "Check your email for the registration confirmation and click on the provided deregister link.");
            error.addClassName("error");
            add(error);
        }
    }
}
