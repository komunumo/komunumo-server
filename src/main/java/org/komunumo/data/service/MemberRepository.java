package org.komunumo.data.service;

import org.komunumo.data.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Member getByEmail(String email);

}