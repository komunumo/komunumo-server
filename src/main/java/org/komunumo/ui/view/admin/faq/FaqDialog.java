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

package org.komunumo.ui.view.admin.faq;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.FaqRecord;
import org.komunumo.ui.component.CustomLabel;
import org.komunumo.ui.component.EditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class FaqDialog extends EditDialog<FaqRecord> {

    public FaqDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<FaqRecord> binder) {
        final var question = new TextField("Question");
        question.setRequiredIndicatorVisible(true);
        question.setValueChangeMode(EAGER);

        final var answer = new RichTextEditor();
        answer.setRequiredIndicatorVisible(true);

        formLayout.add(question, new CustomLabel("Answer"), answer);

        binder.forField(question)
                .withValidator(new StringLengthValidator(
                        "Please enter the question (max. 255 chars)", 1, 255))
                .bind(FaqRecord::getQuestion, FaqRecord::setQuestion);

        binder.forField(answer.asHtml())
                .withValidator(new StringLengthValidator(
                        "Please enter the question (max. 255 chars)", 1, 10_000))
                .bind(FaqRecord::getAnswer, FaqRecord::setAnswer);
    }

}
