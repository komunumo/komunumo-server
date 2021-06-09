CREATE TABLE event (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    title VARCHAR(255) NOT NULL,
    speaker VARCHAR(255) NOT NULL,
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
