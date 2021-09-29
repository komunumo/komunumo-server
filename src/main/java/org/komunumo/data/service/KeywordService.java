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
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.komunumo.data.entity.Keyword;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.EventKeyword.EVENT_KEYWORD;
import static org.komunumo.data.db.tables.Keyword.KEYWORD;

@Service
public class KeywordService {

    private final DSLContext dsl;

    public KeywordService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Keyword newKeyword() {
        return dsl.newRecord(KEYWORD)
                .into(Keyword.class);
    }

    public int count() {
        return dsl.fetchCount(KEYWORD);
    }

    public Stream<Keyword> getAllKeywords() {
        return dsl.selectFrom(KEYWORD)
                .orderBy(KEYWORD.KEYWORD_)
                .fetchInto(Keyword.class)
                .stream();
    }

    public Stream<Keyword> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.selectFrom(KEYWORD)
                .where(filterValue == null ? DSL.noCondition() : KEYWORD.KEYWORD_.like(filterValue))
                .groupBy(KEYWORD.ID)
                .orderBy(KEYWORD.KEYWORD_)
                .offset(offset)
                .limit(limit)
                .fetchInto(Keyword.class)
                .stream()
                .map(this::addEventCount);
    }

    private Keyword addEventCount(@NotNull final Keyword keyword) {
        final var eventCount = dsl.fetchCount(EVENT_KEYWORD, EVENT_KEYWORD.KEYWORD_ID.eq(keyword.getId()));
        keyword.setEventCount(eventCount);
        return keyword;
    }

    public Optional<Keyword> get(@NotNull final Long id) {
        return dsl.selectFrom(KEYWORD)
                .where(KEYWORD.ID.eq(id))
                .fetchOptionalInto(Keyword.class);
    }

    public void store(@NotNull final Keyword keyword) {
        keyword.store();
    }

    public void delete(@NotNull final Keyword keyword) {
        keyword.delete();
    }
}
