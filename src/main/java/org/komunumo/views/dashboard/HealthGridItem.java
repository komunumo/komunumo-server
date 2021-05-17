package org.komunumo.views.dashboard;

import java.time.LocalDate;

/**
 * Simple DTO class for the inbox list to demonstrate complex object data
 */
public class HealthGridItem {

    private LocalDate date;
    private String city;
    private String country;
    private String status;
    private String theme;

    public HealthGridItem(final LocalDate date, final String city, final String country,
                          final String status, final String theme) {
        this.date = date;
        this.city = city;
        this.country = country;
        this.status = status;
        this.theme = theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }
}
