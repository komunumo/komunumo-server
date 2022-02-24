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
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.Callback;
import org.komunumo.data.db.tables.records.LocationColorRecord;
import org.komunumo.ui.component.EditDialog;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class LocationColorDialog extends EditDialog<LocationColorRecord> {

    private Callback afterOpen;

    public LocationColorDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<LocationColorRecord> binder) {
        final var location = new TextField("Location");
        location.setRequiredIndicatorVisible(true);
        location.setValueChangeMode(EAGER);
        formLayout.add(location);

        final var color = new TextField("Color");
        color.setRequiredIndicatorVisible(true);
        color.setValueChangeMode(EAGER);
        formLayout.add(color);

        binder.forField(location)
                .withValidator(new StringLengthValidator(
                        "Please enter the location (max. 255 chars)", 1, 255))
                .bind(LocationColorRecord::getLocation, LocationColorRecord::setLocation);

        binder.forField(color)
                .withValidator(new RegexpValidator(
                        "Please enter the color as a HEX value in format '#rrggbb'",
                        "^#[a-fA-F0-9]{6}$", true))
                .bind(LocationColorRecord::getColor, LocationColorRecord::setColor);

        afterOpen = () -> location.setReadOnly(!location.getValue().isBlank());
    }

    @Override
    public void open(@NotNull final LocationColorRecord locationColorRecord, @Nullable final Callback afterSave) {
        super.open(locationColorRecord,
                () -> {
                    if (afterOpen != null) {
                        afterOpen.execute();
                    }
                },
                () -> {
                    if (afterSave != null) {
                        afterSave.execute();
                    }
                }
        );
    }

}
