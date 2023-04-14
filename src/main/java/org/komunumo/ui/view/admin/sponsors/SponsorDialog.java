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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.Callback;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.CustomDatePicker;
import org.komunumo.ui.component.CustomLabel;
import org.komunumo.ui.component.EditDialog;
import org.komunumo.ui.component.ImageUploadField;
import org.komunumo.ui.component.TagField;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class SponsorDialog extends EditDialog<SponsorRecord> {

    private final DatabaseService databaseService;

    private String domainCSV;

    public SponsorDialog(@NotNull final String title, @NotNull final DatabaseService databaseService) {
        super(title);
        this.databaseService = databaseService;
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<SponsorRecord> binder) {
        final var name = new TextField("Name");
        final var website = new TextField("Website");
        final var level = new ComboBox<SponsorLevel>("Level");
        final var logo = new ImageUploadField("Logo");
        final var description = new RichTextEditor();
        final var validFrom = new CustomDatePicker("Valid from");
        final var validTo = new CustomDatePicker("Valid to");
        final var domains = new TagField("Domains");

        name.setRequiredIndicatorVisible(true);
        name.setValueChangeMode(EAGER);
        website.setValueChangeMode(EAGER);
        level.setItems(SponsorLevel.values());

        formLayout.add(name, website, level, logo, new CustomLabel("Description"), description, validFrom, validTo, domains);

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

        binder.forField(description.asHtml())
                .withValidator(new StringLengthValidator(
                        "The description is too long (max. 100'000 chars)", 0, 100_000))
                .bind(SponsorRecord::getDescription, SponsorRecord::setDescription);

        binder.forField(validFrom)
                .withValidator(value -> value == null || validTo.isEmpty() || value.isBefore(validTo.getValue()),
                        "The valid from date must be before the valid to date")
                .bind(SponsorRecord::getValidFrom, SponsorRecord::setValidFrom);

        binder.forField(validTo)
                .withValidator(value -> value == null || validFrom.isEmpty() || value.isAfter(validFrom.getValue()),
                        "The valid to date must be after the valid from date")
                .bind(SponsorRecord::getValidTo, SponsorRecord::setValidTo);

        binder.forField(domains)
                .bind(this::getDomains, this::setDomains);
    }

    @Override
    public void open(@NotNull final SponsorRecord sponsorRecord, @Nullable final Callback afterSave) {
        final var domainList = databaseService.getSponsorDomains(sponsorRecord);
        domainCSV = domainList.isEmpty() ? "" : domainList.stream()
                .sorted().collect(Collectors.joining(","));
        super.open(sponsorRecord,
                () -> {
                    databaseService.setSponsorDomains(sponsorRecord,
                            domainCSV.isBlank() ? Set.of() : Arrays.stream(domainCSV.split(","))
                                    .collect(Collectors.toUnmodifiableSet()));
                    if (afterSave != null) {
                        afterSave.execute();
                    }
                }
        );
    }

    private String getDomains(@NotNull final SponsorRecord sponsorRecord) {
        return domainCSV;
    }

    @SuppressWarnings("checkstyle:HiddenField") // setter
    private void setDomains(@NotNull final SponsorRecord sponsorRecord, @Nullable final String domainCSV) {
        this.domainCSV = domainCSV != null ? domainCSV : "";
    }

}
