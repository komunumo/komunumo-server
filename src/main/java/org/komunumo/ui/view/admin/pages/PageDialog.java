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

package org.komunumo.ui.view.admin.pages;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.entity.Page;
import org.komunumo.ui.component.CustomLabel;
import org.komunumo.ui.component.EditDialog;

import java.util.Objects;

import org.komunumo.util.URLUtil;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class PageDialog extends EditDialog<Page> {

    public PageDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<Page> binder) {
        final var parent = new Select<>(PageParent.values());
        final var pageUrl = new TextField("URL");
        final var title = new TextField("Title");
        final var content = new RichTextEditor();

        parent.setLabel("Parent");
        parent.setRequiredIndicatorVisible(true);
        parent.addValueChangeListener(changeEvent -> pageUrl.setPrefixComponent(
                new Span("%s/".formatted(URLUtil.createReadableUrl(changeEvent.getValue().getLiteral())))));
        pageUrl.setRequiredIndicatorVisible(true);
        pageUrl.setValueChangeMode(EAGER);
        title.setRequiredIndicatorVisible(true);
        title.setValueChangeMode(EAGER);

        formLayout.add(parent, pageUrl, title, new CustomLabel("Content"), content);

        binder.forField(parent)
                .withValidator(Objects::nonNull, "Please select the parent navigation")
                .bind(Page::getParent, Page::setParent);

        binder.forField(pageUrl)
                .withValidator(new StringLengthValidator(
                        "Please enter the URL of the page (max. 255 chars)", 1, 255))
                .bind(Page::getPageUrl, Page::setPageUrl);

        binder.forField(title)
                .withValidator(new StringLengthValidator(
                        "Please enter the title of the page (max. 255 chars)", 1, 255))
                .bind(Page::getTitle, Page::setTitle);

        binder.forField(content.asHtml())
                .withValidator(new StringLengthValidator(
                        "The content is too long (max. 8'000 chars)", 0, 8_000))
                .bind(Page::getContent, Page::setContent);
    }

}
