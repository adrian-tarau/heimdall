# Heimdall

## Introduction

Heimdall is a tool created for developers to monitor and test services, allowing them to validate and troubleshoot performance issues. It provides simulated services that applications can use to replicate real interactions with remote services. Furthermore, it monitors the infrastructure while conducting load tests on these services.

The following services are faked and available to be used:
* SMTP (Simple Mail Transfer Protocol)
* SNMP (Simple Network Management Protocol)
* Syslog (Standard for Message Logging)
* GELF (Graylog Extended Logging Format)

For testing the performance of services, Heimdall integrates the following load testing tools:
* [Apache JMeter](https://jmeter.apache.org/)
* [Grafana K6](https://k6.io/)
* [Gatling](https://github.com/gatling/gatling)

Heimdall utilizes these tools' reporting capabilities for individual simulations, but it also integrates the results from these simulations to provide trends and reporting capabilities that surpass their individual functionalities.

This project uses Apache Lucene as a search engine. All communication over supported protocols is indexed and can be searched using a full-text search engine, making it easy to find any events or messages passed to Heimdall.

## Getting Started

## Build & Run

The project requires Java 21 to develop and run and uses Spring Boot 3.X. The latest version can be downloaded from https://adoptium.net/

Once it is installed, check the version from the shell:

```
java --version
```

The output must show a text similar to the one bellow (maybe a newer version is acceptable):

```
openjdk version "21.0.5" 2024-10-15 LTS
OpenJDK Runtime Environment Temurin-21.0.5+11 (build 21.0.5+11-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.5+11 (build 21.0.5+11-LTS, mixed mode, sharing)
```

### IDE

Load the project in any IDE that supports Apache Maven (Eclipse, IntelliJ, VS Code). The main class is called
`HeimdallApplication`, just run it, and it will start the Spring Boot. Access the application at http://localhost:8080

### Shell

Apache Maven is used to build the project.

`mvn clean install -DskipTests` can be used to compile the application.

`mvn spring-boot:run` can be used to run the application using Apache Maven

### Tests

`mvn clean test` can be used to compile and run tests only.

## Running

Heimdall uses automatic database migration. During the initial startup, it will provision all required database tables. All default passwords (database user, application administrator, etc) should be changed for production environments.

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
## Users

There is a default administrator (called `admin`, password `WhfAeDkf8857`) which can be used to access the application over the web.

## Configuration

Most important settings can be changed using environment variables. See [application.properties](web/src/main/resources/application.properties) for available environment variables.

## Container

The project is available in [Docker Hub](https://hub.docker.com/repository/docker/adriantarau/heimdall). Heimdall relies on MySQL as a required dependency and an optional S3 object store to store data collected from services.

Read [docker](docs/docker.md) document to learn how to deploy Heimdall with Docker.
