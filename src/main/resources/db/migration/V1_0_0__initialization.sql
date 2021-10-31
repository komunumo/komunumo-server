CREATE TABLE event (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    type ENUM('Talk', 'Workshop', 'Meetup', 'Sponsored') NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255) NOT NULL DEFAULT '',
    description MEDIUMTEXT NOT NULL DEFAULT '',
    agenda MEDIUMTEXT NOT NULL DEFAULT '',
    level ENUM('All', 'Beginner', 'Intermediate', 'Advanced') NULL,
    language ENUM('DE', 'EN') NULL,
    location VARCHAR(255) NULL DEFAULT '',
    webinar_url VARCHAR(255) NOT NULL DEFAULT '',
    date DATETIME NULL,
    duration TIME NULL,
    published BOOLEAN NOT NULL DEFAULT 0,
    event_url VARCHAR(255) NOT NULL DEFAULT '',

    PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;

CREATE INDEX event_date ON event (date);

CREATE TABLE member (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL DEFAULT '',
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL DEFAULT '',
    zip_code VARCHAR(255) NOT NULL DEFAULT '',
    city VARCHAR(255) NOT NULL DEFAULT '',
    state VARCHAR(255) NOT NULL DEFAULT '',
    country VARCHAR(255) NOT NULL DEFAULT '',
    registration_date DATETIME NOT NULL,
    membership_begin DATE NULL,
    membership_end DATE NULL,
    membership_id INTEGER UNSIGNED NOT NULL DEFAULT 0,
    admin BOOLEAN NOT NULL DEFAULT 0,
    password_salt VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    password_change BOOLEAN NOT NULL DEFAULT 0,
    activation_code VARCHAR(255) NULL,
    account_active BOOLEAN NOT NULL DEFAULT 0,
    account_blocked BOOLEAN NOT NULL DEFAULT 0,
    account_blocked_reason VARCHAR(255) NOT NULL DEFAULT '',
    account_deleted BOOLEAN NOT NULL DEFAULT 0,
    comment MEDIUMTEXT NOT NULL DEFAULT '',

    PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;

CREATE INDEX member_names ON member (last_name, first_name);
CREATE INDEX member_email ON member (email);

CREATE TABLE event_member (
    event_id INTEGER UNSIGNED NOT NULL,
    member_id INTEGER UNSIGNED NOT NULL,
    date DATETIME NULL,
    no_show BOOLEAN NOT NULL DEFAULT 0,

    PRIMARY KEY (event_id, member_id),
    CONSTRAINT FOREIGN KEY (event_id) REFERENCES event(id),
    CONSTRAINT FOREIGN KEY (member_id) REFERENCES member(id)
) DEFAULT CHARSET=utf8;

CREATE TABLE event_organizer (
    event_id INTEGER UNSIGNED NOT NULL,
    member_id INTEGER UNSIGNED NOT NULL,

    PRIMARY KEY (event_id, member_id),
    CONSTRAINT FOREIGN KEY (event_id) REFERENCES event(id),
    CONSTRAINT FOREIGN KEY (member_id) REFERENCES member(id)
) DEFAULT CHARSET=utf8;

CREATE TABLE keyword (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    keyword VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX (keyword)
) DEFAULT CHARSET=utf8;

CREATE TABLE event_keyword (
    event_id INTEGER UNSIGNED NOT NULL,
    keyword_id INTEGER UNSIGNED NOT NULL,

    PRIMARY KEY (event_id, keyword_id),
    CONSTRAINT FOREIGN KEY (event_id) REFERENCES event(id),
    CONSTRAINT FOREIGN KEY (keyword_id) REFERENCES keyword(id)
) DEFAULT CHARSET=utf8;

CREATE TABLE speaker (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL DEFAULT '',
    bio MEDIUMTEXT NOT NULL DEFAULT '',
    photo MEDIUMTEXT NOT NULL DEFAULT '',
    email VARCHAR(255) NOT NULL DEFAULT '',
    twitter VARCHAR(255) NOT NULL DEFAULT '',
    linkedin varchar(255) NOT NULL DEFAULT '',
    website VARCHAR(255) NOT NULL DEFAULT '',
    address VARCHAR(255) NOT NULL DEFAULT '',
    zip_code VARCHAR(255) NOT NULL DEFAULT '',
    city VARCHAR(255) NOT NULL DEFAULT '',
    state VARCHAR(255) NOT NULL DEFAULT '',
    country VARCHAR(255) NOT NULL DEFAULT '',

    PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;

CREATE INDEX speaker_names ON speaker (last_name, first_name);

CREATE TABLE event_speaker (
   event_id INTEGER UNSIGNED NOT NULL,
   speaker_id INTEGER UNSIGNED NOT NULL,

   PRIMARY KEY (event_id, speaker_id),
   CONSTRAINT FOREIGN KEY (event_id) REFERENCES event(id),
   CONSTRAINT FOREIGN KEY (speaker_id) REFERENCES speaker(id)
) DEFAULT CHARSET=utf8;

CREATE TABLE sponsor (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    name VARCHAR(255) NOT NULL,
    website VARCHAR(255) NOT NULL DEFAULT '',
    logo MEDIUMTEXT NOT NULL DEFAULT '',
    valid_from DATE NULL,
    valid_to DATE NULL,
    level ENUM('Silver', 'Gold', 'Platinum') NULL,

    PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;
