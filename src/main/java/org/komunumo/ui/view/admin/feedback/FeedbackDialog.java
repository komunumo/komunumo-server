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

package org.komunumo.ui.view.admin.feedback;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.FeedbackRecord;
import org.komunumo.ui.component.DateTimePicker;
import org.komunumo.ui.component.EditDialog;

public class FeedbackDialog extends EditDialog<FeedbackRecord> {

    public FeedbackDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<FeedbackRecord> binder) {
        final var received = new DateTimePicker("Received");
        final var firstName = new TextField("First name");
        final var lastName = new TextField("Last name");
        final var email = new TextField("Email");
        final var feedback = new TextArea("Feedback");

        formLayout.add(received, firstName, lastName, email, feedback);

        binder.forField(received).bind(FeedbackRecord::getReceived, FeedbackRecord::setReceived);
        binder.forField(firstName).bind(FeedbackRecord::getFirstName, FeedbackRecord::setFirstName);
        binder.forField(lastName).bind(FeedbackRecord::getLastName, FeedbackRecord::setLastName);
        binder.forField(email).bind(FeedbackRecord::getEmail, FeedbackRecord::setEmail);
        binder.forField(feedback).bind(FeedbackRecord::getFeedback, FeedbackRecord::setFeedback);

        binder.setReadOnly(true);

    }

}
