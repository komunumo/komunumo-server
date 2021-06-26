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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.service.SponsorService;

import java.util.List;

import static org.komunumo.data.db.tables.Sponsor.SPONSOR;

public class SponsorDialog extends Dialog {

    private final SponsorService sponsorService;

    public SponsorDialog(@NotNull final SponsorRecord sponsor,
                         @NotNull final SponsorService sponsorService) {
        this.sponsorService = sponsorService;

        setCloseOnOutsideClick(false);

        final var form = new SponsorForm(List.of(SponsorLevel.values()));
        form.addListener(SponsorForm.SaveEvent.class, event -> saveSponsor(event.getSponsor()));
        form.addListener(SponsorForm.CancelEvent.class, event -> close());
        form.setSponsor(sponsor);

        add(form);
    }

    private void saveSponsor(@NotNull final SponsorRecord sponsor) {
        sponsorService.store(sponsor);
        Notification.show(String.format("Sponsor \"%s\" saved.", sponsor.get(SPONSOR.NAME)));
        close();
    }
}
