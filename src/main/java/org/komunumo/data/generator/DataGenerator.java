package org.komunumo.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.komunumo.data.entity.Event;
import org.komunumo.data.entity.Member;
import org.komunumo.data.entity.Sponsor;
import org.komunumo.data.service.EventRepository;
import org.komunumo.data.service.MemberRepository;
import org.komunumo.data.service.SponsorRepository;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.vaadin.artur.exampledata.DataType;
import org.vaadin.artur.exampledata.ExampleDataGenerator;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(final EventRepository eventRepository,
                                      final MemberRepository memberRepository,
                                      final SponsorRepository sponsorRepository) {
        return args -> {
            final var logger = LoggerFactory.getLogger(getClass());
            final int seed = 123;

            if (eventRepository.count() == 0L) {
                logger.info("Generating event entities...");
                final var eventRepositoryGenerator = new ExampleDataGenerator<>(Event.class,
                        LocalDateTime.of(2021, 5, 15, 0, 0, 0));
                eventRepositoryGenerator.setData(Event::setId, DataType.ID);
                eventRepositoryGenerator.setData(Event::setTitle, DataType.TWO_WORDS);
                eventRepositoryGenerator.setData(Event::setSpeaker, DataType.FULL_NAME);
                eventRepositoryGenerator.setData(Event::setDate, DataType.DATETIME_NEXT_1_YEAR);
                eventRepositoryGenerator.setData(Event::setVisible, DataType.BOOLEAN_50_50);
                eventRepository.saveAll(eventRepositoryGenerator.create(100, seed));
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
                final var sponsorRepositoryGenerator = new ExampleDataGenerator<>(Sponsor.class,
                        LocalDateTime.of(2021, 5, 15, 0, 0, 0));
                sponsorRepositoryGenerator.setData(Sponsor::setId, DataType.ID);
                sponsorRepositoryGenerator.setData(Sponsor::setName, DataType.FULL_NAME);
                sponsorRepositoryGenerator.setData(Sponsor::setLogo, DataType.PROFILE_PICTURE_URL);
                sponsorRepositoryGenerator.setData(Sponsor::setValidFrom, DataType.DATE_LAST_1_YEAR);
                sponsorRepositoryGenerator.setData(Sponsor::setValidTo, DataType.DATE_NEXT_1_YEAR);
                sponsorRepository.saveAll(sponsorRepositoryGenerator.create(100, seed));
            }

            logger.info("Demo data ready.");
        };
    }

}