
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/TheVietScholar/CSC510-S2G1-F2025/java-ci.yml?style=for-the-badge)

[![Maven Central](https://img.shields.io/maven-central/v/com.diffplug.spotless/spotless-maven-plugin.svg)](https://search.maven.org/artifact/com.diffplug.spotless/spotless-maven-plugin)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)

![YAML](https://img.shields.io/badge/yaml-%23ffffff.svg?style=for-the-badge&logo=yaml&logoColor=151515)

![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)

![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

# CSC510-S2G1-F2025 Project Setup Guide

This document provides step-by-step instructions to set up the project environment locally and connect to the MySQL database running in Docker.

---

## 🚀 Step 1: Clone the Repository

Clone the repository using your preferred IDE (VS Code, IntelliJ, Eclipse, etc.) or from the terminal:

```bash
git clone https://github.com/TheVietScholar/CSC510-S2G1-F2025.git
````

---

## ⚙️ Step 2: Install Maven

If Maven is not already installed, follow the official installation guide:

🔗 [Install Apache Maven](https://maven.apache.org/install.html)

---

## 🐳 Step 3: Install Docker and Docker Compose

If you don’t already have Docker installed, follow the steps below:

* 🔗 [Install Docker Desktop](https://docs.docker.com/get-docker/)
* Ensure **Docker Compose** is included (it comes by default with Docker Desktop).

After installation, verify Docker and Docker Compose are working:

```bash
docker --version
docker-compose --version
```

---

## 🗄️ Step 4: Install MySQL (Optional for Local Access)

You can install MySQL locally if you want to access it outside Docker (e.g., using MySQL Workbench):

🔗 [MySQL Download Page](https://dev.mysql.com/downloads/)

> Note: The app uses a MySQL instance running inside Docker, so installing MySQL locally is optional.

---

## 🧹 Step 5: Reset Docker Environment (if needed)

If you previously had Docker or Docker Compose containers running, remove them to avoid conflicts.

### Stop and Remove All Containers, Networks, and Volumes

```bash
docker-compose down -v
```

### Start Fresh Containers

```bash
docker-compose up -d
```

### Wait for MySQL to Initialize

Allow around 15 seconds for MySQL to fully start:

```bash
Start-Sleep -Seconds 15   # For PowerShell
# OR
sleep 15                  # For macOS/Linux
```

---

## ▶️ Step 6: Run the Application

Once the containers are running and MySQL is ready, start the Spring Boot application with the Docker profile:

```bash
./mvnw "-Dspring-boot.run.profiles=docker" spring-boot:run
```

---

## 🧩 Step 7: Connect MySQL Workbench to Docker MySQL

If your MySQL Docker container doesn’t appear automatically in MySQL Workbench, create a manual connection.

### Steps to Connect:

1. Open **MySQL Workbench**

2. Click the **“+”** next to **MySQL Connections**

3. Set up the connection:

   * **Connection Name:** Docker MySQL (or any name you prefer)
   * **Hostname:** `127.0.0.1` or `localhost`
   * **Port:** `33060` (check your `docker-compose.yml`)
   * **Username:** `app`
   * **Password:** *(from your `docker-compose.yml` file)*

4. Click **Test Connection** to verify.

### Example Connection Details

```
Host: 127.0.0.1
Port: 33060
User: app
Password: [check docker-compose.yml]
```

---

## 🔍 Troubleshooting MySQL Connection

### Reason 1: Different Connection Methods

Dockerized MySQL runs inside a container, not as a local service that Workbench auto-detects.

### Reason 2: External Connection Setup

Ensure your `docker-compose.yml` exposes the correct port mapping:

```yaml
ports:
  - "33060:3306"
```

### Reason 3: Test External Connection from Terminal

You can verify access to the MySQL instance using:

```bash
mysql -h 127.0.0.1 -P 33060 -u app -p -e "SHOW DATABASES;"
```


## ✅ Environment Setup Complete!

You should now have:

* Docker containers running MySQL
* Spring Boot app running with the Docker profile
* MySQL Workbench or Adminer connected to your Dockerized database

If you encounter issues, double-check Docker container logs and verify the port bindings in `docker-compose.yml`.

