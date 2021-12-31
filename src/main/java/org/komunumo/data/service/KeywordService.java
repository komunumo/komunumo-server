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
import org.komunumo.data.db.tables.records.KeywordRecord;
import org.komunumo.data.entity.KeywordEntity;
import org.komunumo.data.entity.KeywordListEntity;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;

@Service
public interface KeywordService extends DSLContextGetter {

    default KeywordRecord newKeyword() {
        return dsl().newRecord(KEYWORD);
    }

    default Stream<KeywordEntity> getAllKeywords() {
        return dsl().selectFrom(KEYWORD)
                .orderBy(KEYWORD.KEYWORD_)
                .fetchInto(KeywordEntity.class)
                .stream();
    }

    default Stream<KeywordListEntity> findKeywords(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().select(KEYWORD.ID, KEYWORD.KEYWORD_, DSL.count(EVENT_KEYWORD.EVENT_ID).as("event_count"))
                .from(KEYWORD)
                .leftJoin(EVENT_KEYWORD).on(KEYWORD.ID.eq(EVENT_KEYWORD.KEYWORD_ID))
                .where(filterValue == null ? DSL.noCondition() : KEYWORD.KEYWORD_.like(filterValue))
                .groupBy(KEYWORD.ID)
                .orderBy(KEYWORD.KEYWORD_)
                .offset(offset)
                .limit(limit)
                .fetchInto(KeywordListEntity.class)
                .stream();
    }

    default Optional<KeywordRecord> getKeywordRecord(@NotNull final Long id) {
        return dsl().selectFrom(KEYWORD)
                .where(KEYWORD.ID.eq(id))
                .fetchOptional();
    }

    default void deleteKeyword(final long keywordId) {
        getKeywordRecord(keywordId).ifPresent(KeywordRecord::delete);
    }
}
