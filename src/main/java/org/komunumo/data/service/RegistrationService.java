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
import org.jetbrains.annotations.Nullable;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.RegistrationRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Registration;
import org.komunumo.data.entity.RegistrationListEntity;
import org.komunumo.data.entity.RegistrationMemberEntity;
import org.komunumo.data.entity.RegistrationResult;
import org.komunumo.data.service.getter.ConfigurationGetter;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.data.service.getter.MailSenderGetter;
import org.komunumo.util.FormatterUtil;
import org.komunumo.util.URLUtil;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Member.MEMBER;
import static org.komunumo.data.db.tables.Registration.REGISTRATION;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Service
public interface RegistrationService extends ConfigurationGetter, DSLContextGetter, MailSenderGetter, EventOrganizerService {

    default Optional<Registration> getRegistration(@NotNull final Long eventId,
                                                  @NotNull final Long memberId) {
        return dsl().selectFrom(REGISTRATION)
                .where(REGISTRATION.EVENT_ID.eq(eventId))
                .and(REGISTRATION.MEMBER_ID.eq(memberId))
                .fetchOptionalInto(Registration.class);
    }

    default RegistrationResult registerForEvent(@NotNull final Event event,
                                               @NotNull final Member member,
                                               @NotNull final String source) {
        return registerForEvent(event, member, LocalDateTime.now(), source, false, true);
    }

    default RegistrationResult registerForEvent(@NotNull final Event event,
                                                            @NotNull final Member member,
                                                            @NotNull final LocalDateTime date,
                                                            @NotNull final String source,
                                                            final boolean noShow,
                                                            final boolean sendConfirmationMail) {
        synchronized (LockHolder.LOCK) {
            final RegistrationRecord registration;

            final var hasRegistered = getRegistration(event.getId(), member.getId());
            if (hasRegistered.isEmpty()) {
                final var attendeeLimit = event.getAttendeeLimit();
                if (attendeeLimit > 0) {
                    final var attendeeCount = dsl().fetchCount(REGISTRATION, REGISTRATION.EVENT_ID.eq(event.getId()));
                    if (attendeeCount >= attendeeLimit) {
                        return RegistrationResult.FULL;
                    }
                    if (attendeeCount + 1 >= (int) Math.round(attendeeLimit * 0.8)) {
                        final var percent = ((attendeeCount + 1) * 100) / (attendeeLimit);
                        final var emailAddresses = getOrganizersForEvent(event)
                                .map(Member::getEmail)
                                .toArray(String[]::new);
                        final var message = new SimpleMailMessage();
                        message.setTo(emailAddresses);
                        message.setFrom(configuration().getWebsiteContactEmail());
                        message.setSubject("Reached %d %% of attendee limit".formatted(percent));
                        message.setText("""
                                Event: "%s" at %s in %s
                                Attendee limit: %d
                                Attendee count: %d
                                """.formatted(
                                event.getTitle(), formatDateTime(event.getDate()), event.getLocation(),
                                attendeeLimit, attendeeCount + 1
                        ));
                        mailSender().send(message);
                    }
                }
                registration = dsl().newRecord(REGISTRATION);
                registration.setEventId(event.getId());
                registration.setMemberId(member.getId());
                registration.setDate(date);
                registration.setSource(source);
                registration.setDeregister(RandomStringUtils.randomAlphanumeric(16));
                registration.setNoShow(noShow);
                registration.store();
            } else {
                registration = hasRegistered.get();
            }

            if (sendConfirmationMail) {
                final var message = new SimpleMailMessage();
                message.setTo(member.getEmail());
                message.setFrom(configuration().getWebsiteContactEmail());
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
                        event.getTitle(), formatDateTime(event.getDate()), event.getLocation(), configuration().getWebsiteBaseUrl(),
                        event.getCompleteEventUrl(), formatDateTime(registration.getDate()), member.getFullName(), source,
                        configuration().getWebsiteBaseUrl(), event.getCompleteEventUrl(), URLUtil.encode(registration.getDeregister())
                ));
                mailSender().send(message);
            }
            return hasRegistered.isPresent() ? RegistrationResult.EXISTING : RegistrationResult.SUCCESS;
        }
    }

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    default boolean registerForEvent(final long eventId,
                                     final long memberId,
                                     @NotNull final LocalDateTime registerDate,
                                     final boolean noShow,
                                     @NotNull final String deregisterCode) {
        final var hasRegistered = getRegistration(eventId, memberId);
        if (hasRegistered.isEmpty()) {
            final var eventMember = dsl().newRecord(REGISTRATION);
            eventMember.setEventId(eventId);
            eventMember.setMemberId(memberId);
            eventMember.setDate(registerDate);
            eventMember.setNoShow(noShow);
            eventMember.setDeregister(deregisterCode);
            eventMember.store();
        }
        return hasRegistered.isEmpty();
    }

    default boolean deregisterFromEvent(@NotNull final String deregisterCode) {
        final var registration = getRegistration(deregisterCode);
        if (registration != null) {
            return registration.delete() > 0;
        }
        return false;
    }

    default void deregisterFromEvent(final long eventId, final long memberId) {
        getRegistration(eventId, memberId).ifPresent(Registration::delete);
    }

    default int countRegistrations(final long eventId) {
        return dsl().fetchCount(REGISTRATION, REGISTRATION.EVENT_ID.eq(eventId));
    }

    default Registration getRegistration(@NotNull final String deregisterCode) {
        return dsl().selectFrom(REGISTRATION)
                .where(REGISTRATION.DEREGISTER.eq(deregisterCode))
                .fetchOneInto(Registration.class);
    }

    default void updateNoShow(@NotNull final Registration registration, final boolean noShow) {
        registration.setNoShow(noShow);
        registration.store();
    }

    default Stream<RegistrationListEntity> findRegistrations(final long eventId, final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().select(MEMBER.ID, MEMBER.FIRST_NAME, MEMBER.LAST_NAME, MEMBER.EMAIL, MEMBER.CITY,
                        REGISTRATION.DATE, REGISTRATION.SOURCE, REGISTRATION.NO_SHOW)
                .from(REGISTRATION)
                .join(MEMBER).on(REGISTRATION.MEMBER_ID.eq(MEMBER.ID))
                .where(REGISTRATION.EVENT_ID.eq(eventId)
                        .and(MEMBER.ACCOUNT_DELETED.isFalse())
                        .and(filterValue == null ? DSL.noCondition() :
                                MEMBER.FIRST_NAME.like(filterValue)
                                        .or(MEMBER.LAST_NAME.like(filterValue))
                                        .or(MEMBER.EMAIL.like(filterValue))
                                        .or(REGISTRATION.SOURCE.like(filterValue))))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .offset(offset)
                .limit(limit)
                .fetchInto(RegistrationListEntity.class)
                .stream();
    }

    default List<RegistrationMemberEntity> getUnregisteredMembers(final long eventId) {
        return dsl().select(MEMBER.ID, MEMBER.FIRST_NAME, MEMBER.LAST_NAME, MEMBER.EMAIL)
                .from(MEMBER)
                .leftJoin(REGISTRATION).on(MEMBER.ID.eq(REGISTRATION.MEMBER_ID)
                        .and(REGISTRATION.EVENT_ID.eq(eventId)))
                .where(MEMBER.ACCOUNT_DELETED.isFalse()
                        .and(REGISTRATION.DATE.isNull()))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .fetchInto(RegistrationMemberEntity.class);
    }

    class LockHolder {
        public static final Object LOCK = new Object();
    }

}
