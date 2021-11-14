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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.ui.component.EditDialog;
import org.komunumo.ui.component.ImageUploadField;
import org.komunumo.util.GravatarUtil;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static org.komunumo.util.GravatarUtil.GRAVATAR_URL;

public class SpeakerDialog extends EditDialog<SpeakerRecord> {

    public SpeakerDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm(@NotNull final FormLayout formLayout, @NotNull final Binder<SpeakerRecord> binder) {
        final var firstName = new TextField("First name");
        final var lastName = new TextField("Last name");
        final var company = new TextField("Company");
        final var bio = new TextArea("Bio");
        final var photo = new ImageUploadField("Photo");
        final var email = new EmailField("Email");
        final var twitter = new TextField("Twitter");
        final var linkedIn = new TextField("LinkedIn");
        final var website = new TextField("Website");
        final var address = new TextField("Address");
        final var zipCode = new TextField("Zip code");
        final var city = new TextField("City");
        final var state = new TextField("State");
        final var country = new TextField("Country");

        firstName.setRequiredIndicatorVisible(true);
        firstName.setValueChangeMode(EAGER);
        lastName.setRequiredIndicatorVisible(true);
        lastName.setValueChangeMode(EAGER);
        photo.setMaxPreviewSize("150px", "150px");
        email.addValueChangeListener(changeEvent -> {
            final var newEmail = changeEvent.getValue();
            final var photoValue = photo.getValue();
            if (!newEmail.isBlank() && (photoValue.isBlank() || photoValue.startsWith(GRAVATAR_URL))) {
                photo.setValue(GravatarUtil.getGravatarAddress(newEmail, 150));
            } else if (newEmail.isBlank() && photoValue.startsWith(GRAVATAR_URL)) {
                photo.setValue("");
            }
        });

        formLayout.add(firstName, lastName, company, bio, photo, email, twitter,
                linkedIn, website, address, zipCode, city, state, country);

        binder.forField(firstName)
                .withValidator(new StringLengthValidator(
                        "Please enter the first name of the speaker (max. 255 chars)", 1, 255))
                .bind(SpeakerRecord::getFirstName, SpeakerRecord::setFirstName);

        binder.forField(lastName)
                .withValidator(new StringLengthValidator(
                        "Please enter the last name of the speaker (max. 255 chars)", 1, 255))
                .bind(SpeakerRecord::getLastName, SpeakerRecord::setLastName);

        binder.forField(company)
                .withValidator(new StringLengthValidator(
                        "The company name is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getCompany, SpeakerRecord::setCompany);

        binder.forField(bio)
                .withValidator(new StringLengthValidator(
                        "The bio is too long (max. 100'000 chars)", 0, 100_000))
                .bind(SpeakerRecord::getBio, SpeakerRecord::setBio);

        binder.forField(photo)
                .withValidator(value -> value.isEmpty() || value.startsWith("data:") || value.startsWith("https://"),
                        "The photo must be uploaded or the photo address must be secure (HTTPS)")
                .withValidator(new StringLengthValidator(
                        "The photo is too big (max. 250 KB)", 0, 250_000))
                .bind(SpeakerRecord::getPhoto, SpeakerRecord::setPhoto);

        binder.forField(email)
                .withValidator(new EmailValidator(
                        "Please enter a correct email address or leave this field empty", true))
                .withValidator(new StringLengthValidator(
                        "The email address is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getEmail, SpeakerRecord::setEmail);

        binder.forField(twitter)
                .withValidator(new StringLengthValidator(
                        "The twitter username is too long (max. 15 chars)", 0, 15))
                .bind(SpeakerRecord::getTwitter, SpeakerRecord::setTwitter);

        binder.forField(linkedIn)
                .withValidator(value -> value.isEmpty() || value.startsWith("https://"),
                        "The LinkedIn address must start with \"https://\"")
                .withValidator(new StringLengthValidator(
                        "The LinkedIn address is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getLinkedin, SpeakerRecord::setLinkedin);

        binder.forField(website)
                .withValidator(value -> value.isEmpty() || value.startsWith("https://"),
                        "The website address must start with \"https://\"")
                .withValidator(new StringLengthValidator(
                        "The website address is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getWebsite, SpeakerRecord::setWebsite);

        binder.forField(address)
                .withValidator(new StringLengthValidator(
                        "The address is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getAddress, SpeakerRecord::setAddress);

        binder.forField(zipCode)
                .withValidator(new StringLengthValidator(
                        "The zip code is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getZipCode, SpeakerRecord::setZipCode);

        binder.forField(city)
                .withValidator(new StringLengthValidator(
                        "The city is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getCity, SpeakerRecord::setCity);

        binder.forField(state)
                .withValidator(new StringLengthValidator(
                        "The state is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getState, SpeakerRecord::setState);

        binder.forField(country)
                .withValidator(new StringLengthValidator(
                        "The country is too long (max. 255 chars)", 0, 255))
                .bind(SpeakerRecord::getCountry, SpeakerRecord::setCountry);
    }
}
