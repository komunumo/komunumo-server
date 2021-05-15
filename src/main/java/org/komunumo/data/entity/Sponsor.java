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
    public void setName(String name) {
        this.name = name;
    }
    public String getLogo() {
        return logo;
    }
    public void setLogo(String logo) {
        this.logo = logo;
    }
    public LocalDate getValidFrom() {
        return validFrom;
    }
    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }
    public LocalDate getValidTo() {
        return validTo;
    }
    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

}
