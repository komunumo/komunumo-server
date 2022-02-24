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
import org.komunumo.data.entity.NoShows;
import org.komunumo.data.service.DatabaseService;

import java.time.Year;
import java.util.Random;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.komunumo.util.FormatterUtil.formatNumber;

public class WebsiteStats extends Div {

    private final Random random;
    private final DatabaseService databaseService;

    public WebsiteStats(@NotNull final DatabaseService databaseService) {
        this.random = new Random();
        this.databaseService = databaseService;

        final var stats = getRandomStats();
        final var number = new Span(new Text(formatNumber(stats.number())));
        number.addClassName("number");
        final var text = new Span(new Text(stats.text()));
        text.addClassName("text");

        add(number, text);
        addClassName("website-stats");
    }

    private Stats getRandomStats() {
        return switch (random.nextInt(9)) {
            case 0 -> getAttendeesActualYear();
            case 1 -> getUniqueAttendeesActualYear();
            case 2 -> getAttendeesLastYear();
            case 3 -> getUniqueAttendeesLastYear();
            case 4 -> getMemberCountLastYear();
            case 5 -> getMembersJoinedLastYear();
            case 6 -> getMembersJoinedThisYear();
            case 7 -> getEventCountLastYear();
            case 8 -> getEventCountThisYear();
            default -> throw new RuntimeException("random website stats out of bounds");
        };
    }

    private Stats getAttendeesActualYear() {
        final var year = Year.now();
        final var number = databaseService.countAttendeesByYear(year, NoShows.INCLUDE);
        final var text = String.format("attendees have registered for our events so far in %s.", year);
        return new Stats(number, text);
    }

    private Stats getUniqueAttendeesActualYear() {
        final var year = Year.now();
        final var number = databaseService.countUniqueAttendeesByYear(year, NoShows.INCLUDE);
        final var text = String.format("unique attendees have registered for our events so far in %s.", year);
        return new Stats(number, text);
    }

    private Stats getAttendeesLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = databaseService.countAttendeesByYear(year, NoShows.INCLUDE);
        final var text = String.format("attendees registered for our events during %s.", year);
        return new Stats(number, text);
    }

    private Stats getUniqueAttendeesLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = databaseService.countUniqueAttendeesByYear(year, NoShows.INCLUDE);
        final var text = String.format("unique attendees have registered for our events in %s.", year);
        return new Stats(number, text);
    }

    private Stats getMemberCountLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = databaseService.countMembersByYear(year);
        final var text = String.format("members had joined JUG Switzerland at the end of %s.", year);
        return new Stats(number, text);
    }

    private Stats getMembersJoinedLastYear() {
        final var year = Year.now().minusYears(1);
        final var membersNow = databaseService.countMembersByYear(year);
        final var membersBefore = databaseService.countMembersByYear(year.minusYears(1));
        final var number = membersNow - membersBefore;
        final var text = String.format("new members joined JUG Switzerland during %s.", year);
        return new Stats(number, text);
    }

    private Stats getMembersJoinedThisYear() {
        final var year = Year.now();
        final var firstDay = year.atMonth(JANUARY).atDay(1);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth();
        final var number = databaseService.countNewMembers(firstDay, lastDay);
        final var text = String.format("new members joined JUG Switzerland in %s so far.", year);
        return new Stats(number, text);
    }

    private Stats getEventCountLastYear() {
        final var year = Year.now().minusYears(1);
        final var number = databaseService.countEventsByYear(year);
        final var text = String.format("events were organized by JUG Switzerland during %s.", year);
        return new Stats(number, text);
    }

    private Stats getEventCountThisYear() {
        final var year = Year.now();
        final var number = databaseService.countEventsByYear(year);
        final var text = String.format("events were organized by JUG Switzerland in %s so far.", year);
        return new Stats(number, text);
    }

    private record Stats(int number, String text) { }

}
