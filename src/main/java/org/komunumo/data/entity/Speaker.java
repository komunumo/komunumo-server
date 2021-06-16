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
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.tables.records.SpeakerRecord;

import static org.komunumo.data.db.tables.Speaker.SPEAKER;

public class Speaker {

    private final SpeakerRecord record;

    public Speaker(@NotNull final SpeakerRecord record) {
        this.record = record;
    }

    public SpeakerRecord getRecord() {
        return record;
    }

    public Long getId() {
        return record.get(SPEAKER.ID);
    }

    public String getFirstName() {
        return record.get(SPEAKER.FIRST_NAME);
    }

    public Speaker setFirstName(@NotNull final String firstName) {
        record.set(SPEAKER.FIRST_NAME, firstName);
        return this;
    }

    public String getLastName() {
        return record.get(SPEAKER.LAST_NAME);
    }

    public Speaker setLastName(@NotNull final String lastName) {
        record.set(SPEAKER.LAST_NAME, lastName);
        return this;
    }

    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }

    public String getCompany() {
        return record.get(SPEAKER.COMPANY);
    }

    public Speaker setCompany(@Nullable final String company) {
        record.set(SPEAKER.COMPANY, company);
        return this;
    }

    public String getEmail() {
        return record.get(SPEAKER.EMAIL);
    }

    public Speaker setEmail(@Nullable final String email) {
        record.set(SPEAKER.EMAIL, email);
        return this;
    }

    public String getTwitter() {
        return record.get(SPEAKER.TWITTER);
    }

    public Speaker setTwitter(@Nullable final String twitter) {
        record.set(SPEAKER.TWITTER, twitter);
        return this;
    }

    public Integer getEventCount() {
        return record.get(SPEAKER.EVENT_COUNT, Integer.class);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final var speaker = (Speaker) o;
        return getId().equals(speaker.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
