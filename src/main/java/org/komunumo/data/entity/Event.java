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

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.util.URLUtil;

import java.util.Collections;
import java.util.List;

public class Event extends EventRecord {

    private List<EventSpeakerEntity> speakers;
    private List<Keyword> keywords;
    private int attendeeCount;

    public void setSpeakers(@NotNull final List<EventSpeakerEntity> eventSpeakerEntities) {
        this.speakers = Collections.unmodifiableList(eventSpeakerEntities);
    }

    public List<EventSpeakerEntity> getSpeakers() {
        return speakers;
    }

    public void setKeywords(@NotNull final List<Keyword> keywords) {
        this.keywords = Collections.unmodifiableList(keywords);
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setAttendeeCount(final int attendeeCount) {
        this.attendeeCount = attendeeCount;
    }

    public int getAttendeeCount() {
        return attendeeCount;
    }

    public String getCompleteEventUrl() {
        return "/event/%s/%d/%s".formatted(
                URLUtil.createReadableUrl(getLocation()),
                getDate().getYear(), getEventUrl());
    }
}
