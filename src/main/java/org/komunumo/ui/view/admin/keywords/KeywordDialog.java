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

package org.komunumo.ui.view.admin.keywords;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.KeywordRecord;
import org.komunumo.ui.component.EditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class KeywordDialog extends EditDialog<KeywordRecord> {

    public KeywordDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<KeywordRecord> binder) {
        final var keyword = new TextField("Keyword");
        keyword.setRequiredIndicatorVisible(true);
        keyword.setValueChangeMode(EAGER);
        formLayout.add(keyword);

        binder.forField(keyword)
                .withValidator(new StringLengthValidator(
                        "Please enter the keyword (max. 255 chars)", 1, 255))
                .bind(KeywordRecord::getKeyword, KeywordRecord::setKeyword);
    }
}
