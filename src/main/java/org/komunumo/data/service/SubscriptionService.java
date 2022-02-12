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
import org.komunumo.data.entity.MailTemplateId;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.util.URLUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.komunumo.data.db.tables.Subscription.SUBSCRIPTION;

interface SubscriptionService extends DSLContextGetter, MailService {

    default Optional<SubscriptionRecord> getSubscription(@NotNull final String emailAddress) {
        return dsl().selectFrom(SUBSCRIPTION)
                .where(SUBSCRIPTION.EMAIL.eq(emailAddress))
                .fetchOptional();
    }

    default SubscriptionStatus addSubscription(@NotNull final String emailAddress) {
        final var subscriptionRecord = getSubscription(emailAddress).orElse(dsl().newRecord(SUBSCRIPTION));

        if (subscriptionRecord.getStatus() == null) {
            final var validationCode = RandomStringUtils.randomAlphabetic(16);
            subscriptionRecord.setEmail(emailAddress);
            subscriptionRecord.setSubscriptionDate(LocalDateTime.now());
            subscriptionRecord.setStatus(SubscriptionStatus.PENDING);
            subscriptionRecord.setValidationCode(validationCode);
            subscriptionRecord.store();
        }

        if (subscriptionRecord.getStatus() == SubscriptionStatus.PENDING) {
            final var link = "%s/newsletter/subscription/validation?email=%s&code=%s".formatted(
                    configuration().getWebsiteBaseUrl(),
                    URLUtil.encode(subscriptionRecord.getEmail()),
                    URLUtil.encode(subscriptionRecord.getValidationCode()));
            sendMail(MailTemplateId.EVENT_REGISTRATION_LIMIT_REACHED, Map.of("validation.url", link), subscriptionRecord.getEmail());

        }

        return subscriptionRecord.getStatus();
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

            sendMail(MailTemplateId.NEWSLETTER_SUBSCRIPTION_CONFIRMATION, null, emailAddress);

            return true;
        }
        return false;
    }

}
