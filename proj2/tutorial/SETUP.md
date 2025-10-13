# BoozeBuddies Setup Guide

This guide helps you run the Spring Boot backend locally with MySQL.

## Prerequisites

- Java 21 (Temurin recommended)
- Docker + Docker Compose
- Git

## Start dependencies (MySQL + Adminer)

From `proj2/` run:

```sh
docker compose up -d
```

Services:

- MySQL at `localhost:3306`, db `boozebuddies`, user `app` / `app`
- Adminer at <http://localhost:8081> (System: MySQL, Server: db or localhost)

## Build & Run (Maven Wrapper)

From `proj2/`:

```sh
./mvnw -q spotless:apply
./mvnw -q verify
./mvnw spring-boot:run
```

App starts on <http://localhost:8080>

- Health endpoint: `GET /api/health` → `{ "status": "ok" }`

## Configuration

Edit `src/main/resources/application.properties` to adjust DB settings.

## Project Quality

- Format: `./mvnw spotless:apply`
- Lint: `./mvnw -q spotless:check && ./mvnw -q checkstyle:check`
- Tests: `./mvnw -q test`

## Troubleshooting

- If DB connection fails, ensure MySQL container is healthy and ports are free.
- Check credentials match properties.
- On first run, schema is created automatically (Hibernate `ddl-auto=update`).
