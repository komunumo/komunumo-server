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

import java.util.HashSet;
import java.util.Set;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.select;
import static org.komunumo.data.db.tables.EventOrganizer.EVENT_ORGANIZER;
import static org.komunumo.data.db.tables.Member.MEMBER;

@Service
public class EventOrganizerService {

    private final DSLContext dsl;

    public EventOrganizerService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Stream<Member> getOrganizersForEvent(@NotNull final EventRecord event) {
        return dsl
                .selectFrom(MEMBER)
                .where(MEMBER.ID.in(
                        select(EVENT_ORGANIZER.MEMBER_ID)
                                .from(EVENT_ORGANIZER)
                                .where(EVENT_ORGANIZER.EVENT_ID.eq(event.getId()))
                ))
                .fetchInto(Member.class)
                .stream();
    }

    public void setEventOrganizers(@NotNull final EventRecord event,
                                   @NotNull final Set<Member> organizers) {
        final var eventOrganizers = new HashSet<Member>(organizers.size());
        eventOrganizers.addAll(organizers);
        getOrganizersForEvent(event).forEach(organizer -> {
            if (eventOrganizers.contains(organizer)) {
                eventOrganizers.remove(organizer);
            } else {
                removeOrganizersFromEvent(event, organizer);
            }
        });
        eventOrganizers.forEach(organizer -> addOrganizerToEvent(event, organizer));
    }

    private void addOrganizerToEvent(@NotNull final EventRecord event,
                                     @NotNull final MemberRecord organizer) {
        final var eventOrganizer = dsl.newRecord(EVENT_ORGANIZER);
        eventOrganizer.setEventId(event.getId());
        eventOrganizer.setMemberId(organizer.getId());
        eventOrganizer.store();
    }

    private void removeOrganizersFromEvent(@NotNull final EventRecord event,
                                           @NotNull final MemberRecord organizer) {
        dsl.delete(EVENT_ORGANIZER)
                .where(EVENT_ORGANIZER.EVENT_ID.eq(event.getId()))
                .and(EVENT_ORGANIZER.MEMBER_ID.eq(organizer.getId()))
                .execute();
    }

    public void removeAllOrganizersFromEvent(@NotNull final Event event) {
        dsl.delete(EVENT_ORGANIZER)
                .where(EVENT_ORGANIZER.EVENT_ID.eq(event.getId()))
                .execute();
    }
}
