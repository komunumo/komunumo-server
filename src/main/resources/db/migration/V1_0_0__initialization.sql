CREATE TABLE configuration (
    `key` VARCHAR(255) NOT NULL,
    `value` MEDIUMTEXT NOT NULL DEFAULT '',

    PRIMARY KEY (`key`)
);

CREATE TABLE event (
    `id` INTEGER NOT NULL AUTO_INCREMENT,

    `type` ENUM('Talk', 'Workshop', 'Meetup', 'Sponsored') NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `subtitle` VARCHAR(255) NOT NULL DEFAULT '',
    `description` MEDIUMTEXT NOT NULL DEFAULT '',
    `agenda` MEDIUMTEXT NOT NULL DEFAULT '',
    `level` ENUM('All', 'Beginner', 'Intermediate', 'Advanced') NULL,
    `language` ENUM('DE', 'EN') NULL,
    `location` VARCHAR(255) NULL DEFAULT '',
    `room` varchar(255) NOT NULL DEFAULT '',
    `travel_instructions` varchar(255) NOT NULL DEFAULT '',
    `webinar_url` VARCHAR(255) NOT NULL DEFAULT '',
    `youtube` VARCHAR(255) NOT NULL DEFAULT '',
    `date` DATETIME NULL,
    `duration` TIME NULL,
    `attendee_limit` SMALLINT NOT NULL DEFAULT 0,
    `members_only` BOOLEAN NOT NULL DEFAULT 0,
    `published` BOOLEAN NOT NULL DEFAULT 0,
    `event_url` VARCHAR(255) NOT NULL DEFAULT '',

    PRIMARY KEY (`id`)
);

CREATE INDEX `event_date` ON event (`date`);

CREATE TABLE redirect (
    `old_url` VARCHAR(255) NOT NULL,
    `new_url` VARCHAR(255) NOT NULL,

    PRIMARY KEY (`old_url`)
);

CREATE TABLE location_color (
    `location` VARCHAR(255) NOT NULL,
    `color` VARCHAR(7) NOT NULL,

    PRIMARY KEY (`location`)
);

CREATE TABLE member (
    `id` INTEGER NOT NULL AUTO_INCREMENT,

    `first_name` VARCHAR(255) NOT NULL,
    `last_name` VARCHAR(255) NOT NULL,
    `company` VARCHAR(255) NOT NULL DEFAULT '',
    `email` VARCHAR(255) NOT NULL,
    `address` VARCHAR(255) NOT NULL DEFAULT '',
    `zip_code` VARCHAR(255) NOT NULL DEFAULT '',
    `city` VARCHAR(255) NOT NULL DEFAULT '',
    `state` VARCHAR(255) NOT NULL DEFAULT '',
    `country` VARCHAR(255) NOT NULL DEFAULT '',
    `registration_date` DATETIME NOT NULL,
    `membership_begin` DATE NULL,
    `membership_end` DATE NULL,
    `membership_id` INTEGER NOT NULL DEFAULT 0,
    `theme` ENUM('dark', 'light') NOT NULL DEFAULT 'light',
    `admin` BOOLEAN NOT NULL DEFAULT 0,
    `password_hash` VARCHAR(255) NULL,
    `password_change` BOOLEAN NOT NULL DEFAULT 0,
    `activation_code` VARCHAR(255) NULL,
    `account_active` BOOLEAN NOT NULL DEFAULT 0,
    `account_blocked` BOOLEAN NOT NULL DEFAULT 0,
    `account_blocked_reason` VARCHAR(255) NOT NULL DEFAULT '',
    `account_deleted` BOOLEAN NOT NULL DEFAULT 0,
    `comment` MEDIUMTEXT NOT NULL DEFAULT '',

    PRIMARY KEY (`id`)
);

CREATE INDEX `member_names` ON member (`first_name`, `last_name`);
CREATE INDEX `member_email` ON member (`email`);

CREATE TABLE registration (
    `event_id` INTEGER NOT NULL,
    `member_id` INTEGER NOT NULL,
    `date` DATETIME NULL,
    `source` VARCHAR(255) NOT NULL DEFAULT '',
    `deregister` VARCHAR(255) NOT NULL DEFAULT '',
    `no_show` BOOLEAN NOT NULL DEFAULT 0,

    PRIMARY KEY (`event_id`, `member_id`),
    FOREIGN KEY (`event_id`) REFERENCES event(`id`),
    FOREIGN KEY (`member_id`) REFERENCES member(`id`)
);

CREATE TABLE event_organizer (
    `event_id` INTEGER NOT NULL,
    `member_id` INTEGER NOT NULL,

    PRIMARY KEY (`event_id`, `member_id`),
    FOREIGN KEY (`event_id`) REFERENCES event(`id`),
    FOREIGN KEY (`member_id`) REFERENCES member(`id`)
);

