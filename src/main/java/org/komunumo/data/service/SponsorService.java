package org.komunumo.data.service;

import org.komunumo.data.entity.Sponsor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import javax.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDate;

@Service
public class SponsorService extends CrudService<Sponsor, Integer> {

    private SponsorRepository repository;

    public SponsorService(@Autowired SponsorRepository repository) {
        this.repository = repository;
    }

    @Override
    protected SponsorRepository getRepository() {
        return repository;
    }

}
