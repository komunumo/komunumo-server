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
    private List<KeywordEntity> keywords;
    private int attendeeCount;

    /**
     * Set the speakers for this event.
     * @param eventSpeakerEntities list of {@link EventSpeakerEntity} objects (maybe empty)
     */
    public void setSpeakers(@NotNull final List<EventSpeakerEntity> eventSpeakerEntities) {
        this.speakers = Collections.unmodifiableList(eventSpeakerEntities);
    }

    /**
     * Get the speakers for this event.
     * @return list of {@link EventSpeakerEntity} objects (maybe empty)
     */
    public List<EventSpeakerEntity> getSpeakers() {
        return speakers;
    }

    /**
     * Set the keywords for this event.
     * @param keywordEntities list of {@link KeywordEntity} objects (maybe empty)
     */
    public void setKeywords(@NotNull final List<KeywordEntity> keywordEntities) {
        this.keywords = Collections.unmodifiableList(keywordEntities);
    }

    /**
     * Get the keywords for this event.
     * @return list of {@link KeywordEntity} objects (maybe empty)
     */
    public List<KeywordEntity> getKeywords() {
        return keywords;
    }

    /**
     * Set the attendee count for this event.
     * @param attendeeCount attendee count
     */
    public void setAttendeeCount(final int attendeeCount) {
        this.attendeeCount = attendeeCount;
    }

    /**
     * Get the attendee count for this event.
     * @return attendee count
     */
    public int getAttendeeCount() {
        return attendeeCount;
    }

    /**
     * Get the complete (absolute) event URL (without the host name and port).
     * @return the complete (absolute) event URL
     */
    public String getCompleteEventUrl() {
        return (getLocation().isBlank() || getDate() == null) ? ""
                : "/event/%s/%d/%s".formatted(
                        URLUtil.createReadableUrl(getLocation()),
                        getDate().getYear(),
                        getEventUrl());
    }

    /**
     * Get the complete (absolute) event URL (without the host name and port) for the event preview.
     * @return the complete (absolute) event URL for the preview
     */
    public String getCompleteEventPreviewUrl() {
        return "%s?preview=%s".formatted(getCompleteEventUrl(), getEventPreviewCode());
    }

    /**
     * Get the code to access the event preview.
     * @return the event preview code
     */
    public String getEventPreviewCode() {
        return Integer.toHexString(getCompleteEventUrl().hashCode());
    }

}
