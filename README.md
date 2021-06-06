# Komunumo

**Open Source Community Manager**

*Komunumo* is an esperanto noun with a meaning of *community*.

## Configuring the server

The file `application.properties` contains only some default values. To override the default values and to specify other configuration options, just set them as environment variables. The following sections describe all available configuration options.

### Server

The server runs on port 8080 by default. If you don't like it, change it:

```
PORT=80
```

### Mail

To be able to send mails, you need to specify an SMTP server:

```
MAIL_HOST=localhost
MAIL_PORT=25
```

### Database

*Komunumo* needs a database to permanently store the business data. All JDBC compatible databases are supported. We highly recommend [MariaDB](https://mariadb.org/), just because we are using it during development and it is highly tested with *Komunumo*.

The `DB_USER` is used during runtime and only needs the privileges `SELECT`, `INSERT`, `UPDATE`, and `DELETE` on the *Komunumo* database. The `DB_ADMIN_USER` is used for database schema migrations only and needs `ALL PRIVILEGES` on the *Komunumo* database.

```
DB_URL=jdbc:mysql://localhost:3306/komunumo?serverTimezone\=Europe/Zurich
DB_USER=johndoe
DB_PASS=verysecret
DB_ADMIN_USER=janedoe
DB_ADMIN_PASS=extremesecret
```

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

### Database

While developing, sometimes it is very useful to reset the database manually. You can this very easily using Maven and Flyway:

```
mvn flyway:clean flyway:migrate flyway:info \
    -Dflyway.user='janedoe' \
    -Dflyway.password='extremesecret' \
    -Dflyway.url='jdbc:mysql://localhost:3306/komunumo?serverTimezone\=Europe/Zurich'
```

This command will first clean your database (erase everything). Then it will execute all needed migration steps to recreate all tables, indexes, etc. Last but not least it will inform you, if the migration was successful or not. You need to specify the credentials for a database user with administrative privileges to the database and the database URL.

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Komunumo**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

## Authors

**Komunumo** is led by [Marcus Fihlon](https://github.com/McPringle) and has received contributions from [many individuals](https://github.com/orgs/komunumo/people) in Komunumo’s awesome community. The project was initiated in 2017 by Marcus Fihlon.
