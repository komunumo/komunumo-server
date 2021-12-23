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
import org.komunumo.data.db.tables.records.RegistrationRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Registration;
import org.komunumo.data.entity.RegistrationResult;
import org.komunumo.util.FormatterUtil;
import org.komunumo.util.URLUtil;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.komunumo.data.db.tables.Registration.REGISTRATION;
import static org.komunumo.util.FormatterUtil.formatDateTime;

@Service
@SuppressWarnings("ClassCanBeRecord")
public class RegistrationService {

    private final DSLContext dsl;
    private final Configuration configuration;
    private final MailSender mailSender;

    public RegistrationService(@NotNull final DSLContext dsl,
                               @NotNull final Configuration configuration,
                               @NotNull final MailSender mailSender) {
        this.dsl = dsl;
        this.configuration = configuration;
        this.mailSender = mailSender;
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

    public int count() {
        return dsl.fetchCount(REGISTRATION);
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
}
