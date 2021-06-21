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

package org.komunumo.ui.view.admin.speakers;

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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.ui.component.ImageUploadField;
import org.komunumo.util.GravatarUtil;

import static org.komunumo.util.GravatarUtil.GRAVATAR_URL;

// TODO warum wurde kein Binder verwendet?
public class SpeakerDialog extends Dialog {

    private final Focusable<? extends Component> focusField;

    public SpeakerDialog(@NotNull final SpeakerRecord speaker,
                         @NotNull final SpeakerService speakerService) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        final var title = new H2(speaker.getId() == null ? "New speaker" : "Edit speaker");
        title.getStyle().set("margin-top", "0");

        final var firstNameField = new TextField("First name");
        firstNameField.setRequiredIndicatorVisible(true);
        firstNameField.setValue(speaker.getFirstName());

        final var lastNameField = new TextField("Last name");
        lastNameField.setRequiredIndicatorVisible(true);
        lastNameField.setValue(speaker.getLastName());

        final var companyField = new TextField("Company");
        companyField.setValue(speaker.getCompany());

        final var bioField = new TextArea("Bio");
        bioField.setValue(speaker.getBio());

        final var photoField = new ImageUploadField("Photo");
        photoField.setPreviewWidth("150px");
        photoField.setValue(speaker.getPhoto());

        final var emailField = new EmailField("Email");
        emailField.setValue(speaker.getEmail());
        emailField.addValueChangeListener(changeEvent -> {
            final var email = changeEvent.getValue();
            final var photo = photoField.getValue();
            if (!email.isBlank() && (photo.isBlank() || photo.startsWith(GRAVATAR_URL))) {
                photoField.setValue(GravatarUtil.getGravatarAddress(email, 150));
            } else if (email.isBlank() && photo.startsWith(GRAVATAR_URL)) {
                photoField.setValue("");
            }
        });

        final var twitterField = new TextField("Twitter");
        twitterField.setValue(speaker.getTwitter());

        final var linkedinField = new TextField("LinkedIn");
        linkedinField.setValue(speaker.getLinkedin());

        final var websiteField = new TextField("Website");
        websiteField.setValue(speaker.getWebsite());

        final var addressField = new TextField("Address");
        addressField.setValue(speaker.getAddress());

        final var zipCodeField = new TextField("Zip code");
        zipCodeField.setValue(speaker.getZipCode());

        final var cityField = new TextField("City");
        cityField.setValue(speaker.getCity());

        final var stateField = new TextField("State");
        stateField.setValue(speaker.getState());

        final var countryField = new TextField("Country");
        countryField.setValue(speaker.getCountry());

        final var form = new FormLayout();
        form.add(firstNameField, lastNameField, companyField, bioField,
                emailField, twitterField, linkedinField, websiteField,
                addressField, zipCodeField, cityField, stateField,
                countryField, photoField);

        final var saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(clickEvent -> {
            if (firstNameField.getValue().isBlank()) {
                Notification.show("Please enter the first name of the speaker!");
            } else if (lastNameField.getValue().isBlank()) {
                Notification.show("Please enter the last name of the speaker!");
            } else {
                saveButton.setEnabled(false);
                speaker.setFirstName(firstNameField.getValue());
                speaker.setLastName(lastNameField.getValue());
                speaker.setCompany(companyField.getValue());
                speaker.setBio(bioField.getValue());
                speaker.setPhoto(photoField.getValue());
                speaker.setEmail(emailField.getValue());
                speaker.setTwitter(twitterField.getValue());
                speaker.setLinkedin(linkedinField.getValue());
                speaker.setWebsite(websiteField.getValue());
                speaker.setAddress(addressField.getValue());
                speaker.setZipCode(zipCodeField.getValue());
                speaker.setCity(cityField.getValue());
                speaker.setState(stateField.getValue());
                speaker.setCountry(countryField.getValue());
                speakerService.store(speaker);

                Notification.show("Speaker saved.");
                close();
            }
        });
        saveButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        final var cancelButton = new Button("Cancel", clickEvent -> close());
        final var buttonBar = new HorizontalLayout(saveButton, cancelButton);

        add(title, form, buttonBar);

        focusField = firstNameField;
    }

    @Override
    public void open() {
        super.open();
        focusField.focus();
    }
}
