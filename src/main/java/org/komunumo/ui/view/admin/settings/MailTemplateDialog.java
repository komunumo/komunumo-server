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

package org.komunumo.ui.view.admin.settings;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.Callback;
import org.komunumo.data.db.tables.records.MailTemplateRecord;
import org.komunumo.data.entity.MailTemplateId;
import org.komunumo.ui.component.CustomLabel;
import org.komunumo.ui.component.EditDialog;

import java.util.List;
import java.util.Objects;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class MailTemplateDialog extends EditDialog<MailTemplateRecord> {

    private final List<MailTemplateId> mailTemplateIds;
    private Callback afterOpen;

    public MailTemplateDialog(@NotNull final String title,
                              @NotNull final List<MailTemplateId> mailTemplateIds) {
        super(title);
        this.mailTemplateIds = mailTemplateIds;
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout,
                           @NotNull final Binder<MailTemplateRecord> binder) {
        final var id = new Select<>(mailTemplateIds.stream().map(MailTemplateId::name).toArray(String[]::new));
        id.setRequiredIndicatorVisible(true);
        id.setLabel("ID");
        formLayout.add(id);

        final var subject = new TextField("Subject");
        subject.setRequiredIndicatorVisible(true);
        subject.setValueChangeMode(EAGER);
        formLayout.add(subject);

        final var contentText = new TextArea();
        contentText.setRequiredIndicatorVisible(true);
        formLayout.add(new CustomLabel("Content as plain text"), contentText);

        final var contentHTML = new RichTextEditor();
        contentHTML.setRequiredIndicatorVisible(true);
        formLayout.add(new CustomLabel("Content as formatted HTML"), contentHTML);

        binder.forField(id)
                .withValidator(Objects::nonNull, "Please select the ID")
                .bind(MailTemplateRecord::getId, MailTemplateRecord::setId);

        binder.forField(subject)
                .withValidator(new StringLengthValidator(
                        "Please enter the subject (max. 255 chars)", 1, 255))
                .bind(MailTemplateRecord::getSubject, MailTemplateRecord::setSubject);

        binder.forField(contentText)
                .withValidator(new StringLengthValidator(
                        "Please enter the content as plain text (max. 8'000 chars)", 1, 8_000))
                .bind(MailTemplateRecord::getContentText, MailTemplateRecord::setContentText);

        binder.forField(contentHTML.asHtml())
                .withValidator(new StringLengthValidator(
                        "Please enter the content as formattet HTML (max. 8'000 chars)", 1, 8_000))
                .bind(MailTemplateRecord::getContentHtml, MailTemplateRecord::setContentHtml);

        afterOpen = () -> id.setReadOnly(id.getValue() != null);
    }

    @Override
    public void open(@NotNull final MailTemplateRecord mailTemplateRecord, @Nullable final Callback afterSave) {
        super.open(mailTemplateRecord,
                () -> {
                    if (afterOpen != null) {
                        afterOpen.execute();
                    }
                },
                afterSave
        );
    }

}
