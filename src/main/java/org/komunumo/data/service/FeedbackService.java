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
import org.komunumo.data.db.tables.records.FeedbackRecord;
import org.komunumo.data.entity.MailTemplateId;
import org.komunumo.data.service.getter.ConfigurationGetter;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.time.LocalDateTime;
import java.util.Map;

import static org.komunumo.data.db.tables.Feedback.FEEDBACK;
import static org.komunumo.util.FormatterUtil.formatDateTime;

interface FeedbackService extends DSLContextGetter, ConfigurationGetter, MailService {

    default FeedbackRecord newFeedback() {
        final var feedbackRecord = dsl().newRecord(FEEDBACK);
        feedbackRecord.setReceived(LocalDateTime.now());
        return feedbackRecord;
    }

    default void receiveFeedback(@NotNull final String firstName,
                                 @NotNull final String lastName,
                                 @NotNull final String email,
                                 @NotNull final String feedback) {
        final var feedbackRecord = newFeedback();
        feedbackRecord.setFirstName(firstName);
        feedbackRecord.setLastName(lastName);
        feedbackRecord.setEmail(email);
        feedbackRecord.setFeedback(feedback);
        feedbackRecord.store();

        final var variables = Map.of(
                "id", Long.toString(feedbackRecord.getId()),
                "received", formatDateTime(feedbackRecord.getReceived()),
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "feedback", feedback
        );
        sendMail(MailTemplateId.MEMBER_FEEDBACK, variables, configuration().getWebsiteContactEmail());
    }

}
