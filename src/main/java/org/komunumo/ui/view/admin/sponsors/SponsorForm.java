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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.ui.component.ImageUploadField;
import org.komunumo.ui.component.KomunumoDatePicker;

import java.util.List;

import static com.vaadin.flow.component.Key.ENTER;
import static com.vaadin.flow.component.Key.ESCAPE;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class SponsorForm extends FormLayout {

    private final TextField name = new TextField("Name");
    private final TextField website = new TextField("Website");
    private final ComboBox<SponsorLevel> level = new ComboBox<>("Level");
    private final ImageUploadField logo = new ImageUploadField("Logo");
    private final KomunumoDatePicker validFrom = new KomunumoDatePicker("Valid from");
    private final KomunumoDatePicker validTo = new KomunumoDatePicker("Valid to");

    private final Button save = new Button("Save");
    private final Button cancel = new Button("Cancel");

    private final Binder<SponsorRecord> binder = new Binder<>(SponsorRecord.class);

    public SponsorForm(@NotNull final List<SponsorLevel> levels) {
        addClassName("sponsor-form");
        configureFields(levels);
        configureBinder();
        add(name, website, level, logo, validFrom, validTo, createButtonsLayout());
    }

    private void configureFields(@NotNull List<SponsorLevel> levels) {
        name.setRequiredIndicatorVisible(true);
        name.setValueChangeMode(EAGER);
        name.focus();
        level.setItems(levels);
        level.setItemLabelGenerator(level -> WordUtils.capitalizeFully(level.toString(), '_'));
    }

    private void configureBinder() {
        binder.forField(name)
                .withValidator(new StringLengthValidator(
                        "Please enter the name of the sponsor", 1, null))
                .bind(SponsorRecord::getName, SponsorRecord::setName);

        binder.forField(website)
                .withValidator(website -> website.isEmpty() || website.startsWith("https://"),
                        "The website address must start with \"https://\"")
                .bind(SponsorRecord::getWebsite, SponsorRecord::setWebsite);

        binder.forField(logo)
                .withValidator(logo -> logo.isEmpty() || logo.startsWith("data:") || logo.startsWith("https://"),
                        "The logo must be uploaded or the logo address must be secure (HTTPS)")
                .bind(SponsorRecord::getLogo, SponsorRecord::setLogo);

        binder.forField(validFrom)
                .withValidator(validFrom -> validFrom == null || validTo.isEmpty() || validFrom.isBefore(validTo.getValue()),
                        "The valid from date must be before the valid to date")
                .bind(SponsorRecord::getValidFrom, SponsorRecord::setValidFrom);

        binder.forField(validTo)
                .withValidator(validTo -> validTo == null || validFrom.isEmpty() || validTo.isAfter(validFrom.getValue()),
                        "The valid to date must be after the valid from date")
                .bind(SponsorRecord::getValidFrom, SponsorRecord::setValidFrom);

        binder.bindInstanceFields(this);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(LUMO_PRIMARY);
        cancel.addThemeVariants(LUMO_TERTIARY);

        save.addClickShortcut(ENTER);
        cancel.addClickShortcut(ESCAPE);

        save.addClickListener(click -> validateAndSave());
        cancel.addClickListener(click -> fireEvent(new CancelEvent(this)));

        binder.addStatusChangeListener(event -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, cancel);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    public void setSponsor(@NotNull final SponsorRecord sponsor) {
        binder.setBean(sponsor);
    }

    public static abstract class SponsorFormEvent extends ComponentEvent<SponsorForm> {
        private final SponsorRecord sponsor;

        protected SponsorFormEvent(@NotNull final SponsorForm source, @Nullable final SponsorRecord sponsor) {
            super(source, false);
            this.sponsor = sponsor;
        }

        public SponsorRecord getSponsor() {
            return sponsor;
        }
    }

    public static class SaveEvent extends SponsorFormEvent {
        protected SaveEvent(@NotNull final SponsorForm source, @Nullable final SponsorRecord sponsor) {
            super(source, sponsor);
        }
    }

    public static class CancelEvent extends SponsorFormEvent {
        protected CancelEvent(@NotNull final SponsorForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(@NotNull final Class<T> eventType,
                                                                  @NotNull final ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
