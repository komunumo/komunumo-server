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
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.EventKeywordRecord;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.db.tables.records.KeywordRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Keyword;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.select;
import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;

@Service
public class EventKeywordService {

    private final DSLContext dsl;

    public EventKeywordService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    public EventKeywordRecord newEventKeyword() {
        return dsl.newRecord(EVENT_KEYWORD);
    }

    /**
     * @deprecated remove after migration of JUG.CH to Komunumo has finished
     */
    @Deprecated(forRemoval = true)
    public void store(@NotNull final EventKeywordRecord event) {
        event.store();
    }

    public Stream<Keyword> getKeywordsForEvent(@NotNull final EventRecord event) {
        return dsl
                .selectFrom(KEYWORD)
                .where(KEYWORD.ID.in(
                        select(EVENT_KEYWORD.KEYWORD_ID)
                                .from(EVENT_KEYWORD)
                                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                ))
                .fetchInto(Keyword.class)
                .stream();
    }

    public void setEventKeywords(@NotNull final EventRecord event,
                                 @NotNull final Set<Keyword> keywords) {
        final var eventKeywords = new HashSet<Keyword>(keywords.size());
        eventKeywords.addAll(keywords);
        getKeywordsForEvent(event).forEach(keyword -> {
            if (eventKeywords.contains(keyword)) {
                eventKeywords.remove(keyword);
            } else {
                removeKeywordsFromEvent(event, keyword);
            }
        });
        eventKeywords.forEach(keyword -> addKeywordToEvent(event, keyword));
    }

    private void addKeywordToEvent(@NotNull final EventRecord event,
                                   @NotNull final KeywordRecord keyword) {
        final var eventKeyword = dsl.newRecord(EVENT_KEYWORD);
        eventKeyword.setEventId(event.getId());
        eventKeyword.setKeywordId(keyword.getId());
        eventKeyword.store();
    }

    private void removeKeywordsFromEvent(@NotNull final EventRecord event,
                                         @NotNull final KeywordRecord keyword) {
        dsl.delete(EVENT_KEYWORD)
                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                .and(EVENT_KEYWORD.KEYWORD_ID.eq(keyword.getId()))
                .execute();
    }

    public void removeAllKeywordsFromEvent(@NotNull final Event event) {
        dsl.delete(EVENT_KEYWORD)
                .where(EVENT_KEYWORD.EVENT_ID.eq(event.getId()))
                .execute();
    }
}
