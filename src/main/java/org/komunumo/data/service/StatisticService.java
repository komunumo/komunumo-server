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

package org.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.jfree.data.time.Month;
import org.jooq.impl.DSL;
import org.komunumo.data.db.enums.EventType;
import org.komunumo.data.entity.MonthlyVisitors;
import org.komunumo.data.entity.NoShows;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.Collection;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.jooq.impl.DSL.month;
import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.Member.MEMBER;
import static org.komunumo.data.db.tables.Registration.REGISTRATION;

interface StatisticService extends DSLContextGetter {

    default int countMembersByYear(@NotNull final Year year) {
        final var endOfYear = year.atMonth(DECEMBER).atEndOfMonth();
        return dsl().fetchCount(MEMBER,
                MEMBER.ACCOUNT_DELETED.isFalse().and(MEMBER.MEMBERSHIP_BEGIN.lessOrEqual(endOfYear)).and(
                        MEMBER.MEMBERSHIP_END.isNull().or(MEMBER.MEMBERSHIP_END.greaterOrEqual(endOfYear))));
    }

    default int countNewMembers(@NotNull final LocalDate fromDate, @NotNull final LocalDate toDate) {
        return dsl().fetchCount(MEMBER, MEMBER.ACCOUNT_DELETED.isFalse()
                .and(MEMBER.MEMBERSHIP_BEGIN.between(fromDate, toDate)));
    }

    default int countEventsByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl().fetchCount(EVENT, EVENT.DATE.between(firstDay, lastDay)
                .and(EVENT.TYPE.notEqual(EventType.Sponsored))
                .and(EVENT.PUBLISHED.isTrue()));
    }

    default int countAttendeesByYear(@NotNull final Year year, @NotNull final NoShows noShows) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl().fetchCount(REGISTRATION,
                REGISTRATION.DATE.between(firstDay, lastDay)
                        .and(noShows == NoShows.INCLUDE
                                ? DSL.noCondition()
                                : REGISTRATION.NO_SHOW.eq(noShows == NoShows.ONLY)));
    }

    default int countUniqueAttendeesByYear(@NotNull final Year year, @NotNull final NoShows noShows) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl().selectCount()
                .from(REGISTRATION)
                .where(REGISTRATION.DATE.between(firstDay, lastDay)
                        .and(noShows == NoShows.INCLUDE
                                ? DSL.noCondition()
                                : REGISTRATION.NO_SHOW.eq(noShows == NoShows.ONLY)))
                .groupBy(REGISTRATION.MEMBER_ID)
                .execute();
    }

    default Collection<MonthlyVisitors> calculateMonthlyVisitorsByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);

        return dsl().select(
                        EVENT.LOCATION.as("Location"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.JANUARY)).as("January"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.FEBRUARY)).as("February"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.MARCH)).as("March"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.APRIL)).as("April"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.MAY)).as("May"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.JUNE)).as("June"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.JULY)).as("July"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.AUGUST)).as("August"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.SEPTEMBER)).as("September"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.OCTOBER)).as("October"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.NOVEMBER)).as("November"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(Month.DECEMBER)).as("December"))
                .from(REGISTRATION)
                .leftJoin(EVENT).on(REGISTRATION.EVENT_ID.eq(EVENT.ID))
                .where(EVENT.DATE.greaterOrEqual(firstDay).and(EVENT.DATE.lessOrEqual(lastDay)))
                        .and(REGISTRATION.NO_SHOW.isFalse())
                .groupBy(EVENT.LOCATION)
                .orderBy(EVENT.LOCATION)
                .fetchInto(MonthlyVisitors.class);
    }

    default List<Year> getYearsWithEvents() {
        return dsl().selectDistinct(DSL.year(EVENT.DATE).as("year"))
                .from(EVENT)
                .where(EVENT.DATE.isNotNull())
                .orderBy(DSL.field("year").desc())
                .stream()
                .map(record -> Year.of((Integer) record.getValue("year")))
                .toList();
    }

}
