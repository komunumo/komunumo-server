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
import org.komunumo.data.db.enums.SubscriptionStatus;
import org.komunumo.data.db.tables.records.SubscriptionRecord;
import org.komunumo.data.service.getter.ConfigurationGetter;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.data.service.getter.MailSenderGetter;
import org.komunumo.util.URLUtil;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.komunumo.data.db.tables.Subscription.SUBSCRIPTION;

interface SubscriptionService extends ConfigurationGetter, DSLContextGetter, MailSenderGetter {

    default Optional<SubscriptionRecord> getSubscription(@NotNull final String emailAddress) {
        return dsl().selectFrom(SUBSCRIPTION)
                .where(SUBSCRIPTION.EMAIL.eq(emailAddress))
                .fetchOptional();
    }

    default void addSubscription(@NotNull final String emailAddress) {
        if (getSubscription(emailAddress).isEmpty()) {
            final var subscription = dsl().newRecord(SUBSCRIPTION);

            final var validationCode = RandomStringUtils.randomAlphabetic(16);

            subscription.setEmail(emailAddress);
            subscription.setSubscriptionDate(LocalDateTime.now());
            subscription.setStatus(SubscriptionStatus.PENDING);
            subscription.setValidationCode(validationCode);
            subscription.store();

            final var link = "%s/newsletter/subscription/validation?email=%s&code=%s"
                    .formatted(configuration().getWebsiteBaseUrl(), URLUtil.encode(emailAddress), URLUtil.encode(validationCode));

            final var message = new SimpleMailMessage();
            message.setTo(emailAddress);
            message.setFrom(configuration().getWebsiteContactEmail());
            message.setSubject("Validate your newsletter subscription");
            message.setText("Please click on the following link to validate your newsletter subscription:\n" + link);
            mailSender().send(message);
        }
    }

    default boolean validateSubscription(@NotNull final String emailAddress, @NotNull final String validationCode) {
        final var subscription =  dsl().selectFrom(SUBSCRIPTION)
                .where(SUBSCRIPTION.EMAIL.eq(emailAddress))
                .and(SUBSCRIPTION.VALIDATION_CODE.eq(validationCode))
                .fetchOne();
        if (subscription != null) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setValidationCode(null);
            subscription.store();

            final var message = new SimpleMailMessage();
            message.setTo(emailAddress);
            message.setFrom(configuration().getWebsiteContactEmail());
            message.setSubject("Newsletter subscription activated");
            message.setText("Thank you very much for subscribing to our newsletter.");
            mailSender().send(message);

            return true;
        }
        return false;
    }

}
