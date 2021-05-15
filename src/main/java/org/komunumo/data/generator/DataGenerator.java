package org.komunumo.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;

import org.h2.engine.Role;
import org.komunumo.data.service.EventRepository;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.MemberRepository;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.SponsorRepository;
import org.komunumo.data.entity.Sponsor;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.vaadin.artur.exampledata.DataType;
import org.vaadin.artur.exampledata.ExampleDataGenerator;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(final EventRepository eventRepository, final MemberRepository memberRepository,
                                      final SponsorRepository sponsorRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (eventRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 100 Event entities...");
            ExampleDataGenerator<Event> eventRepositoryGenerator = new ExampleDataGenerator<>(Event.class,
                    LocalDateTime.of(2021, 5, 15, 0, 0, 0));
            eventRepositoryGenerator.setData(Event::setId, DataType.ID);
            eventRepositoryGenerator.setData(Event::setTitle, DataType.TWO_WORDS);
            eventRepositoryGenerator.setData(Event::setSpeaker, DataType.FULL_NAME);
            eventRepositoryGenerator.setData(Event::setDate, DataType.DATETIME_NEXT_1_YEAR);
            eventRepositoryGenerator.setData(Event::setVisible, DataType.BOOLEAN_50_50);
            eventRepository.saveAll(eventRepositoryGenerator.create(100, seed));

            logger.info("... generating 100 Member entities...");
            ExampleDataGenerator<Member> memberRepositoryGenerator = new ExampleDataGenerator<>(Member.class,
                    LocalDateTime.of(2021, 5, 15, 0, 0, 0));
            memberRepositoryGenerator.setData(Member::setId, DataType.ID);
            memberRepositoryGenerator.setData(Member::setFirstName, DataType.FIRST_NAME);
            memberRepositoryGenerator.setData(Member::setLastName, DataType.LAST_NAME);
            memberRepositoryGenerator.setData(Member::setEmail, DataType.EMAIL);
            memberRepositoryGenerator.setData(Member::setAddress, DataType.ADDRESS);
            memberRepositoryGenerator.setData(Member::setZipCode, DataType.ZIP_CODE);
            memberRepositoryGenerator.setData(Member::setCity, DataType.CITY);
            memberRepositoryGenerator.setData(Member::setState, DataType.STATE);
            memberRepositoryGenerator.setData(Member::setCountry, DataType.COUNTRY);
            memberRepositoryGenerator.setData(Member::setMemberSince, DataType.DATE_LAST_10_YEARS);
            memberRepositoryGenerator.setData(Member::setAdmin, DataType.BOOLEAN_50_50);
            memberRepository.saveAll(memberRepositoryGenerator.create(98, seed));

            final var user = memberRepositoryGenerator.create(1, 456).get(0);
            user.setEmail("marcus@fihlon.ch");
            user.setPassword("user");
            user.setAdmin(false);
            memberRepository.save(user);

            final var admin = memberRepositoryGenerator.create(1, 789).get(0);
            admin.setEmail("marcus@fihlon.swiss");
            admin.setPassword("admin");
            admin.setAdmin(true);
            memberRepository.save(admin);

            logger.info("... generating 100 Sponsor entities...");
            ExampleDataGenerator<Sponsor> sponsorRepositoryGenerator = new ExampleDataGenerator<>(Sponsor.class,
                    LocalDateTime.of(2021, 5, 15, 0, 0, 0));
            sponsorRepositoryGenerator.setData(Sponsor::setId, DataType.ID);
            sponsorRepositoryGenerator.setData(Sponsor::setName, DataType.FULL_NAME);
            sponsorRepositoryGenerator.setData(Sponsor::setLogo, DataType.PROFILE_PICTURE_URL);
            sponsorRepositoryGenerator.setData(Sponsor::setValidFrom, DataType.DATE_LAST_1_YEAR);
            sponsorRepositoryGenerator.setData(Sponsor::setValidTo, DataType.DATE_NEXT_1_YEAR);
            sponsorRepository.saveAll(sponsorRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}