# Heimdall

## Introduction

A monitoring/testing tool for developers, used to validate or troubleshoot services. The tool provides fake services which are used by applications to mimic a real interaction with a remote service.

However, all services provided by _Heimdall_ will be partially or completely faked. For example, the _SMPT_ server will act like a real SMTP server, but it will not forward the email to real accounts - it will accept an email, store it, make it available for introspection but it will not be forwarded to users.

The following services are faked and available to be used:

* SMTP (Simple Mail Transfer Protocol)
* SNMP (Simple Network Management Protocol)
* Syslog (Standard for Message Logging)
* GELF (Graylog Extended Logging Format)

## Getting Started

## Build & Run

The project requires Java 17 to develop and run and uses Spring Boot 3.X. The latest version can be downloaded from https://adoptium.net/

Once it is installed, check the version from the shell:

```
java --version
```

The output must show a text similar to the one bellow (maybe a newer version is acceptable):

```
openjdk 17.0.6 2023-01-17
OpenJDK Runtime Environment Temurin-17.0.6+10 (build 17.0.6+10)
OpenJDK 64-Bit Server VM Temurin-17.0.6+10 (build 17.0.6+10, mixed mode, sharing)
```

### IDE

Load the project in any IDE which support Apache Maven (Eclipse, IntelliJ, VS Code). There is a main class called
`HeimdallApplication`, just run it, and it will start the Spring Boot. Access the application at http://localhost:8080

### Shell

Apache Maven is used to build the project.

`mvn clean install -DskipTests` can be used to compile the application.

`mvn spring-boot:run` can be used to run the application using Apache Maven

### Tests

`mvn clean test` can be used to compile and run tests only.

## Users

There is a default administrator (called `admin`, with password `9uYI4IE583Eb`) which can be used to access the application over the web.

## Database

This project uses MySQL for the data store. The database needs to be provided as an external dependency. Run the following statements (under `root` user) to create an empty database
and a user for application access (change the database & user if desired):

```sql
CREATE USER 'heimdall'@'%' IDENTIFIED BY '7Ry4j2Y5j1Of'; 
CREATE DATABASE heimdall CHARACTER SET utf8 COLLATE utf8_bin; 
GRANT ALL ON heimdall.* TO 'heimdall'@'%';
GRANT SELECT ON mysql.* TO heimdall;
FLUSH PRIVILEGES; 
```

## Search Engine

This project uses Apache Lucene as a search engine. 

## Configuration

Under `application.properties` the database name or the database user/password can be changed.

## Container

The project is available in [Docker Hub](https://hub.docker.com/repository/docker/adriantarau/heimdall). Heimdall relies on MySQL as a required dependency and an optional 
S3 object store to store data collected from services.

Read [docker](docs/docker.md) document to learn how to deploy Heimdall in Docker.
