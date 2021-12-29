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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.ApplicationServiceInitListener;
import org.komunumo.Callback;
import org.komunumo.data.db.tables.records.RedirectRecord;
import org.komunumo.ui.component.EditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class RedirectDialog extends EditDialog<RedirectRecord> {

    private final ApplicationServiceInitListener applicationServiceInitListener;
    private Callback afterOpen;

    public RedirectDialog(@NotNull final String title,
                          @NotNull final ApplicationServiceInitListener applicationServiceInitListener) {
        super(title);
        this.applicationServiceInitListener = applicationServiceInitListener;
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout,
                           @NotNull final Binder<RedirectRecord> binder) {
        final var oldUrl = new TextField("Old URL");
        oldUrl.setRequiredIndicatorVisible(true);
        oldUrl.setValueChangeMode(EAGER);
        formLayout.add(oldUrl);

        final var newUrl = new TextField("New URL");
        newUrl.setRequiredIndicatorVisible(true);
        newUrl.setValueChangeMode(EAGER);
        formLayout.add(newUrl);

        binder.forField(oldUrl)
                .withValidator(new StringLengthValidator(
                        "Please enter the old URL (max. 255 chars)", 1, 255))
                .bind(RedirectRecord::getOldUrl, RedirectRecord::setOldUrl);

        binder.forField(newUrl)
                .withValidator(new StringLengthValidator(
                        "Please enter the new URL (max. 255 chars)", 1, 255))
                .bind(RedirectRecord::getNewUrl, RedirectRecord::setNewUrl);

        afterOpen = () -> oldUrl.setReadOnly(!oldUrl.getValue().isBlank());
    }

    @Override
    public void open(@NotNull final RedirectRecord redirectRecord, @Nullable final Callback afterSave) {
        super.open(redirectRecord,
                () -> {
                    if (afterOpen != null) {
                        afterOpen.execute();
                    }
                },
                () -> {
                    if (afterSave != null) {
                        applicationServiceInitListener.reloadRedirects();
                        afterSave.execute();
                    }
                }
        );
    }

}
