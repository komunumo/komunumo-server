package org.komunumo.data.service;

import org.komunumo.data.entity.Member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import java.time.LocalDate;

@Service
public class MemberService extends CrudService<Member, Integer> {

    private MemberRepository repository;

    public MemberService(@Autowired MemberRepository repository) {
        this.repository = repository;
    }

    @Override
    protected MemberRepository getRepository() {
        return repository;
    }

}
