# CSC510-S2G1-F2025

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Style: Checkstyle](https://img.shields.io/badge/style-Checkstyle-black)](proj2/config/checkstyle/checkstyle.xml)
[![Formatter: Spotless](https://img.shields.io/badge/formatter-Spotless-blue)](proj2/pom.xml)
[![Syntax: PMD](https://img.shields.io/badge/syntax-PMD-darkgreen)](proj2/config/pmd/ruleset.xml)
[![Coverage: JaCoCo](https://img.shields.io/badge/coverage-JaCoCo-lightgrey)](proj2/target/site/jacoco/index.html)
[![Build: Maven verify](https://img.shields.io/badge/build-maven%20verify-orange)](proj2/pom.xml)
[![Java 17](https://img.shields.io/badge/java-17-007396)](https://adoptium.net/)

Welcome! This repository contains course artifacts and the BoozeBuddies Spring Boot service under proj2/.

See installation and environment setup details in [INSTALL.md](INSTALL.md).

## Quick start (summary)

- Clone the repo: git clone https://github.com/TheVietScholar/CSC510-S2G1-F2025.git
- Start MySQL via Docker (recommended):
    - cd proj2 && docker compose up -d
    - Adminer at http://localhost:8081
    - MySQL available on host 127.0.0.1:3307 (maps to container 3306)
- Run the app with the docker profile:
    - ./scripts/wait-for-mysql.sh 127.0.0.1 3307 60
    - ./mvnw -Dspring-boot.run.profiles=docker spring-boot:run
- Health check: GET http://localhost:8080/api/health → { "status": "ok" }

## Tests (before opening a PR)

From proj2/ run either:

- ./mvnw -q verify (recommended: formatting, style, tests, static analysis, coverage), or
- ./mvnw -q spotless:check && ./mvnw -q checkstyle:check && ./mvnw -q pmd:check && ./mvnw -q test

## Tooling and configuration

- License: MIT — see [LICENSE](LICENSE)
- Style checker: Checkstyle — config at [proj2/config/checkstyle/checkstyle.xml](proj2/config/checkstyle/checkstyle.xml)
- Code formatter: Spotless (Eclipse JDT) — configured in [proj2/pom.xml](proj2/pom.xml); shared editor prefs
  in [.editorconfig](.editorconfig)
- Syntax checker: PMD — ruleset at [proj2/config/pmd/ruleset.xml](proj2/config/pmd/ruleset.xml)
- Code coverage: JaCoCo — HTML report generated at proj2/target/site/jacoco/index.html after `./mvnw -q verify`

## CI test flow (summary)

The pipeline runs ./mvnw -B -q verify which enforces formatting (Spotless), style (Checkstyle), PMD analysis, runs
tests (Surefire/Failsafe), and produces a JaCoCo coverage report. If DB is needed in CI, add a MySQL service and pass
DB_* env vars.

## Full installation & troubleshooting

For complete setup instructions (Dockerized MySQL, local MySQL, environment overrides, Windows notes, and CI details),
see:

→ [INSTALL.md](INSTALL.md)
→ [SETUP.md](proj2/tutorial/SETUP.md)
