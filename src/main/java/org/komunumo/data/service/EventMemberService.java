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
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventMemberRecord;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.komunumo.data.db.tables.EventMember.EVENT_MEMBER;

@Service
public class EventMemberService {

    private final DSLContext dsl;

    public EventMemberService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<EventMemberRecord> get(@NotNull final Long eventId,
                                           @NotNull final Long memberId) {
        return Optional.ofNullable(dsl.selectFrom(EVENT_MEMBER)
                .where(EVENT_MEMBER.EVENT_ID.eq(eventId).and(EVENT_MEMBER.MEMBER_ID.eq(memberId)))
                .fetchOne());
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
}
