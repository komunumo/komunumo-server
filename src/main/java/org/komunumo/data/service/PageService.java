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
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.db.tables.records.PageRecord;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.Optional;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Page.PAGE;

interface PageService extends DSLContextGetter {

    default Stream<PageRecord> getPages(@NotNull final PageParent parent) {
        return dsl().selectFrom(PAGE)
                .where(PAGE.PARENT.eq(parent))
                .orderBy(PAGE.ID.asc())
                .fetch()
                .stream();
    }

    default Optional<PageRecord> getPage(@NotNull final PageParent parent, @NotNull final String url) {
        return dsl().selectFrom(PAGE)
                .where(PAGE.PARENT.eq(parent).and(PAGE.PAGE_URL.eq(url)))
                .fetchOptional();
    }
}
