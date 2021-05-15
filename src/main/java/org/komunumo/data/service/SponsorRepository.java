package org.komunumo.data.service;

import org.komunumo.data.entity.Sponsor;

import org.springframework.data.jpa.repository.JpaRepository;
import javax.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDate;

public interface SponsorRepository extends JpaRepository<Sponsor, Integer> {

}