package org.komunumo.data.service;

import org.komunumo.data.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class MemberService extends CrudService<Member, Integer> {

    private final MemberRepository repository;

    public MemberService(@Autowired final MemberRepository repository) {
        this.repository = repository;
    }

    @Override
    protected MemberRepository getRepository() {
        return repository;
    }

}
