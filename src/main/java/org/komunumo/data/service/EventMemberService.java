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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.EventMemberRecord;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.springframework.stereotype.Service;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.month;
import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.EventMember.EVENT_MEMBER;

@Service
public class EventMemberService {

    private final DSLContext dsl;

    public EventMemberService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<EventMemberRecord> get(@NotNull final Long eventId,
                                           @NotNull final Long memberId) {
        return dsl.fetchOptional(EVENT_MEMBER,
                EVENT_MEMBER.EVENT_ID.eq(eventId)
                        .and(EVENT_MEMBER.MEMBER_ID.eq(memberId)));
    }

    public void registerForEvent(@NotNull final EventRecord event,
                                  @NotNull final MemberRecord member) {
        final var hasRegistered = get(event.getId(), member.getId());
        if (hasRegistered.isEmpty()) {
            final var eventMember = dsl.newRecord(EVENT_MEMBER);
            eventMember.setEventId(event.getId());
            eventMember.setMemberId(member.getId());
            eventMember.setDate(LocalDateTime.now());
            eventMember.setNoShow(false);
            eventMember.store();
        }
    }

    @Deprecated(forRemoval = true) // TODO remove after migration of JUG.CH to Komunumo has finished
    public void registerForEvent(final long eventId,
                                  final long memberId,
                                  @NotNull final LocalDateTime registerDate,
                                  final boolean noShow) {
        final var hasRegistered = get(eventId, memberId);
        if (hasRegistered.isEmpty()) {
            final var eventMember = dsl.newRecord(EVENT_MEMBER);
            eventMember.setEventId(eventId);
            eventMember.setMemberId(memberId);
            eventMember.setDate(registerDate);
            eventMember.setNoShow(noShow);
            eventMember.store();
        }
    }

    public void unregisterFromEvent(@NotNull final EventRecord event,
                                     @NotNull final MemberRecord member) {
        dsl.delete(EVENT_MEMBER)
                .where(EVENT_MEMBER.EVENT_ID.eq(event.getId()))
                .and(EVENT_MEMBER.MEMBER_ID.eq(member.getId()))
                .execute();
    }

    public int count() {
        return dsl.fetchCount(EVENT_MEMBER);
    }

    public int countByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);
        return dsl.fetchCount(EVENT_MEMBER, EVENT_MEMBER.DATE.between(firstDay, lastDay));
    }

    public int calculateNoShowRateByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);

        final var registered = countByYear(year);
        final var noShows =  dsl.fetchCount(EVENT_MEMBER,
                EVENT_MEMBER.DATE.between(firstDay, lastDay)
                        .and(condition(EVENT_MEMBER.NO_SHOW)));

        return registered > 0 ? noShows * 100 / registered : 0;
    }

    public Collection<MonthlyVisitors> calculateMonthlyVisitorsByYear(@NotNull final Year year) {
        final var firstDay = year.atMonth(JANUARY).atDay(1).atTime(LocalTime.MIN);
        final var lastDay = year.atMonth(DECEMBER).atEndOfMonth().atTime(LocalTime.MAX);

        return dsl.select(
                        EVENT.LOCATION.as("Location"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(1)).as("January"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(2)).as("February"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(3)).as("March"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(4)).as("April"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(5)).as("May"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(6)).as("June"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(7)).as("July"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(8)).as("August"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(9)).as("September"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(10)).as("October"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(11)).as("November"),
                        DSL.count().filterWhere(month(EVENT.DATE).eq(12)).as("December"))
                .from(EVENT_MEMBER)
                .leftJoin(EVENT).on(EVENT_MEMBER.EVENT_ID.eq(EVENT.ID))
                .where(EVENT.DATE.greaterOrEqual(firstDay).and(EVENT.DATE.lessOrEqual(lastDay)))
                .groupBy(EVENT.LOCATION)
                .orderBy(EVENT.LOCATION)
                .fetchInto(MonthlyVisitors.class);
    }

    @SuppressWarnings("unused") // setters used via reflection by jOOQ
    public static class MonthlyVisitors {
        private String location;
        private int january, february, march, april, may, june, july, august, september, october, november, december;

        public String getLocation() {
            return location;
        }

        public void setLocation(@NotNull final String location) {
            this.location = location;
        }

        public int getJanuary() {
            return january;
        }

        public void setJanuary(final int january) {
            this.january = january;
        }

        public int getFebruary() {
            return february;
        }

        public void setFebruary(final int february) {
            this.february = february;
        }

        public int getMarch() {
            return march;
        }

        public void setMarch(final int march) {
            this.march = march;
        }

        public int getApril() {
            return april;
        }

        public void setApril(final int april) {
            this.april = april;
        }

        public int getMay() {
            return may;
        }

        public void setMay(final int may) {
            this.may = may;
        }

        public int getJune() {
            return june;
        }

        public void setJune(final int june) {
            this.june = june;
        }

        public int getJuly() {
            return july;
        }

        public void setJuly(final int july) {
            this.july = july;
        }

        public int getAugust() {
            return august;
        }

        public void setAugust(final int august) {
            this.august = august;
        }

        public int getSeptember() {
            return september;
        }

        public void setSeptember(final int september) {
            this.september = september;
        }

        public int getOctober() {
            return october;
        }

        public void setOctober(final int october) {
            this.october = october;
        }

        public int getNovember() {
            return november;
        }

        public void setNovember(final int november) {
            this.november = november;
        }

        public int getDecember() {
            return december;
        }

        public void setDecember(final int december) {
            this.december = december;
        }
    }
}
