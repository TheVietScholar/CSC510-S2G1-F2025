# Dependencies

This document lists all third-party dependencies used in BoozeBuddies, including their versions, licenses, and purposes.

## Table of Contents

- [Backend Dependencies](#backend-dependencies)
- [Frontend Dependencies](#frontend-dependencies)
- [DevOps & Build Tools](#devops--build-tools)
- [License Information](#license-information)
- [Dependency Management](#dependency-management)

## Backend Dependencies

### Core Framework

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Spring Boot Starter Parent | 3.3.4 | Apache 2.0 | Parent POM for Spring Boot | Yes |
| Spring Boot Starter Web | 3.3.4 | Apache 2.0 | REST API and web layer | Yes |
| Spring Boot Starter Data JPA | 3.3.4 | Apache 2.0 | Database access with JPA/Hibernate | Yes |
| Spring Boot Starter Security | 3.3.4 | Apache 2.0 | Authentication and authorization | Yes |
| Spring Boot Starter Validation | 3.3.4 | Apache 2.0 | Bean validation | Yes |
| Spring Boot Starter Actuator | 3.3.4 | Apache 2.0 | Production monitoring and management | Yes |

**Links:**
- Spring Boot: https://spring.io/projects/spring-boot
- Documentation: https://docs.spring.io/spring-boot/docs/3.3.4/reference/html/

### Security & Authentication

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| jjwt-api | 0.11.5 | Apache 2.0 | JWT token API | Yes |
| jjwt-impl | 0.11.5 | Apache 2.0 | JWT token implementation | Yes |
| jjwt-jackson | 0.11.5 | Apache 2.0 | JWT JSON processing | Yes |

**Links:**
- JJWT: https://github.com/jwtk/jjwt
- Documentation: https://github.com/jwtk/jjwt#install

### Database

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| MySQL Connector/J | 8.0.33 | GPL 2.0 with FOSS Exception | MySQL database driver | Yes |
| Flyway Core | 9.22.3 | Apache 2.0 | Database migration tool | Yes |
| Flyway MySQL | 9.22.3 | Apache 2.0 | MySQL-specific Flyway support | Yes |
| H2 Database | 2.2.224 | MPL 2.0 / EPL 1.0 | In-memory database for testing | Yes (test only) |

**Links:**
- MySQL Connector: https://dev.mysql.com/downloads/connector/j/
- Flyway: https://flywaydb.org/
- H2: https://www.h2database.com/

### Utilities & Libraries

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Lombok | 1.18.32 | MIT | Boilerplate code reduction | Yes |
| Jackson Datatype JSR310 | 2.15.3 | Apache 2.0 | Java 8 date/time JSON support | Yes |

**Links:**
- Lombok: https://projectlombok.org/
- Jackson: https://github.com/FasterXML/jackson

### Testing

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Spring Boot Starter Test | 3.3.4 | Apache 2.0 | Testing support including JUnit 5 | Yes |
| JUnit Jupiter | 5.10.0 | EPL 2.0 | Testing framework | Yes |
| Mockito | 5.5.0 | MIT | Mocking framework | Yes |
| AssertJ | 3.24.2 | Apache 2.0 | Fluent assertions | Yes |
| Hamcrest | 2.2 | BSD 3-Clause | Matcher library | No |

**Links:**
- JUnit 5: https://junit.org/junit5/
- Mockito: https://site.mockito.org/
- AssertJ: https://assertj.github.io/doc/

### Build & Code Quality

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Spotless Maven Plugin | 2.45.0 | Apache 2.0 | Code formatting | Yes |
| Google Java Format | 1.23.0 | Apache 2.0 | Java code formatter | Yes |
| Maven Compiler Plugin | 3.14.0 | Apache 2.0 | Java compilation | Yes |
| Maven Surefire Plugin | 3.1.2 | Apache 2.0 | Unit test execution | Yes |
| Maven Failsafe Plugin | 3.1.2 | Apache 2.0 | Integration test execution | Yes |

**Links:**
- Spotless: https://github.com/diffplug/spotless
- Google Java Format: https://github.com/google/google-java-format

---

## Frontend Dependencies

### Core Framework

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| React | 19.2.0 | MIT | UI library | Yes |
| React DOM | 19.2.0 | MIT | React rendering for web | Yes |
| React Router DOM | 7.9.4 | MIT | Client-side routing | Yes |

**Links:**
- React: https://react.dev/
- React Router: https://reactrouter.com/

### HTTP & State Management

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Axios | 1.12.2 | MIT | HTTP client | Yes |

**Links:**
- Axios: https://axios-http.com/

### UI Components & Styling

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Tailwind CSS | 4.1.16 | MIT | Utility-first CSS framework | Yes |
| Headless UI | 2.2.9 | MIT | Accessible UI components | Yes |
| Lucide React | 0.548.0 | ISC | Icon library | Yes |

**Links:**
- Tailwind CSS: https://tailwindcss.com/
- Headless UI: https://headlessui.com/
- Lucide: https://lucide.dev/

### Testing

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| @testing-library/react | 16.3.0 | MIT | React component testing | Yes |
| @testing-library/jest-dom | 6.9.1 | MIT | Custom Jest matchers | Yes |
| @testing-library/user-event | 13.5.0 | MIT | User interaction simulation | Yes |
| @testing-library/dom | 10.4.1 | MIT | DOM testing utilities | Yes |
| web-vitals | 2.1.4 | Apache 2.0 | Performance monitoring | No |

**Links:**
- Testing Library: https://testing-library.com/
- Web Vitals: https://web.dev/vitals/

### Development Tools

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| Vite | 7.1.12 | MIT | Build tool and dev server | Yes |
| @vitejs/plugin-react | 5.0.4 | MIT | React plugin for Vite | Yes |
| ESLint | 8.57.1 | MIT | JavaScript linter | Yes |
| eslint-plugin-react-hooks | 7.0.0 | MIT | React hooks linting rules | Yes |
| eslint-plugin-react-refresh | 0.4.24 | MIT | React refresh linting | Yes |

**Links:**
- Vite: https://vite.dev/
- ESLint: https://eslint.org/

### Type Definitions

| Dependency | Version | License | Purpose | Mandatory |
|------------|---------|---------|---------|-----------|
| @types/react | 19.2.2 | MIT | TypeScript types for React | No |
| @types/react-dom | 19.2.2 | MIT | TypeScript types for React DOM | No |

**Links:**
- DefinitelyTyped: https://github.com/DefinitelyTyped/DefinitelyTyped

---

## DevOps & Build Tools

### Containerization

| Tool | Version | License | Purpose | Mandatory |
|------|---------|---------|---------|-----------|
| Docker | 24.x+ | Apache 2.0 | Container platform | Yes |
| Docker Compose | 2.x+ | Apache 2.0 | Multi-container orchestration | Yes |
| MySQL Docker Image | 8.0.43 | GPL 2.0 | Database container | Yes |

**Links:**
- Docker: https://www.docker.com/
- MySQL Docker: https://hub.docker.com/_/mysql

### Runtime

| Tool | Version | License | Purpose | Mandatory |
|------|---------|---------|---------|-----------|
| Java JDK | 17+ | GPL 2.0 with CPE | Java runtime | Yes |
| Node.js | 18+ | MIT | JavaScript runtime | Yes |
| npm | 9+ | Artistic 2.0 | Package manager | Yes |
| Maven | 3.6+ | Apache 2.0 | Build automation | Yes |

**Links:**
- OpenJDK: https://adoptium.net/
- Node.js: https://nodejs.org/
- Maven: https://maven.apache.org/

---

## License Information

### License Summary

| License | Count | Type | Commercial Use |
|---------|-------|------|----------------|
| Apache 2.0 | 25+ | Permissive | ✅ Yes |
| MIT | 20+ | Permissive | ✅ Yes |
| GPL 2.0 with Exception | 2 | Copyleft | ✅ Yes (with exception) |
| EPL 1.0/2.0 | 2 | Weak Copyleft | ✅ Yes |
| BSD 3-Clause | 1 | Permissive | ✅ Yes |
| ISC | 1 | Permissive | ✅ Yes |

### License Compatibility

All dependencies are compatible with the project's MIT license for distribution.

**Key Points:**
- **Apache 2.0**: Highly permissive, compatible with MIT
- **MIT**: Most permissive, no restrictions
- **GPL 2.0 with FOSS Exception**: MySQL connector allows use in FOSS projects
- **EPL**: Weak copyleft, changes to library must be shared but not application
- **BSD/ISC**: Similar to MIT, very permissive

### License Files

Complete license texts are available in:
```
licenses/
├── APACHE-2.0.txt
├── MIT.txt
├── GPL-2.0.txt
├── EPL-1.0.txt
├── EPL-2.0.txt
├── BSD-3-Clause.txt
└── ISC.txt
```

---

## Dependency Management

### Updating Dependencies

#### Backend Dependencies

```bash
# Check for updates
./mvnw versions:display-dependency-updates

# Update a specific dependency
./mvnw versions:use-latest-versions \
  -Dincludes=org.springframework.boot:*

# Update Spring Boot version
# Edit pom.xml parent version
```

#### Frontend Dependencies

```bash
cd frontend

# Check for updates
npm outdated

# Update dependencies
npm update

# Update to latest versions (careful!)
npm install package-name@latest
```

### Security Scanning

#### Backend

```bash
# OWASP Dependency Check
./mvnw dependency-check:check

# Snyk vulnerability scan
snyk test
```

#### Frontend

```bash
cd frontend

# npm audit
npm audit

# Fix vulnerabilities
npm audit fix

# Snyk scan
snyk test
```

### Dependency Lock Files

- **Backend**: `pom.xml` defines exact versions
- **Frontend**: `package-lock.json` locks transitive dependencies

**Important**: Always commit `package-lock.json` to ensure consistent builds.

### Adding New Dependencies

#### Backend

1. **Add to pom.xml**:
   ```xml
   <dependency>
       <groupId>com.example</groupId>
       <artifactId>library</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

2. **Check license compatibility**
3. **Update this documentation**
4. **Run security scan**
5. **Test thoroughly**

#### Frontend

1. **Install dependency**:
   ```bash
   npm install package-name
   ```

2. **Check license**:
   ```bash
   npm view package-name license
   ```

3. **Update this documentation**
4. **Run security scan**
5. **Test thoroughly**

### Dependency Guidelines

1. **Prefer well-maintained libraries**:
   - Active development
   - Good documentation
   - Large community
   - Recent updates

2. **Check licenses**:
   - Must be compatible with MIT
   - Avoid GPL without exception
   - Document all licenses

3. **Minimize dependencies**:
   - Avoid large libraries for small features
   - Consider bundle size impact (frontend)
   - Evaluate maintenance burden

4. **Security first**:
   - Scan for vulnerabilities
   - Update regularly
   - Monitor security advisories

5. **Document purpose**:
   - Update this file
   - Add comments in code
   - Note mandatory vs optional

---

## Transitive Dependencies

Some notable transitive dependencies (automatically included):

### Backend
- **Hibernate** (via Spring Data JPA)
- **HikariCP** (connection pooling via Spring Boot)
- **Logback** (logging via Spring Boot)
- **Tomcat** (embedded web server via Spring Boot)

### Frontend
- **React internals** (scheduler, reconciler, etc.)
- **PostCSS** (via Tailwind CSS)
- **ESBuild** (via Vite)

Use the following commands to view all dependencies:

```bash
# Backend
./mvnw dependency:tree

# Frontend
cd frontend && npm list
```

---

## Deprecated Dependencies

None currently. This section will track dependencies scheduled for removal.

---

## Version Constraints

### Backend

- **Java**: 17+ (LTS version)
- **Spring Boot**: 3.x (latest 3.3.x)
- **MySQL**: 8.0+ (for JSON support)

### Frontend

- **Node.js**: 18+ (LTS)
- **React**: 19.x (latest stable)
- **Vite**: 7.x (latest)

---

## Additional Resources

- **Maven Central**: https://search.maven.org/
- **npm Registry**: https://www.npmjs.com/
- **SPDX License List**: https://spdx.org/licenses/
- **Choose a License**: https://choosealicense.com/
- **FOSSA**: https://fossa.com/ (license compliance)

---

**Last Updated**: November 2025

For questions about dependencies or to report issues, please open a GitHub issue.
