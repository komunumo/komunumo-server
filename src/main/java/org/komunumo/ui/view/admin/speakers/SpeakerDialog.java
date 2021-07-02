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

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.ui.component.ImageUploadField;
import org.komunumo.ui.component.KomunumoEditDialog;
import org.komunumo.util.GravatarUtil;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static org.komunumo.util.GravatarUtil.GRAVATAR_URL;

public class SpeakerDialog extends KomunumoEditDialog<SpeakerRecord> {

    public SpeakerDialog(@NotNull final String title) {
        super(title);
    }

    @Override
    public void createForm() {
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
        firstName.focus();
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
                        "Please enter the first name of the speaker", 1, null))
                .bind(SpeakerRecord::getFirstName, SpeakerRecord::setFirstName);

        binder.forField(lastName)
                .withValidator(new StringLengthValidator(
                        "Please enter the last name of the speaker", 1, null))
                .bind(SpeakerRecord::getLastName, SpeakerRecord::setLastName);

        binder.forField(company)
                .bind(SpeakerRecord::getCompany, SpeakerRecord::setCompany);

        binder.forField(bio)
                .bind(SpeakerRecord::getBio, SpeakerRecord::setBio);

        binder.forField(photo)
                .withValidator(value -> value.isEmpty() || value.startsWith("data:") || value.startsWith("https://"),
                        "The photo must be uploaded or the photo address must be secure (HTTPS)")
                .bind(SpeakerRecord::getPhoto, SpeakerRecord::setPhoto);

        binder.forField(email)
                .withValidator(new EmailValidator(
                        "Please enter a correct email address or leave this field empty", true))
                .bind(SpeakerRecord::getEmail, SpeakerRecord::setEmail);

        binder.forField(twitter)
                .withValidator(value -> value.isEmpty() || value.startsWith("https://"),
                        "The twitter address must start with \"https://\"")
                .bind(SpeakerRecord::getTwitter, SpeakerRecord::setTwitter);

        binder.forField(linkedIn)
                .withValidator(value -> value.isEmpty() || value.startsWith("https://"),
                        "The LinkedIn address must start with \"https://\"")
                .bind(SpeakerRecord::getLinkedin, SpeakerRecord::setLinkedin);

        binder.forField(website)
                .withValidator(value -> value.isEmpty() || value.startsWith("https://"),
                        "The website address must start with \"https://\"")
                .bind(SpeakerRecord::getWebsite, SpeakerRecord::setWebsite);

        binder.forField(address)
                .bind(SpeakerRecord::getAddress, SpeakerRecord::setAddress);

        binder.forField(zipCode)
                .bind(SpeakerRecord::getZipCode, SpeakerRecord::setZipCode);

        binder.forField(city)
                .bind(SpeakerRecord::getCity, SpeakerRecord::setCity);

        binder.forField(state)
                .bind(SpeakerRecord::getState, SpeakerRecord::setState);

        binder.forField(country)
                .bind(SpeakerRecord::getCountry, SpeakerRecord::setCountry);
    }
}
