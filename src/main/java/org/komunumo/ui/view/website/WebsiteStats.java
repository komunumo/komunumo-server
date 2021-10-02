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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.Random;

import org.jetbrains.annotations.NotNull;

import static org.komunumo.util.FormatterUtil.formatNumber;

public class WebsiteStats extends Div {

    public WebsiteStats() {
        final var stats = getRandomStats();

        final var number = new Span(new Text(formatNumber(stats.getNumber())));
        number.addClassName("number");

        final var text = new Span(new Text(stats.getText()));
        text.addClassName("text");

        add(number, text);
        addClassName("website-stats");
    }

    private Stats getRandomStats() {
        final var randomNumber = new Random().nextInt(7);
        switch (randomNumber) {
            case 0: return new Stats(3488, "participiants have registered for our events so far in 2021.");
            case 1: return new Stats(1362, "unique visitors have registered for our events so far in 2021.");
            case 2: return new Stats(4486, "participiants registered for our events during 2020.");
            case 3: return new Stats(1408, "unique visitors have registered for our events in 2020.");
            case 4: return new Stats(1029, "members had joined JUG Switzerland at the end of 2020.");
            case 5: return new Stats(54, "new members joined JUG Switzerland in 2020.");
            case 6: return new Stats(41, "events were organized by JUG Switzerland during 2020.");
        }
        throw new RuntimeException("random website stats out of bounds");
    }

    private static class Stats {

        private final int number;
        private final String text;

        public Stats(final int number, @NotNull final String text) {
            this.number = number;
            this.text = text;
        }

        public int getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }
    }

}
