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

import javax.persistence.Entity;

import org.komunumo.data.AbstractEntity;
import java.time.LocalDateTime;

@Entity
public class Event extends AbstractEntity {

    private String title;
    private String speaker;
    private LocalDateTime date;
    private boolean visible;

    public String getTitle() {
        return title;
    }
    public void setTitle(final String title) {
        this.title = title;
    }
    public String getSpeaker() {
        return speaker;
    }
    public void setSpeaker(final String speaker) {
        this.speaker = speaker;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

}
