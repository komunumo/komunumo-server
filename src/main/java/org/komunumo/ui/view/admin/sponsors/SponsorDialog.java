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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.service.SponsorService;
import org.komunumo.ui.component.ImageUploadField;

import static org.komunumo.util.DatePickerUtil.createDatePicker;

public class SponsorDialog extends Dialog {

    private final Focusable<? extends Component> focusField;

    public SponsorDialog(@NotNull final SponsorRecord sponsor,
                         final @NotNull SponsorService sponsorService) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        final var title = new H2(sponsor.getId() == null ? "New sponsor" : "Edit sponsor");
        title.getStyle().set("margin-top", "0");

        final var nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setValue(sponsor.getName());

        final var websiteField = new TextField("Website");
        websiteField.setValue(sponsor.getWebsite());

        final var levelField = new Select<>(SponsorLevel.values());
        levelField.setLabel("Level");
        levelField.setValue(sponsor.getLevel());

        final var logoField = new ImageUploadField("Logo");
        logoField.setValue(sponsor.getLogo());

        final var validFromField = createDatePicker("Valid from", sponsor.getValidFrom());
        final var validToField = createDatePicker("Valid to", sponsor.getValidTo());

        final var form = new FormLayout();
        form.add(nameField, websiteField, levelField, logoField, validFromField, validToField);

        final var saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(clickEvent -> {
            if (nameField.getValue().isBlank()) {
                Notification.show("Please enter at least the name of the sponsor!");
            } else if (validFromField.getValue() != null && validToField.getValue() != null
                    && validFromField.getValue().isAfter(validToField.getValue())) {
                Notification.show("The valid from date can't be after the valid to date!");
            } else {
                saveButton.setEnabled(false);
                sponsor.setName(nameField.getValue());
                sponsor.setWebsite(websiteField.getValue());
                sponsor.setLogo(logoField.getValue());
                sponsor.setLevel(levelField.getValue());
                sponsor.setValidFrom(validFromField.getValue());
                sponsor.setValidTo(validToField.getValue());
                sponsorService.store(sponsor);

                Notification.show("Sponsor saved.");
                close();
            }
        });
        saveButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        final var cancelButton = new Button("Cancel", clickEvent -> close());
        final var buttonBar = new HorizontalLayout(saveButton, cancelButton);

        add(title, form, buttonBar);

        focusField = nameField;
    }

    @Override
    public void open() {
        super.open();
        focusField.focus();
    }
}
