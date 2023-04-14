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

package org.komunumo.ui.view.admin.news;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.NewsRecord;
import org.komunumo.ui.component.CustomDateTimePicker;
import org.komunumo.ui.component.CustomLabel;
import org.komunumo.ui.component.EditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class NewsDialog extends EditDialog<NewsRecord> {

    public NewsDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<NewsRecord> binder) {
        final var title = new TextField("Title");
        final var subtitle = new TextField("Subtitle");
        final var teaser = new RichTextEditor();
        final var message = new RichTextEditor();
        final var showFrom = new CustomDateTimePicker("Show from");
        final var showTo = new CustomDateTimePicker("Show to");

        title.setRequiredIndicatorVisible(true);
        title.setValueChangeMode(EAGER);
        subtitle.setValueChangeMode(EAGER);

        formLayout.add(title, subtitle, new CustomLabel("Teaser"), teaser,
                new CustomLabel("Message"), message, showFrom, showTo);

        binder.forField(title)
                .withValidator(new StringLengthValidator(
                        "Please enter the title of the news (max. 255 chars)", 1, 255))
                .bind(NewsRecord::getTitle, NewsRecord::setTitle);

        binder.forField(subtitle)
                .withValidator(new StringLengthValidator(
                        "The subtitle is too long (max. 255 chars)", 0, 255))
                .bind(NewsRecord::getSubtitle, NewsRecord::setSubtitle);

        binder.forField(teaser.asHtml())
                .withValidator(new StringLengthValidator(
                        "The teaser is too long (max. 1'000 chars)", 0, 1_000))
                .bind(NewsRecord::getTeaser, NewsRecord::setTeaser);

        binder.forField(message.asHtml())
                .withValidator(new StringLengthValidator(
                        "The message is too long (max. 100'000 chars)", 0, 100_000))
                .bind(NewsRecord::getMessage, NewsRecord::setMessage);

        binder.forField(showFrom)
                .withValidator(value -> value == null || showTo.isEmpty() || value.isBefore(showTo.getValue()),
                        "The show from date must be before the show to date")
                .bind(NewsRecord::getShowFrom, NewsRecord::setShowFrom);

        binder.forField(showTo)
                .withValidator(value -> value == null || showFrom.isEmpty() || value.isAfter(showFrom.getValue()),
                        "The show to date must be after the show from date")
                .bind(NewsRecord::getShowTo, NewsRecord::setShowTo);
    }

}
