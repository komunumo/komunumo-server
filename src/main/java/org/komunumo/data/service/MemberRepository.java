package org.komunumo.data.service;

import org.komunumo.data.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Member getByEmail(final String email);

}