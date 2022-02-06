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
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.entity.Page;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Page.PAGE;

interface PageService extends DSLContextGetter {

    default Page newPage() {
        final var page = dsl().newRecord(PAGE)
                .into(Page.class);
        page.setParent(null);
        page.setPageUrl("");
        page.setTitle("");
        page.setContent("");
        return page;
    }

    default Stream<Page> findPages(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().select(PAGE.asterisk())
                .from(PAGE)
                .where(filterValue == null ? DSL.noCondition() :
                        PAGE.PAGE_URL.like(filterValue).or(PAGE.TITLE.like(filterValue)))
                .orderBy(PAGE.PARENT.asc(), PAGE.TITLE.asc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Page.class)
                .stream();
    }

    default Stream<Page> getPages(@NotNull final PageParent parent) {
        return dsl().select(PAGE.asterisk())
                .from(PAGE)
                .where(PAGE.PARENT.eq(parent))
                .orderBy(PAGE.ID.asc())
                .fetchInto(Page.class)
                .stream();
    }

    default Optional<Page> getPage(@NotNull final PageParent parent, @NotNull final String url) {
        return dsl().select(PAGE.asterisk())
                .from(PAGE)
                .where(PAGE.PARENT.eq(parent).and(PAGE.PAGE_URL.eq(url)))
                .fetchOptionalInto(Page.class);
    }
}
