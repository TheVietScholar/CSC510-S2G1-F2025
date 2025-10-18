# Installation and Local Development Guide

This guide consolidates setup instructions from proj2/tutorial/SETUP.md and adds testing and CI/CD guidance. It covers:

- Running the backend locally with Dockerized MySQL or your own MySQL
- Using the Maven Wrapper (no global Maven needed)
- Running tests locally before opening a PR
- What the CI test flow does

Applies to the Spring Boot service under proj2/.

---

## Prerequisites

- Java 17+ (Temurin recommended)
- Docker Desktop (includes Docker Compose)
- Git
- Optional: MySQL client or Workbench

Note: The project includes the Maven Wrapper, so you don’t need to install Maven. Use ./mvnw (macOS/Linux) or mvnw (
Windows) throughout.

---

## Option A: Use Dockerized MySQL (recommended)

From the proj2/ directory:

```sh
docker compose up -d
```

Services started:

- MySQL (classic protocol) mapped host port 3307 -> container 3306
    - Database: boozebuddies
    - Username/password: app / app
- Adminer at http://localhost:8081 (System: MySQL, Server: db or localhost)

Important ports:

- 3306 = MySQL classic protocol (what JDBC uses)
- 33060 = MySQL X Protocol (not for JDBC)

In this project, host 3307 maps to container 3306 (classic). When connecting from your host to the Docker DB, use port
3307.

### Wait for DB and run with the docker profile

```sh
# from proj2/
./scripts/wait-for-mysql.sh 127.0.0.1 3307 60
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

- App runs at: http://localhost:8080
- Health endpoint: GET /api/health → { "status": "ok" }

Windows notes (PowerShell/CMD):

- Wait: ./scripts/wait-for-mysql.ps1 -Host 127.0.0.1 -Port 3307 -TimeoutSeconds 60
- Run (PowerShell): ./mvnw "-Dspring-boot.run.profiles=docker" spring-boot:run
- Run (CMD.exe): mvnw -Dspring-boot.run.profiles=docker spring-boot:run

---

## Option B: Use your local MySQL (no Docker)

Ensure you’re using the classic protocol port (default 3306). Do not use 33060 unless you explicitly mapped X Protocol
to classic.

Create database and app user (sample):

```sql
-- Connect as root (adjust auth as needed)
CREATE
DATABASE IF NOT EXISTS boozebuddies CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE
USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY 'app';
GRANT ALL PRIVILEGES ON boozebuddies.* TO
'app'@'%';
FLUSH
PRIVILEGES;
```

Run with environment overrides if needed:

```sh
# Example (classic port 3306)
DB_HOST=127.0.0.1 \
DB_PORT=3306 \
DB_NAME=boozebuddies \
DB_USER=app \
DB_PASS=app \
./mvnw spring-boot:run
```

Tip when using the repo’s Docker DB from host: set DB_PORT=3307 (maps to container classic 3306).

---

## Building, formatting, and running

From proj2/:

```sh
# Auto-format (optional but recommended before commits)
./mvnw -q spotless:apply

# Full verification (format check, style, unit/integration tests)
./mvnw -q verify

# Run the app against your current DB settings
./mvnw spring-boot:run
```

On startup, Flyway executes migrations in src/main/resources/db/migration/, starting with V1__baseline_schema.sql.

To run a specific test class or method:

```sh
./mvnw -q -Dtest=YourTestClass test
./mvnw -q -Dtest=YourTestClass#yourTestMethod test
```

---

## Testing locally before opening a PR

Run these commands from proj2/:

```sh
# 1) Format and style checks
./mvnw -q spotless:check
./mvnw -q checkstyle:check

# 2) Unit tests
./mvnw -q test

# Or simply run all of the above via
./mvnw -q verify
```

Notes:

- Unit tests run with Maven Surefire. An in-memory H2 database is available via test configuration in
  src/test/resources/application-test.properties
- If you need to activate a test profile: ./mvnw -Dspring.profiles.active=test test

---

## CI/CD test flow (what the pipeline runs)

While CI configuration files may vary or be added later, the expected pipeline test flow for this project is:

1) Checkout code and set up JDK 17
2) Cache the local Maven repository (optional)
3) Run Maven in batch/CI mode:

```sh
./mvnw -B -q verify
```

This verify phase performs:

- Spotless check (formatting) during verify
- Checkstyle check (style rules) during verify
- Unit tests via Surefire
- Integration tests via Failsafe (if any are defined)

A minimal GitHub Actions job would look like:

```yaml
name: CI
on: [ push, pull_request ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Build & test
        run: ./mvnw -B -q verify
```

If your tests require MySQL in CI, add a service container or spin up Docker before running verify, and pass
DB_HOST/DB_PORT/DB_USER/DB_PASS as needed.

---

## Troubleshooting

- "Unsupported protocol version: 11" — You are likely connecting to the MySQL X Protocol (33060) with a JDBC URL. Use
  the classic protocol (3306), or when using this repo’s Docker DB, connect to host port 3307.
- Ports already in use — Stop other MySQL instances or change mappings in proj2/docker-compose.yml.
- Credentials mismatch — Ensure they match application.properties or your env var overrides.
- First run creates schema — Flyway migrations will create tables automatically on startup.

---

## Reference

- Docker Compose file: proj2/docker-compose.yml
- Wait scripts: proj2/scripts/wait-for-mysql.sh (bash), proj2/scripts/wait-for-mysql.ps1 (PowerShell)
- App configs: proj2/src/main/resources/application.properties and application-docker.properties
- Migrations: proj2/src/main/resources/db/migration/
- Build config: proj2/pom.xml
