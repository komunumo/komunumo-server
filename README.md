# Komunumo

[![All Tests](https://github.com/komunumo/komunumo-server/actions/workflows/all-tests.yml/badge.svg)](https://github.com/komunumo/komunumo-server/actions/workflows/all-tests.yml)

**Open Source Community Manager**

*Komunumo* is an esperanto noun with a meaning of *community*.

## Architecture

The server of *Komunumo* is written using the [Java programming language](https://en.wikipedia.org/wiki/Java_(programming_language)). The main framework is [Spring](https://spring.io/). For the user interface, we use [Vaadin Flow](https://vaadin.com/flow). To access the database, we rely on [jOOQ](https://www.jooq.org/).

## Configuration

The file `application.properties` contains only some default values. To override the default values and to specify other configuration options, just set them as environment variables. The following sections describe all available configuration options. You only need to specify these options if your configuration settings differ from the defaults.

### Server

The server runs on port 8080 by default. If you don't like it, change it:

```
PORT=8080
```

### Mail

To be able to send mails, you need to specify an SMTP server (defaults are `localhost` and port`25`):

```
MAIL_HOST=localhost
MAIL_PORT=25
```

### Database

*Komunumo* needs a database to store the business data. By default, *Komunumo* comes with [MariaDB](https://mariadb.org/) drivers by default, which is recommended because we are using it during development and it is highly tested with *Komunumo*. All JDBC compatible databases are supported, but you need to configure the JDBC driver dependencies accordingly. Please make sure that your database is using a unicode character set to avoid problems storing data containing unicode characters.

The `DB_USER` is used to access the *Komunumo* database including automatic schema migrations and needs `ALL PRIVILEGES`.

```
DB_URL=jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich&allowMultiQueries=true
DB_USER=johndoe
DB_PASS=verysecret
```

The database schema will be migrated automatically by *Komunumo*.

#### Important MySQL and MariaDB configuration

MySQL and MariaDB have a possible silent truncation problem with the `GROUP_CONCAT` command. To avoid this it is necessary, to configure these two databases to allow multi queries. Just add `allowMultiQueries=true` to the JDBC database URL like in this example (you may need to scroll the example code to the right):

```
DB_URL=jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich&allowMultiQueries=true
```

### Admin

Only members with administrative privileges are allowed to login to the backend. You should add yourself as admin to the database, **after** you have started *Komunumo* (because the database tables will be created at the first start):

```sql
insert into member (id, first_name, last_name, email, registration_date, membership_begin, admin, account_active)
values (1, 'First name', 'Last name', 'email@domain.tld', now(), null, true, true);
```

Then, open `http://localhost:8080/admin`, enter your email address, and click on "I forgot my password" to start the password reset process (you will receive a one time password via email), and set your own admin password.

## Running and debugging the server

### Running the server from the command line.

To run from the command line, use `mvn` and open http://localhost:8080 in your browser.

### Running and debugging the server in Intellij IDEA

- Locate the `Application.java` class in the Project view. It is in the `src` folder, under the main package's root.
- Right-click on the Application class
- Select "Debug 'Application.main()'" from the list

After the server has started, you can view it at http://localhost:8080/ in your browser. 
You can now also attach breakpoints in code for debugging purposes, by clicking next to a line number in any source file.

### Running and debugging the server in Eclipse

- Locate the `Application.java` class in the Package Explorer. It is in `src/main/java`, under the main package.
- Right-click on the file and select `Debug As` --> `Java Application`.

Do not worry if the debugger breaks at a `SilentExitException`. This is a Spring Boot feature and happens on every startup.

After the server has started, you can view it at http://localhost:8080/ in your browser.
You can now also attach breakpoints in code for debugging purposes, by clicking next to a line number in any source file.

## Deploying using Docker

To build the Dockerized version of the project, run

```
docker build . -t komunumo:latest
```

Once the Docker image is correctly built, you can test it locally using

```
docker run -p 8080:8080 komunumo:latest
```

## Development

### Build

We are using [Maven](https://maven.apache.org/) to build the *Komunumo* project. You do not need to have Maven installed! *Komunumo* makes use of the Maven Wrapper. In the root folder of this project, instead of using the `mvn` command directly just call the wrapper script `./mvnw` (or `.\mvnw` on Windows).

### Test

The integration tests need a database. To provide a temporary MariaDB database, the [Testcontainers](https://www.testcontainers.org/) framework is used. This framework needs a working [Docker](https://www.docker.com/) environment.

### Mail

While developing, I highly recommend not to use your real mail server. Instead, I suggest using [FakeSMTP](http://nilhcem.com/FakeSMTP/). It runs locally on your machine and does not deliver any mail to a real mail account. Instead, all mails are kept locally inside the FakeSMTP client. You can be sure not to accidentally send mails.

### Database

#### Reset

While developing, sometimes it is very useful to reset the database manually. You can do this very easily using Maven and Flyway:

```
mvn flyway:clean \
    -D'flyway.user'='johndoe' \
    -D'flyway.password'='verysecret' \
    -D'flyway.url'='jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich'
```

This command will clean your database (erase everything). You need to specify the credentials for a database user with administrative privileges to the database and the database URL.

*Komunumo* will automatically migrate the database schema on the next start.

### Repair

When you modify the database schema during development, Flyway will detect the change and complain. If you update the Flyway SQL scripts and don't want to clean your database, you can manually update the schema in your database, too. Then start a repair using Maven and Flyway:

```
mvn flyway:repair \
    -D'flyway.user'='johndoe' \
    -D'flyway.password'='verysecret' \
    -D'flyway.url'='jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich'
```

This command will update the flyway database history table, so *Komunumo* will start again without complaining about the database schema. The database schema itself will **not** be modified. You need to specify the credentials for a database user with administrative privileges to the database and the database URL.

*Komunumo* will **not** migrate the database schema on the next start.

## Security

There are three levels of security for three areas of the website:

### Public access

Most pages are public. There is no authentication or authorization needed to access public pages. These contain public informations only.

### Self-service

Members can enter a self-service area to change their email address, postal address, newsletter subscription, etc. The member must be authenticated to access this area. The authentication process is very easy: *Komunumo* will send an email to the member with a random code valid for five minutes. Within these five minutes the member must enter this code on the website to get access to the self-service area. In the self-service area the member has access to his own information only and the access is limited to the self-service area itself plus the public pages.

### Administration

The admin area is protected by a traditional login with username and password. Once authenticated, the user has access to all administrative informations and can execute standard CRUD operations (create, read, update, delete).

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Komunumo**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

## Authors

**Komunumo** is led by [Marcus Fihlon](https://github.com/McPringle) and has received contributions from [many individuals](https://github.com/komunumo/komunumo-server/blob/main/CONTRIBUTORS.md) in Komunumo’s awesome community. The project was initiated in 2017 by Marcus Fihlon.
