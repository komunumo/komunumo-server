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
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.komunumo.configuration.Configuration;
import org.komunumo.data.db.tables.records.RegistrationRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Registration;
import org.komunumo.data.entity.RegistrationListEntity;
import org.komunumo.data.entity.RegistrationMemberEntity;
import org.komunumo.data.entity.RegistrationResult;
import org.komunumo.util.FormatterUtil;
import org.komunumo.util.URLUtil;
import org.springframework.mail.MailSender;
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
@SuppressWarnings("ClassCanBeRecord")
public class RegistrationService {

    private final DSLContext dsl;
    private final Configuration configuration;
    private final MailSender mailSender;
    private final EventOrganizerService eventOrganizerService;
    private final MemberService memberService;

    public RegistrationService(@NotNull final DSLContext dsl,
                               @NotNull final Configuration configuration,
                               @NotNull final MailSender mailSender,
                               @NotNull final EventOrganizerService eventOrganizerService,
                               @NotNull final MemberService memberService) {
        this.dsl = dsl;
        this.configuration = configuration;
        this.mailSender = mailSender;
        this.eventOrganizerService = eventOrganizerService;
        this.memberService = memberService;
    }

    public Optional<Registration> get(@NotNull final Long eventId,
                                      @NotNull final Long memberId) {
        return dsl.selectFrom(REGISTRATION)
                .where(REGISTRATION.EVENT_ID.eq(eventId))
                .and(REGISTRATION.MEMBER_ID.eq(memberId))
                .fetchOptionalInto(Registration.class);
    }

    public RegistrationResult registerForEvent(@NotNull final Event event,
                                               @NotNull final Member member,
                                               @NotNull final String source) {
        return registerForEvent(event, member, LocalDateTime.now(), source, false, true);
    }

    public synchronized RegistrationResult registerForEvent(@NotNull final Event event,
                                                            @NotNull final Member member,
                                                            @NotNull final LocalDateTime date,
                                                            @NotNull final String source,
                                                            final boolean noShow,
                                                            final boolean sendConfirmationMail) {
        final RegistrationRecord registration;

        final var hasRegistered = get(event.getId(), member.getId());
        if (hasRegistered.isEmpty()) {
            final var attendeeLimit = event.getAttendeeLimit();
            if (attendeeLimit > 0) {
                final var attendeeCount = dsl.fetchCount(REGISTRATION, REGISTRATION.EVENT_ID.eq(event.getId()));
                if (attendeeCount >= attendeeLimit) {
                    return RegistrationResult.FULL;
                }
                if (attendeeCount + 1 >= (int) Math.round(attendeeLimit * 0.8)) {
                    final var percent = ((attendeeCount + 1) * 100) / (attendeeLimit);
                    final var emailAddresses = eventOrganizerService.getOrganizersForEvent(event)
                            .map(Member::getEmail)
                            .toArray(String[]::new);
                    final var message = new SimpleMailMessage();
                    message.setTo(emailAddresses);
                    message.setFrom(configuration.getEmail().getAddress());
                    message.setSubject("Reached %d %% of attendee limit".formatted(percent));
                    message.setText("""
                    Event: "%s" at %s in %s
                    Attendee limit: %d
                    Attendee count: %d
                    """.formatted(
                            event.getTitle(), formatDateTime(event.getDate()), event.getLocation(),
                            attendeeLimit, attendeeCount + 1
                    ));
                    mailSender.send(message);
                }
            }
            registration = dsl.newRecord(REGISTRATION);
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
        return hasRegistered.isPresent() ? RegistrationResult.EXISTING : RegistrationResult.SUCCESS;
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
            final var eventMember = dsl.newRecord(REGISTRATION);
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
        final var registration = getRegistration(deregisterCode);
        if (registration != null) {
            return registration.delete() > 0;
        }
        return false;
    }

    public void deregister(final long eventId, final long memberId) {
        get(eventId, memberId).ifPresent(Registration::delete);
    }

    public int count() {
        return dsl.fetchCount(REGISTRATION);
    }

    public int count(final long eventId) {
        return dsl.fetchCount(REGISTRATION, REGISTRATION.EVENT_ID.eq(eventId));
    }

    public Registration getRegistration(@NotNull final String deregisterCode) {
        return dsl.selectFrom(REGISTRATION)
                .where(REGISTRATION.DEREGISTER.eq(deregisterCode))
                .fetchOneInto(Registration.class);
    }

    public void updateNoShow(@NotNull final Registration registration, final boolean noShow) {
        registration.setNoShow(noShow);
        registration.store();
    }

    public Stream<RegistrationListEntity> find(final long eventId, final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.select(MEMBER.ID, MEMBER.FIRST_NAME, MEMBER.LAST_NAME, MEMBER.EMAIL, MEMBER.CITY,
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

    public List<RegistrationMemberEntity> getUnregisteredMembers(final long eventId) {
        return dsl.select(MEMBER.ID, MEMBER.FIRST_NAME, MEMBER.LAST_NAME, MEMBER.EMAIL)
                .from(MEMBER)
                .leftJoin(REGISTRATION).on(MEMBER.ID.eq(REGISTRATION.MEMBER_ID)
                        .and(REGISTRATION.EVENT_ID.eq(eventId)))
                .where(MEMBER.ACCOUNT_DELETED.isFalse()
                        .and(REGISTRATION.DATE.isNull()))
                .orderBy(MEMBER.FIRST_NAME, MEMBER.LAST_NAME)
                .fetchInto(RegistrationMemberEntity.class);
    }

    public Optional<Member> toMember(@NotNull final RegistrationMemberEntity registrationMemberEntity) {
        return memberService.get(registrationMemberEntity.memberId());
    }
}
