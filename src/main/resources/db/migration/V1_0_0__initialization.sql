CREATE TABLE event (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    title VARCHAR(255) NOT NULL,
    date DATETIME NULL,
    visible BOOLEAN NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);

CREATE INDEX event_title ON event (title);

CREATE TABLE member (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255) NULL,
    zip_code VARCHAR(255) NULL,
    city VARCHAR(255) NULL,
    state VARCHAR(255) NULL,
    country VARCHAR(255) NULL,
    member_since DATETIME NOT NULL,
    admin BOOLEAN NOT NULL DEFAULT 0,
    password_salt VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activation_code VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);

CREATE INDEX member_names ON member (last_name, first_name);
CREATE INDEX member_email ON member (email);

CREATE TABLE speaker (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    company VARCHAR(255) NULL,
    bio TEXT NULL,
    photo MEDIUMTEXT NULL,
    email VARCHAR(255) NULL,
    twitter VARCHAR(255) NULL,
    linkedin varchar(255) NULL,
    website VARCHAR(255) NULL,
    address VARCHAR(255) NULL,
    zip_code VARCHAR(255) NULL,
    city VARCHAR(255) NULL,
    state VARCHAR(255) NULL,
    country VARCHAR(255) NULL,

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
    url VARCHAR(255) NOT NULL,
    logo MEDIUMTEXT NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    level ENUM('SILBER', 'GOLD', 'PLATIN') NOT NULL,

    PRIMARY KEY (id)
);
