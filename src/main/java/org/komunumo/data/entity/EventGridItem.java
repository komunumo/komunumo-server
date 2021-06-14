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

package org.komunumo.data.entity;

import org.jooq.Record5;

import java.time.LocalDateTime;

import static org.komunumo.data.db.tables.Event.EVENT;

public class EventGridItem {

    private final Record5<Long, String, String, LocalDateTime, Boolean> record;

    public EventGridItem(final Record5<Long, String, String, LocalDateTime, Boolean> record) {
        this.record = record;
    }

    public Long getId() {
        return record.get(EVENT.ID);
    }

    public String getTitle() {
        return record.get(EVENT.TITLE);
    }

    public String getSpeaker() {
        return (String) record.get("speaker");
    }

    public LocalDateTime getDate() {
        return record.get(EVENT.DATE);
    }

    public Boolean getVisible() {
        return record.get(EVENT.VISIBLE);
    }

}
