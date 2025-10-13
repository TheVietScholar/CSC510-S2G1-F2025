# BoozeBuddies Setup Guide

This guide helps you run the Spring Boot backend locally with MySQL.

## Prerequisites

- Java 17+ (Temurin recommended)
- Docker + Docker Compose
- Git

## Start dependencies (MySQL + Adminer) — optional

If you don't have MySQL locally, from `proj2/` run:

```sh
docker compose up -d
```

Services (Docker):

- MySQL (classic protocol) exposed at host `localhost:33060` → mapped to container `3306`
- Database: `boozebuddies`, user `app` / `app`
- Adminer at <http://localhost:8081> (System: MySQL, Server: db or localhost)

Important: MySQL has two ports by default:

- 3306 = classic protocol (JDBC uses this)
- 33060 = MySQL X Protocol (NOT compatible with JDBC)

Our docker-compose maps host 33060 to the container's classic 3306. So you can connect via JDBC to `localhost:33060` safely when using Docker. If you're using a system-installed MySQL, use port 3306 unless you specifically reconfigured classic to listen on a different port.

## Build & Run (Maven Wrapper)

From `proj2/`:

```sh
./mvnw -q spotless:apply
./mvnw -q verify
./mvnw spring-boot:run
```

App starts on <http://localhost:8080>

- Health endpoint: `GET /api/health` → `{ "status": "ok" }`

### Run against Docker DB

If using Docker to provide MySQL, wait for the DB to be reachable and use the `docker` Spring profile:

```sh
./scripts/wait-for-mysql.sh 127.0.0.1 33060 60
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

## Using your local MySQL (no Docker)

If you use a system-installed MySQL, JDBC expects the classic protocol port (3306 by default). Port 33060 is the X Protocol and won't work with JDBC unless you've explicitly mapped it to classic.

Create the database and a user (or reuse your own):

```sh
# Connect as root (adjust if your root auth differs)
mysql -h 127.0.0.1 -P 3306 -u root -p

-- Inside MySQL shell:
CREATE DATABASE IF NOT EXISTS boozebuddies CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- Option A: create app/app user
CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY 'app';
GRANT ALL PRIVILEGES ON boozebuddies.* TO 'app'@'%';
FLUSH PRIVILEGES;
EXIT;
```

If you prefer your own credentials or different host/port, override at runtime using environment variables:

```sh
DB_HOST=127.0.0.1 \
DB_PORT=3306 \
DB_NAME=boozebuddies \
DB_USER=youruser \
DB_PASS=yourpass \
./mvnw spring-boot:run

Using Docker? Since docker-compose maps host 33060 → container 3306 (classic), set `DB_PORT=33060` when connecting to the container from your host.
```

On startup, Flyway runs migration `V1__baseline_schema.sql` to create initial tables (`users`, `merchants`, `products`, `orders`, `order_items`, `drivers`, `deliveries`, `payments`, etc.).

## Configuration

Edit `src/main/resources/application.properties` to adjust DB settings. Defaults use classic 3306 and can be overridden via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`.

## Project Quality

- Format: `./mvnw spotless:apply`
- Lint: `./mvnw -q spotless:check && ./mvnw -q checkstyle:check`
- Tests: `./mvnw -q test`

## Troubleshooting

- If you see "Unsupported protocol version: 11", you're likely connecting to the MySQL X Protocol (33060) with a JDBC URL. Switch to the classic protocol port (3306) or, when using Docker in this repo, set `DB_PORT=33060` which maps to container 3306.
- Ensure the container is healthy and ports are free.
- Check credentials match properties.
- On first run, schema is created automatically by Flyway (`db/migration` scripts).
