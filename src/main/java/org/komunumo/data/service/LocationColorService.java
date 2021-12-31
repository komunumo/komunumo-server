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
import org.komunumo.data.db.tables.records.LocationColorRecord;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.LocationColor.LOCATION_COLOR;

@Service
public interface LocationColorService extends DSLContextGetter {

    default @NotNull LocationColorRecord newLocationColorRecord() {
        return dsl().newRecord(LOCATION_COLOR);
    }

    default Map<String, String> getAllLocationColors() {
        final var colors = new HashMap<String, String>();
        dsl().selectFrom(LOCATION_COLOR)
                .stream()
                .forEach(record -> colors.put(record.getLocation(), record.getColor()));
        return colors;
    }

    default Stream<LocationColorRecord> findLocationColors(final int offset, final int limit, @Nullable final String filter) {
        final var filterValue = filter == null || filter.isBlank() ? null : "%" + filter.trim() + "%";
        return dsl().selectFrom(LOCATION_COLOR)
                .where(filterValue == null ? DSL.noCondition() : LOCATION_COLOR.LOCATION.like(filterValue))
                .orderBy(LOCATION_COLOR.LOCATION)
                .offset(offset)
                .limit(limit)
                .stream();
    }

}
