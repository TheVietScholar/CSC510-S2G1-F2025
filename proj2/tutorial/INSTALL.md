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

- MySQL (classic protocol) exposed at host `localhost:3307` → mapped to container `3306`
- Database: `boozebuddies`, user `app` / `app`
- Adminer at <http://localhost:8081> (System: MySQL, Server: db or localhost)

Important: MySQL has two ports by default:

- 3306 = classic protocol (JDBC uses this)
- 33060 = MySQL X Protocol (NOT compatible with JDBC)

Our docker-compose maps host 3307 to the container's classic 3306. So you can connect via JDBC to `localhost:3307` safely when using Docker. If you're using a system-installed MySQL, use port 3306 unless you specifically reconfigured classic to listen on a different port.

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
    docker compose up -d
    until docker exec boozebuddies-mysql mysqladmin ping \
      -h 127.0.0.1 -P 3306 -u app -papp --silent; do
      echo "waiting for MySQL service..." && sleep 2
    done
    SPRING_PROFILES_ACTIVE=docker ./mvnw spring-boot:run
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

Using Docker? Since docker-compose maps host 3307 → container 3306 (classic), set `DB_PORT=3307` when connecting to the container from your host.
```

On startup, Flyway runs migration `V1__baseline_schema.sql` to create initial tables (`users`, `merchants`, `products`, `orders`, `order_items`, `drivers`, `deliveries`, `payments`, etc.).

## Configuration

Edit `src/main/resources/application.properties` to adjust DB settings. Defaults use classic 3306 and can be overridden via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`.

## Project Quality

- Format: `./mvnw spotless:apply`
- Lint: `./mvnw -q spotless:check && ./mvnw -q checkstyle:check`
- Tests: `./mvnw -q test`

## Troubleshooting

- If you see "Unsupported protocol version: 11", you're likely connecting to the MySQL X Protocol (33060) with a JDBC URL. Switch to the classic protocol port (3306) or, when using Docker in this repo, set `DB_PORT=3307` which maps to container 3306.
- Ensure the container is healthy and ports are free.
- Check credentials match properties.
- On first run, schema is created automatically by Flyway (`db/migration` scripts).

## Windows setup notes (PowerShell)

PowerShell parses arguments differently than bash. Use the following patterns:

- Start Docker dependencies:
	- `docker compose up -d`

- Wait for MySQL (PowerShell):
	- `./scripts/wait-for-mysql.ps1 -Host 127.0.0.1 -Port 3307 -TimeoutSeconds 60`

- Run Spring Boot with the docker profile:
	- PowerShell: `./mvnw "-Dspring-boot.run.profiles=docker" spring-boot:run`
	- CMD.exe: `mvnw -Dspring-boot.run.profiles=docker spring-boot:run`

- Using environment overrides in PowerShell:
	- `$env:DB_HOST = "127.0.0.1"; $env:DB_PORT = "3306"; $env:DB_NAME = "boozebuddies"; $env:DB_USER = "app"; $env:DB_PASS = "app"; ./mvnw spring-boot:run`

Tip: If you see `unknown life cycle .run.profiles=docker`, quote the property as shown above so PowerShell doesn’t split the `-D` argument.


## Frontend Setup (Vite + React)

The frontend connects to the Spring Boot backend running on http://localhost:8080
.

Prerequisites

Node.js 18+ (LTS recommended)

npm (comes with Node)

Install dependencies

From the frontend/ directory:

npm install


This installs all required Node modules.

Start the development server
npm run dev


The app runs by default at http://localhost:5173
.
When both frontend and backend are running, API requests are automatically proxied to http://localhost:8080
.

Troubleshooting

Port already in use (5173):
If something else is using port 5173, start the dev server on a different port:

npm run dev -- --port 5174


Backend connection errors:
Ensure the Spring Boot backend is running on http://localhost:8080
.
If you changed the backend port, update the frontend proxy setting in vite.config.js.

Environment variables:
If your project uses a .env file (for API URLs or keys), copy the example file before starting:

cp .env.example .env


Then edit .env to match your local setup.