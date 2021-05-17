package org.komunumo.data.service;

import org.komunumo.data.entity.Sponsor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class SponsorService extends CrudService<Sponsor, Integer> {

    private final SponsorRepository repository;

    public SponsorService(@Autowired final SponsorRepository repository) {
        this.repository = repository;
    }

    @Override
    protected SponsorRepository getRepository() {
        return repository;
    }

}