CREATE TABLE keyword (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `keyword` VARCHAR(255) NOT NULL,

    PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX `unqiue_keyword` ON keyword(`keyword`);

CREATE TABLE event_keyword (
    `event_id` INTEGER NOT NULL,
    `keyword_id` INTEGER NOT NULL,

    PRIMARY KEY (`event_id`, `keyword_id`),
    FOREIGN KEY (`event_id`) REFERENCES event(`id`),
    FOREIGN KEY (`keyword_id`) REFERENCES keyword(`id`)
);

CREATE TABLE speaker (
    `id` INTEGER NOT NULL AUTO_INCREMENT,

    `first_name` VARCHAR(255) NOT NULL,
    `last_name` VARCHAR(255) NOT NULL,
    `company` VARCHAR(255) NOT NULL DEFAULT '',
    `bio` MEDIUMTEXT NOT NULL DEFAULT '',
    `photo` MEDIUMTEXT NOT NULL DEFAULT '',
    `email` VARCHAR(255) NOT NULL DEFAULT '',
    `twitter` VARCHAR(255) NOT NULL DEFAULT '',
    `linkedin` varchar(255) NOT NULL DEFAULT '',
    `website` VARCHAR(255) NOT NULL DEFAULT '',
    `address` VARCHAR(255) NOT NULL DEFAULT '',
    `zip_code` VARCHAR(255) NOT NULL DEFAULT '',
    `city` VARCHAR(255) NOT NULL DEFAULT '',
    `state` VARCHAR(255) NOT NULL DEFAULT '',
    `country` VARCHAR(255) NOT NULL DEFAULT '',

    PRIMARY KEY (`id`)
);

CREATE INDEX `speaker_names` ON speaker (`first_name`, `last_name`);

CREATE TABLE event_speaker (
    `event_id` INTEGER NOT NULL,
    `speaker_id` INTEGER NOT NULL,

    PRIMARY KEY (`event_id`, `speaker_id`),
    FOREIGN KEY (`event_id`) REFERENCES event(`id`),
    FOREIGN KEY (`speaker_id`) REFERENCES speaker(`id`)
);

CREATE TABLE sponsor (
    `id` INTEGER NOT NULL AUTO_INCREMENT,

    `name` VARCHAR(255) NOT NULL,
    `website` VARCHAR(255) NOT NULL DEFAULT '',
    `logo` MEDIUMTEXT NOT NULL DEFAULT '',
    `description` MEDIUMTEXT NOT NULL DEFAULT '',
    `valid_from` DATE NULL,
    `valid_to` DATE NULL,
    `level` ENUM('Silver', 'Gold', 'Platinum') NULL,

    PRIMARY KEY (`id`)
);

CREATE TABLE sponsor_domain (
    `sponsor_id` INTEGER NOT NULL,
    `domain` VARCHAR(255) NOT NULL DEFAULT '',

    PRIMARY KEY (`sponsor_id`, `domain`),
    FOREIGN KEY (`sponsor_id`) REFERENCES sponsor(`id`)
);

CREATE TABLE news (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `created` DATETIME NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `subtitle` VARCHAR(255) NOT NULL DEFAULT '',
    `teaser` MEDIUMTEXT NOT NULL,
    `message` MEDIUMTEXT NOT NULL,
    `show_from` DATETIME NULL,
    `show_to` DATETIME NULL,

    PRIMARY KEY (`id`)
);

CREATE TABLE subscription (
    `email` VARCHAR(255) NOT NULL,
    `subscription_date` DATETIME NOT NULL,
    `status` ENUM('PENDING', 'ACTIVE') NOT NULL DEFAULT 'PENDING',
    `validation_code` VARCHAR(255) NULL,

    PRIMARY KEY (`email`)
);

CREATE TABLE faq (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `question` VARCHAR(255) NOT NULL,
    `answer` MEDIUMTEXT NOT NULL,

    PRIMARY KEY (`id`)
);

CREATE TABLE page (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `parent` ENUM('Members', 'Sponsors') NOT NULL,
    `page_url` VARCHAR(255) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `content` LONGTEXT NOT NULL,

    PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX `page_url` ON page(`page_url`);

CREATE TABLE mail_template (
    `id` VARCHAR(255) NOT NULL,
    `subject` VARCHAR(255) NOT NULL,
    `content_text` LONGTEXT NOT NULL,
    `content_html` LONGTEXT NOT NULL,

    PRIMARY KEY (`id`)
);

CREATE TABLE feedback (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `received` DATETIME NOT NULL,
    `first_name` VARCHAR(255) NOT NULL,
    `last_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `feedback` MEDIUMTEXT NOT NULL,

    PRIMARY KEY (`id`)
);
