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

package org.komunumo.ui.view.admin.sponsors;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.ui.component.ImageUploadField;
import org.komunumo.ui.component.KomunumoDatePicker;
import org.komunumo.ui.component.KomunumoEditDialog;
import org.komunumo.util.FormatterUtil;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class SponsorDialog extends KomunumoEditDialog<SponsorRecord> {

    public SponsorDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm() {
        final var name = new TextField("Name");
        final var website = new TextField("Website");
        final var level = new ComboBox<SponsorLevel>("Level");
        final var logo = new ImageUploadField("Logo");
        final var validFrom = new KomunumoDatePicker("Valid from");
        final var validTo = new KomunumoDatePicker("Valid to");

        name.setRequiredIndicatorVisible(true);
        name.setValueChangeMode(EAGER);
        level.setItems(SponsorLevel.values());
        level.setItemLabelGenerator(FormatterUtil::formatCamelCase);

        formLayout.add(name, website, level, logo, validFrom, validTo);

        binder.forField(name)
                .withValidator(new StringLengthValidator(
                        "Please enter the name of the sponsor (max. 255 chars)", 1, 255))
                .bind(SponsorRecord::getName, SponsorRecord::setName);

        binder.forField(website)
                .withValidator(value -> value.isEmpty() || value.startsWith("https://"),
                        "The website address must start with \"https://\"")
                .withValidator(new StringLengthValidator(
                        "The website address is too long (max. 255 chars)", 0, 255))
                .bind(SponsorRecord::getWebsite, SponsorRecord::setWebsite);

        binder.forField(level)
                .bind(SponsorRecord::getLevel, SponsorRecord::setLevel);

        binder.forField(logo)
                .withValidator(value -> value.isEmpty() || value.startsWith("data:") || value.startsWith("https://"),
                        "The logo must be uploaded or the logo address must be secure (HTTPS)")
                .withValidator(new StringLengthValidator(
                        "The logo is too big (max. 100 KB)", 0, 100_000))
                .bind(SponsorRecord::getLogo, SponsorRecord::setLogo);

        binder.forField(validFrom)
                .withValidator(value -> value == null || validTo.isEmpty() || value.isBefore(validTo.getValue()),
                        "The valid from date must be before the valid to date")
                .bind(SponsorRecord::getValidFrom, SponsorRecord::setValidFrom);

        binder.forField(validTo)
                .withValidator(value -> value == null || validFrom.isEmpty() || value.isAfter(validFrom.getValue()),
                        "The valid to date must be after the valid from date")
                .bind(SponsorRecord::getValidTo, SponsorRecord::setValidTo);
    }
}
