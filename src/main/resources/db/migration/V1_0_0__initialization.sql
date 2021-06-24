CREATE TABLE event (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255) NOT NULL DEFAULT '',
    abstract MEDIUMTEXT NOT NULL DEFAULT '',
    agenda MEDIUMTEXT NOT NULL DEFAULT '',
    level ENUM('Beginner', 'Intermediate', 'Advanced') NULL,
    language ENUM('DE', 'EN') NULL,
    location ENUM('Online', 'Basel', 'Bern', 'Luzern', 'St. Gallen', 'Zürich') NULL,
    date DATETIME NULL,
    visible BOOLEAN NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);

CREATE INDEX event_title ON event (title);
CREATE INDEX event_date ON event (date);

CREATE TABLE member (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL DEFAULT '',
    zip_code VARCHAR(255) NOT NULL DEFAULT '',
    city VARCHAR(255) NOT NULL DEFAULT '',
    state VARCHAR(255) NOT NULL DEFAULT '',
    country VARCHAR(255) NOT NULL DEFAULT '',
    member_since DATETIME NOT NULL,
    admin BOOLEAN NOT NULL DEFAULT 0,
    password_salt VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    password_change BOOLEAN NOT NULL DEFAULT 0,
    activation_code VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT 0,
    blocked BOOLEAN NOT NULL DEFAULT 0,
    blocked_reason VARCHAR(255) NOT NULL DEFAULT '',

    PRIMARY KEY (id)
);

CREATE INDEX member_names ON member (last_name, first_name);
CREATE INDEX member_email ON member (email);

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
);

CREATE INDEX speaker_names ON speaker (last_name, first_name);

CREATE TABLE event_speaker (
   event_id INTEGER UNSIGNED NOT NULL,
   speaker_id INTEGER UNSIGNED NOT NULL,

   PRIMARY KEY (event_id, speaker_id),
   CONSTRAINT fk_event_id FOREIGN KEY (event_id) REFERENCES event(id),
   CONSTRAINT fk_speaker_id FOREIGN KEY (speaker_id) REFERENCES speaker(id)
);

CREATE TABLE sponsor (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    name VARCHAR(255) NOT NULL,
    website VARCHAR(255) NOT NULL DEFAULT '',
    logo MEDIUMTEXT NOT NULL DEFAULT '',
    valid_from DATE NULL,
    valid_to DATE NULL,
    level ENUM('SILVER', 'GOLD', 'PLATINUM') NULL,

    PRIMARY KEY (id)
);
