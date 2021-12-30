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

package org.komunumo.ui.view.admin.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.Callback;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.RegistrationMemberEntity;
import org.komunumo.data.service.RegistrationService;
import org.komunumo.ui.component.EnhancedDialog;

public class AddRegistrationDialog extends EnhancedDialog {

    private final Focusable<?> focusField;

    public AddRegistrationDialog(@NotNull final RegistrationService registrationService,
                                 @NotNull final Event event,
                                 @Nullable final Callback afterSaveCallback) {
        super("Add registration for \"%s\"".formatted(event.getTitle()));

        // Content
        final var memberSelect = new ComboBox<RegistrationMemberEntity>("Select attendee to register");
        memberSelect.setItems(registrationService.getUnregisteredMembers(event.getId()));
        memberSelect.setItemLabelGenerator(registrationMemberEntity ->
                "%s <%s>".formatted(registrationMemberEntity.fullName(), registrationMemberEntity.email()));
        focusField = memberSelect;
        addToContent(memberSelect);

        // Footer
        final var saveButton = new Button("Save");
        saveButton.setDisableOnClick(true);
        saveButton.setEnabled(false);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(buttonClickEvent -> {
            final var registrationMemberEntity = memberSelect.getValue();
            if (registrationMemberEntity != null) {
                registrationService.toMember(registrationMemberEntity).ifPresent(member -> {
                    registrationService.registerForEvent(event, member, "Admin");
                    if (afterSaveCallback != null) {
                        afterSaveCallback.execute();
                    }
                    close();
                });
            }
        });
        memberSelect.addValueChangeListener(valueChangeEvent -> saveButton.setEnabled(valueChangeEvent.getValue() != null));

        final var cancelButton = new Button("Cancel");
        cancelButton.addClickListener(click -> close());

        addToFooter(saveButton, cancelButton);
    }

    @Override
    public void open() {
        super.open();
        focusField.focus();
    }
}
