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

package org.komunumo.ui.view.website;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.entity.SponsorEntity;
import org.komunumo.data.service.DatabaseService;

import java.util.Locale;

@CssImport("./themes/komunumo/views/website/sponsor-block.css")
public class SponsorBlock extends ContentBlock {

    public SponsorBlock(@NotNull DatabaseService databaseService) {
        super("Sponsors");
        addClassName("sponsor-block");
        setContent(createSponsorComponent(databaseService));
    }

    private Component createSponsorComponent(@NotNull final DatabaseService databaseService) {
        return new Div(createSponsorComponent(databaseService, SponsorLevel.Platinum),
                createSponsorComponent(databaseService, SponsorLevel.Gold),
                createSponsorComponent(databaseService, SponsorLevel.Silver));
    }

    private Component createSponsorComponent(@NotNull final DatabaseService databaseService, @NotNull final SponsorLevel sponsorLevel) {
        final var levelTitle = new H3("%s sponsor".formatted(sponsorLevel.getLiteral()));
        levelTitle.addClassName("sponsor-level");

        final var sponsorLogos = new Div();
        sponsorLogos.addClassName("sponsor-logos");
        databaseService.getActiveSponsors(sponsorLevel).map(SponsorBlock::toLogo).forEach(sponsorLogos::add);

        final var sponsorComponent = new Div(levelTitle, sponsorLogos);
        sponsorComponent.addClassName("sponsor-level-%s".formatted(sponsorLevel.getLiteral().toLowerCase(Locale.getDefault())));
        return sponsorComponent;
    }

    private static Component toLogo(@NotNull final SponsorEntity sponsor) {
        final var logo = new Div(new Image(sponsor.logo(), sponsor.name()));
        logo.addClassName("sponsor-logo");
        logo.addClickListener(clickEvent -> UI.getCurrent().getPage().setLocation(sponsor.website()));

        final var logoContainer = new Div(logo);
        logoContainer.addClassName("sponsor-logo-container");
        return logoContainer;
    }

}
