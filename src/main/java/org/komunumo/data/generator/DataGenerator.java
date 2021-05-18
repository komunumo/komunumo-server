/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.komunumo.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Sponsor;
import org.komunumo.data.entity.Sponsor.Level;
import org.komunumo.data.service.EventRepository;
import org.komunumo.data.service.MemberRepository;
import org.komunumo.data.service.SponsorRepository;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(final EventRepository eventRepository,
                                      final MemberRepository memberRepository,
                                      final SponsorRepository sponsorRepository) {
        return args -> {
            final var logger = LoggerFactory.getLogger(getClass());

            if (eventRepository.count() == 0L) {
                logger.info("Generating event entities...");

                final var event1 = new Event();
                event1.setId(1);
                event1.setTitle("Testevent One");
                event1.setSpeaker("John Doe");
                event1.setDate(LocalDateTime.of(2021, 10, 1, 18, 0, 0));
                event1.setVisible(true);
                eventRepository.save(event1);

                final var event2 = new Event();
                event2.setId(2);
                event2.setTitle("Testevent Two");
                event2.setSpeaker("Jane Doe");
                event2.setDate(LocalDateTime.of(2021, 11, 1, 18, 0, 0));
                event2.setVisible(true);
                eventRepository.save(event2);

                final var event3 = new Event();
                event3.setId(3);
                event3.setTitle("Testevent Three");
                event3.setSpeaker("Jill Doe");
                event3.setDate(LocalDateTime.of(2021, 12, 1, 18, 0, 0));
                event3.setVisible(false);
                eventRepository.save(event3);
            }

            if (memberRepository.count() == 0) {
                logger.info("Generating member entities...");

                final var member1 = new Member();
                member1.setId(1);
                member1.setFirstName("Marcus");
                member1.setLastName("Fihlon");
                member1.setEmail("marcus@fihlon.ch");
                member1.setAddress("Winkelriedstrasse 25");
                member1.setZipCode("6003");
                member1.setCity("Luzern");
                member1.setState("Luzern");
                member1.setCountry("Schweiz");
                member1.setMemberSince(LocalDate.of(2013, 2, 1));
                member1.setAdmin(false);
                member1.setPassword("user");
                member1.setActive(true);
                memberRepository.save(member1);

                final var member2 = new Member();
                member2.setId(2);
                member2.setFirstName("Marcus");
                member2.setLastName("Fihlon");
                member2.setEmail("marcus@fihlon.swiss");
                member2.setAddress("Winkelriedstrasse 25");
                member2.setZipCode("6003");
                member2.setCity("Luzern");
                member2.setState("Luzern");
                member2.setCountry("Schweiz");
                member2.setMemberSince(LocalDate.of(2013, 2, 1));
                member2.setAdmin(true);
                member2.setPassword("admin");
                member2.setActive(true);
                memberRepository.save(member2);
            }

            if (sponsorRepository.count() == 0) {
                logger.info("Generating sponsor entities...");

                final var sponsor1 = new Sponsor();
                sponsor1.setId(1);
                sponsor1.setName("mimacom ag");
                sponsor1.setUrl("https://www.mimacom.com/");
                sponsor1.setLogo("https://www.jug.ch/images/sponsors/mimacom_platin.jpg");
                sponsor1.setValidFrom(LocalDate.of(2000, 1, 1));
                sponsor1.setValidTo(LocalDate.of(2099, 12, 31));
                sponsor1.setLevel(Level.PLATIN);
                sponsorRepository.save(sponsor1);

                final var sponsor2 = new Sponsor();
                sponsor2.setId(2);
                sponsor2.setName("Netcetera");
                sponsor2.setUrl("https://www.netcetera.com/");
                sponsor2.setLogo("https://www.jug.ch/images/sponsors/netcetera.gif");
                sponsor2.setValidFrom(LocalDate.of(2000, 1, 1));
                sponsor2.setValidTo(LocalDate.of(2099, 12, 31));
                sponsor2.setLevel(Level.GOLD);
                sponsorRepository.save(sponsor2);

                final var sponsor3 = new Sponsor();
                sponsor3.setId(3);
                sponsor3.setName("CSS Versicherung");
                sponsor3.setUrl("https://www.css.ch/");
                sponsor3.setLogo("https://www.jug.ch/images/sponsors/CSS.png");
                sponsor3.setValidFrom(LocalDate.of(2000, 1, 1));
                sponsor3.setValidTo(LocalDate.of(2099, 12, 31));
                sponsor3.setLevel(Level.SILBER);
                sponsorRepository.save(sponsor3);
            }

            logger.info("Demo data ready.");
        };
    }

}