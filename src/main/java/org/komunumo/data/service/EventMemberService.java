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

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.db.tables.records.EventMemberRecord;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.MemberRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.komunumo.util.FormatterUtil;
import org.komunumo.util.URLUtil;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.select;
import static org.komunumo.data.db.tables.EventMember.EVENT_MEMBER;
import static org.komunumo.data.db.tables.EventOrganizer.EVENT_ORGANIZER;
import static org.komunumo.data.db.tables.Member.MEMBER;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Service
@SuppressWarnings("ClassCanBeRecord")
public class EventMemberService {

    private final DSLContext dsl;
    private final Configuration configuration;
    private final MailSender mailSender;

    public EventMemberService(@NotNull final DSLContext dsl,
                              @NotNull final Configuration configuration,
                              @NotNull final MailSender mailSender) {
        this.dsl = dsl;
        this.configuration = configuration;
        this.mailSender = mailSender;
    }

    public EventMemberRecord newRegistration() {
        return dsl.newRecord(EVENT_MEMBER);
    }

    public Optional<EventMemberRecord> get(@NotNull final Long eventId,
                                           @NotNull final Long memberId) {
        return dsl.fetchOptional(EVENT_MEMBER,
                EVENT_MEMBER.EVENT_ID.eq(eventId)
                        .and(EVENT_MEMBER.MEMBER_ID.eq(memberId)));
    }

    public void store(@NotNull final EventMemberRecord registration) {
        registration.store();
    }

    public void registerForEvent(@NotNull final Event event,
                                 @NotNull final Member member,
                                 @NotNull final String source) {
        final EventMemberRecord registration;

        final var hasRegistered = get(event.getId(), member.getId());
        if (hasRegistered.isEmpty()) {
            registration = dsl.newRecord(EVENT_MEMBER);
            registration.setEventId(event.getId());
            registration.setMemberId(member.getId());
            registration.setDate(LocalDateTime.now());
            registration.setSource(source);
            registration.setDeregister(RandomStringUtils.randomAlphanumeric(16));
            registration.setNoShow(false);
            registration.store();
        } else {
            registration = hasRegistered.get();
        }

        final var message = new SimpleMailMessage();
        message.setTo(member.getEmail());
        message.setFrom(configuration.getEmail().getAddress());
        message.setSubject("%s: Event-Registration for %s"
                .formatted(FormatterUtil.formatDate(event.getDate().toLocalDate()), member.getFullName()));
        message.setText("""
                You successfully registered for the following event:
                "%s" at %s in %s
                Details: %s%s
                                
                Registration date: %s
                Your name: %s
                Source: %s
                
                To deregister from this event, please click on the following link:
                %s%s?deregister=%s
                """.formatted(
                        event.getTitle(), formatDateTime(event.getDate()), event.getLocation(), configuration.getWebsite().getBaseUrl(),
                        event.getCompleteEventUrl(), formatDateTime(registration.getDate()), member.getFullName(), source,
                        configuration.getWebsite().getBaseUrl(), event.getCompleteEventUrl(), URLUtil.encode(registration.getDeregister())
        ));
        mailSender.send(message);
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

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    public boolean registerForEvent(final long eventId,
                                    final long memberId,
                                    @NotNull final LocalDateTime registerDate,
                                    final boolean noShow,
                                    @NotNull final String deregisterCode) {
        final var hasRegistered = get(eventId, memberId);
        if (hasRegistered.isEmpty()) {
            final var eventMember = dsl.newRecord(EVENT_MEMBER);
            eventMember.setEventId(eventId);
            eventMember.setMemberId(memberId);
            eventMember.setDate(registerDate);
            eventMember.setNoShow(noShow);
            eventMember.setDeregister(deregisterCode);
            eventMember.store();
        }
        return hasRegistered.isEmpty();
    }

    public boolean deregister(@NotNull final String deregisterCode) {
        final var registration = dsl.selectFrom(EVENT_MEMBER)
                .where(EVENT_MEMBER.DEREGISTER.eq(deregisterCode))
                .fetchOneInto(EventMemberRecord.class);

        if (registration != null) {
            return registration.delete() > 0;
        }

        return false;
    }

    public int count() {
        return dsl.fetchCount(EVENT_MEMBER);
    }

    public EventMemberRecord getRegistration(@NotNull final String deregisterCode) {
        return dsl.selectFrom(EVENT_MEMBER)
                .where(EVENT_MEMBER.DEREGISTER.eq(deregisterCode))
                .fetchOne();
    }
}
