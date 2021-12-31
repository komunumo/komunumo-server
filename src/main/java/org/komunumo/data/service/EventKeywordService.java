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
import org.komunumo.data.db.tables.records.EventKeywordRecord;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.KeywordEntity;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.select;
import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;

interface EventKeywordService extends DSLContextGetter {

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    default EventKeywordRecord newEventKeyword() {
        return dsl().newRecord(EVENT_KEYWORD);
    }

    default Stream<KeywordEntity> getKeywordsForEvent(@NotNull final EventRecord event) {
        return dsl()
                .selectFrom(KEYWORD)
                .where(KEYWORD.ID.in(
                        select(EVENT_KEYWORD.KEYWORD_ID)
                                .from(EVENT_KEYWORD)
                                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                ))
                .fetchInto(KeywordEntity.class)
                .stream();
    }

    default void setEventKeywords(@NotNull final EventRecord eventRecord,
                                  @NotNull final Set<KeywordEntity> keywordEntities) {
        final var eventKeywords = new HashSet<KeywordEntity>(keywordEntities.size());
        eventKeywords.addAll(keywordEntities);
        getKeywordsForEvent(eventRecord).forEach(keywordEntity -> {
            if (eventKeywords.contains(keywordEntity)) {
                eventKeywords.remove(keywordEntity);
            } else {
                removeKeywordsFromEvent(eventRecord, keywordEntity);
            }
        });
        eventKeywords.forEach(keywordEntity -> addKeywordToEvent(eventRecord, keywordEntity));
    }

    private void addKeywordToEvent(@NotNull final EventRecord eventRecord,
                                   @NotNull final KeywordEntity keywordEntity) {
        final var eventKeyword = dsl().newRecord(EVENT_KEYWORD);
        eventKeyword.setEventId(eventRecord.getId());
        eventKeyword.setKeywordId(keywordEntity.id());
        eventKeyword.store();
    }

    private void removeKeywordsFromEvent(@NotNull final EventRecord event,
                                         @NotNull final KeywordEntity keywordEntity) {
        dsl().delete(EVENT_KEYWORD)
                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                .and(EVENT_KEYWORD.KEYWORD_ID.eq(keywordEntity.id()))
                .execute();
    }

    default void removeAllKeywordsFromEvent(@NotNull final Event event) {
        dsl().delete(EVENT_KEYWORD)
                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                .execute();
    }

}
