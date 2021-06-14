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
    url VARCHAR(255) NOT NULL,
    logo MEDIUMTEXT NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    level ENUM('SILBER', 'GOLD', 'PLATIN') NOT NULL,

    PRIMARY KEY (id)
);



-- Example Data

INSERT INTO speaker (first_name, last_name, email) VALUES
        ("John", "Doe", "john.doe@komunumo.org"),
        ("Jane", "Doe", "jane.doe@komunumo.org");

INSERT INTO event (title, date, visible) VALUES
        ("Event One", "2021-10-01 18:00:00", TRUE),
        ("Event Two", "2021-11-01 18:00:00", TRUE),
        ("Event Three", "2021-12-01 18:00:00", FALSE);

INSERT INTO event_speaker (event_id, speaker_id) VALUES
        (1, 1), (2, 2), (3, 1), (3, 2);

INSERT INTO member (first_name, last_name, email, member_since, admin, password_salt, password_hash, active) VALUES
        ("Marcus", "Fihlon", "marcus@fihlon.swiss", "2021-01-01", TRUE, "w9fT9}qBjzNnB75$ClFQ7Ggl5{mYvd,?", "f6c866154ed2279bc446774b48d7ce7e3a3c668d", TRUE),
        ("Marcus", "Fihlon", "marcus@fihlon.ch", "2021-02-01", FALSE, "<\"wCc+GKwQ~C:**#hd;^muH<h'zo#+mk", "c924b6fe42d3ed371c4d7d5145836f0ead10608a", TRUE),
        ("John", "Doe", "john.doe@komunumo.org", "2021-03-01", FALSE, NULL, NULL, FALSE),
        ("Jane", "Doe", "jane.doe@komunumo.org", "2021-04-01", FALSE, NULL, NULL, FALSE);

INSERT INTO sponsor (name, url, logo, valid_from, valid_to, level) VALUES
        ("mimacom ag", "https://www.mimacom.com/", "https://www.jug.ch/images/sponsors/mimacom_platin.jpg", "2000-01-01", "2099-12-31", "PLATIN"),
        ("Netcetera", "https://www.netcetera.com/", "https://www.jug.ch/images/sponsors/netcetera.gif", "2000-01-01", "2099-12-31", "GOLD"),
        ("CSS Versicherung", "https://www.css.ch/", "https://www.jug.ch/images/sponsors/CSS.png", "2000-01-01", "2099-12-31", "SILBER");
