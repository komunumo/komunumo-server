# Komunumo

**Open Source Community Manager**

*Komunumo* is an esperanto noun with a meaning of *community*.

## Running and debugging the application

### Running the application from the command line.
To run from the command line, use `mvn` and open http://localhost:8080 in your browser.

### Running and debugging the application in Intellij IDEA
- Locate the Application.java class in the Project view. It is in the src folder, under the main package's root.
- Right-click on the Application class
- Select "Debug 'Application.main()'" from the list

After the application has started, you can view it at http://localhost:8080/ in your browser. 
You can now also attach breakpoints in code for debugging purposes, by clicking next to a line number in any source file.

### Running and debugging the application in Eclipse
- Locate the Application.java class in the Package Explorer. It is in `src/main/java`, under the main package.
- Right-click on the file and select `Debug As` --> `Java Application`.

Do not worry if the debugger breaks at a `SilentExitException`. This is a Spring Boot feature and happens on every startup.

After the application has started, you can view it at http://localhost:8080/ in your browser.
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

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Komunumo**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

## Authors

**Komunumo** is led by [Marcus Fihlon](https://github.com/McPringle) and has received contributions from [many individuals](https://github.com/orgs/komunumo/people) in Komunumo’s awesome community. The project was initiated in 2017 by Marcus Fihlon.
