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
import org.komunumo.data.db.tables.records.NewsRecord;
import org.komunumo.data.entity.NewsEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.News.NEWS;

@Service
public class NewsService {

    private final DSLContext dsl;

    public NewsService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public NewsRecord newNews() {
        final var news = dsl.newRecord(NEWS);
        news.setCreated(LocalDateTime.now());
        news.setTitle("");
        news.setSubtitle("");
        return news;
    }

    public int count() {
        return dsl.fetchCount(NEWS);
    }

    public Stream<NewsEntity> find(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl.select(NEWS.asterisk())
                .from(NEWS)
                .where(filterValue == null ? DSL.noCondition() :
                        NEWS.TITLE.like(filterValue).or(NEWS.SUBTITLE.like(filterValue)))
                .orderBy(NEWS.CREATED.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(NewsEntity.class)
                .stream();
    }

    public Optional<NewsEntity> getWhenVisible(@NotNull final Long id) {
        return dsl.selectFrom(NEWS)
                .where(NEWS.ID.eq(id))
                .and(NEWS.SHOW_FROM.isNull().or(NEWS.SHOW_FROM.greaterOrEqual(LocalDateTime.now())))
                .and(NEWS.SHOW_TO.isNull().or(NEWS.SHOW_TO.lessOrEqual(LocalDateTime.now())))
                .fetchOptionalInto(NewsEntity.class);
    }

    public Optional<NewsRecord> getNewsRecord(@NotNull final Long id) {
        return dsl.selectFrom(NEWS)
                .where(NEWS.ID.eq(id))
                .fetchOptional();
    }

    public void store(@NotNull final NewsRecord newsRecord) {
        newsRecord.store();
    }

    public void delete(final long newsId) {
        getNewsRecord(newsId).ifPresent(NewsRecord::delete);
    }

    public NewsEntity getLatestNews() {
        return dsl.selectFrom(NEWS)
                .where(NEWS.SHOW_FROM.isNull().or(NEWS.SHOW_FROM.greaterOrEqual(LocalDateTime.now())))
                .and(NEWS.SHOW_TO.isNull().or(NEWS.SHOW_TO.lessOrEqual(LocalDateTime.now())))
                .orderBy(NEWS.CREATED.desc())
                .limit(1)
                .fetchOneInto(NewsEntity.class);
    }

    public List<NewsEntity> getVisibleNews() {
        return dsl.selectFrom(NEWS)
                .where(NEWS.SHOW_FROM.isNull().or(NEWS.SHOW_FROM.greaterOrEqual(LocalDateTime.now())))
                .and(NEWS.SHOW_TO.isNull().or(NEWS.SHOW_TO.lessOrEqual(LocalDateTime.now())))
                .orderBy(NEWS.CREATED.desc())
                .fetchInto(NewsEntity.class);
    }
}
