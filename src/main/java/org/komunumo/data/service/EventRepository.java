package org.komunumo.data.service;

import org.komunumo.data.entity.Event;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface EventRepository extends JpaRepository<Event, Integer> {

}