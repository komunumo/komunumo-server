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
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.StatisticService;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

import static org.komunumo.util.FormatterUtil.formatNumber;

public class WebsiteStats extends Div {

    private final StatisticService statisticService;

    public WebsiteStats(@NotNull final StatisticService statisticService) {
        this.statisticService = statisticService;

        final var stats = getRandomStats();
        final var number = new Span(new Text(formatNumber(stats.getNumber())));
        number.addClassName("number");
        final var text = new Span(new Text(stats.getText()));
        text.addClassName("text");

        add(number, text);
        addClassName("website-stats");
    }

    private Stats getRandomStats() {
//        final var randomNumber = new Random().nextInt(7);
        final var randomNumber = ThreadLocalRandom.current().nextInt(0, 7);
        switch (randomNumber) {
            case 0: return getAttendeesActualYear();
            case 1: return getUniqueAttendeesActualYear();
            case 2: return getAttendeesLastYear();
            case 3: return getUniqueAttendeesLastYear();
            case 4: return getMemberCountLastYear();
            case 5: return getMembersJoinedLastYear();
            case 6: return getEventCountLastYear();
        }
        throw new RuntimeException("random website stats out of bounds");
    }

    private Stats getAttendeesActualYear() {
        final var year = Year.now();
        final var number = statisticService.countAttendeesByYear(year, StatisticService.NoShows.INCLUDE);
        final var text = String.format("attendees have registered for our events so far in %s.", year);
        return new Stats(number, text);
    }

    private Stats getUniqueAttendeesActualYear() {
        final var year = Year.now();
        final var number = statisticService.countUniqueAttendeesByYear(year, StatisticService.NoShows.INCLUDE);
        final var text = String.format("unique attendees have registered for our events so far in %s.", year);
        return new Stats(number, text);
    }

    private Stats getAttendeesLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = statisticService.countAttendeesByYear(year, StatisticService.NoShows.INCLUDE);
        final var text = String.format("attendees registered for our events during %s.", year);
        return new Stats(number, text);
    }

    private Stats getUniqueAttendeesLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = statisticService.countUniqueAttendeesByYear(year, StatisticService.NoShows.INCLUDE);
        final var text = String.format("unique attendees have registered for our events in %s.", year);
        return new Stats(number, text);
    }

    private Stats getMemberCountLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = statisticService.countMembersByYear(year);
        final var text = String.format("members had joined JUG Switzerland at the end of %s.", year);
        return new Stats(number, text);
    }

    private Stats getMembersJoinedLastYear() {
        final var year = Year.now().minusYears(1);
        final var membersNow = statisticService.countMembersByYear(year);
        final var membersBefore = statisticService.countMembersByYear(year.minusYears(1));
        final var number = membersNow - membersBefore;
        final var text = String.format("new members joined JUG Switzerland in %s.", year);
        return new Stats(number, text);
    }

    private Stats getEventCountLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = statisticService.countEventsByYear(year);
        final var text = String.format("events were organized by JUG Switzerland during %s.", year);
        return new Stats(number, text);
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
