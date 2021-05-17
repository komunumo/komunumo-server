package org.komunumo.data.service;

import org.komunumo.data.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class EventService extends CrudService<Event, Integer> {

    private final EventRepository repository;

    public EventService(@Autowired final EventRepository repository) {
        this.repository = repository;
    }

    @Override
    protected EventRepository getRepository() {
        return repository;
    }

}
