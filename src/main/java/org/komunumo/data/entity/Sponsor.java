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
import javax.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDate;

@Entity
public class Sponsor extends AbstractEntity {

    private String name;
    @Lob
    private String logo;
    private LocalDate validFrom;
    private LocalDate validTo;

    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }
    public String getLogo() {
        return logo;
    }
    public void setLogo(final String logo) {
        this.logo = logo;
    }
    public LocalDate getValidFrom() {
        return validFrom;
    }
    public void setValidFrom(final LocalDate validFrom) {
        this.validFrom = validFrom;
    }
    public LocalDate getValidTo() {
        return validTo;
    }
    public void setValidTo(final LocalDate validTo) {
        this.validTo = validTo;
    }

}
