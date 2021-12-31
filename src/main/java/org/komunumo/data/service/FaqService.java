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
import org.jetbrains.annotations.Nullable;
import org.jooq.impl.DSL;
import org.komunumo.data.db.tables.records.FaqRecord;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Faq.FAQ;

interface FaqService extends DSLContextGetter {

    default @NotNull FaqRecord newFaqRecord() {
        return dsl().newRecord(FAQ);
    }

    default Stream<FaqRecord> getAllFaqRecords() {
        return dsl().selectFrom(FAQ)
                .orderBy(FAQ.ID.asc())
                .fetch()
                .stream();
    }

    default Optional<FaqRecord> getFaqRecord(@NotNull final Long id) {
        return dsl().selectFrom(FAQ)
                .where(FAQ.ID.eq(id))
                .fetchOptional();
    }

    default Stream<FaqRecord> findFaqRecords(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().selectFrom(FAQ)
                .where(filterValue == null ? DSL.noCondition() :
                        FAQ.QUESTION.like(filterValue).or(FAQ.ANSWER.like(filterValue)))
                .orderBy(FAQ.ID.desc())
                .offset(offset)
                .limit(limit)
                .stream();
    }

}
