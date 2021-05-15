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
    public void setTitle(String title) {
        this.title = title;
    }
    public String getSpeaker() {
        return speaker;
    }
    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
