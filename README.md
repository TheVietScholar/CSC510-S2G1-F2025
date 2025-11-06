![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/TheVietScholar/CSC510-S2G1-F2025/java-ci.yml?style=for-the-badge)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.17458738.svg)](https://zenodo.org/doi/10.5281/zenodo.17458738)

# 🍻 BoozeBuddies — CSC510-S2G1-F2025

## 💡 Motivation (Why)
BoozeBuddies is designed to modernize local alcohol delivery by connecting customers, drivers, and breweries through a unified digital platform.  
Our goal is to streamline order processing, driver tracking, and payments while ensuring compliance with age verification and delivery safety standards. Also our goal is to extend on food delivery and tackle a new and profitable market.

Therefore, if you need a beer but don't want to drive, call your booze buddy!!!

In short: **we make local beer delivery faster, safer, and easier** for everyone involved.  
The project demonstrates the power of distributed teamwork, automation, and clean software architecture using modern tools like Docker, Spring Boot, and React.

---
## Demo
![QR to Demo Video](/proj2/misc/TRN8G2.jpg)
---

## 🧩 Tech Stack
**Front-End:**

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-B73BFE?style=for-the-badge&logo=vite&logoColor=FFD62E)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)

**Back-End:**

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![YAML](https://img.shields.io/badge/yaml-%23ffffff.svg?style=for-the-badge&logo=yaml&logoColor=151515)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)

**Database:**

![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)

**Dev-Tools:**

![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/apache_maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
[![Maven Central: Spotless](https://img.shields.io/maven-central/v/com.diffplug.spotless/spotless-maven-plugin.svg)](https://search.maven.org/artifact/com.diffplug.spotless/spotless-maven-plugin)
![JUnits](https://img.shields.io/badge/Junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

---

## ⚙️ What the Code Does
The system is structured around a **microservice-like layered architecture**:
- **Controllers** handle RESTful API requests (e.g., user registration, order creation, driver updates)
- **Services** contain business logic (e.g., price calculations, delivery routing, payment validation)
- **Repositories** communicate with the MySQL database using Spring Data JPA
- **DTOs (Data Transfer Objects)** define clear data boundaries between layers

Auto-generated Javadoc documentation can be built with:
```bash
mvn javadoc:javadoc


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

## ▶️ Step 6: Run the Application

Once the containers are running and MySQL is ready, start the Spring Boot application with the Docker profile:

./mvnw "-Dspring-boot.run.profiles=docker" spring-boot:run


## 💻 Frontend Setup (Vite + React)

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

## ⚠️ Troubleshooting (Frontend)

### Port already in use (5173):
If something else is using port 5173, start the dev server on another port:

npm run dev -- --port 5174


### Backend connection errors:
Ensure the Spring Boot backend is running on http://localhost:8080
.
If you changed the backend port, update the frontend proxy setting in vite.config.js.

### Environment variables:
If your project uses a .env file (for API URLs or keys), copy the example file before starting:

cp .env.example .env


Then edit .env to match your local setup.

## ✅ Environment Setup Complete!

You should now have:

Docker containers running MySQL

Spring Boot app running with the Docker profile

React + Vite frontend running on http://localhost:5173

MySQL Workbench or Adminer connected to your Dockerized database

If you encounter issues, double-check Docker container logs and verify port bindings in docker-compose.yml.

# Use Cases

Common scenarios and workflows for the BoozeBuddies API.

---

## 🔐 Use Case 1: New User Registration & First Order

**Scenario:** Alice wants to order beer for delivery.

1. **Register:** `POST /api/auth/register` with name, email, password
2. **Login:** `POST /api/auth/login` to get JWT token
3. **Browse:** `GET /api/products` to see available products
4. **Create Order:** `POST /api/orders` with items and delivery address
5. **Track:** `GET /api/orders/my-orders` to monitor order status

---

## 🛒 Use Case 2: Merchant Adds Products

**Scenario:** A merchant wants to add new products to their store.

1. **Admin registers merchant:** `POST /api/merchants/register`
2. **Admin verifies merchant:** `PUT /api/merchants/{id}/verify?verified=true`
3. **Admin assigns MERCHANT_ADMIN role:** `POST /api/users/{id}/roles` with merchant association
4. **Merchant adds products:** `POST /api/products` for each item
5. **Merchant updates inventory:** `PUT /api/products/{id}` when stock changes

---

## 🚚 Use Case 3: Complete Delivery Workflow

**Scenario:** A driver delivers an order from merchant to customer.

1. **Admin registers driver:** `POST /api/drivers/register`
2. **Admin approves certification:** `PUT /api/drivers/{id}/certification?status=APPROVED`
3. **Driver goes online:** `PUT /api/drivers/my-profile/availability?available=true`
4. **Admin assigns delivery:** `POST /api/deliveries/assign?orderId={id}&driverId={id}`
5. **Driver picks up:** `POST /api/deliveries/{id}/pickup`
6. **Driver en route:** `PUT /api/deliveries/{id}/status?status=IN_TRANSIT`
7. **Driver verifies age:** `POST /api/deliveries/{id}/verify-age?ageVerified=true`
8. **Driver completes:** `POST /api/deliveries/{id}/deliver`

---

## 💳 Use Case 4: Payment & Refund

**Scenario:** Customer pays for order, but needs a refund due to cancellation.

1. **User creates order:** `POST /api/orders`
2. **User pays:** `POST /api/payments/process?orderId={id}&paymentMethod=CREDIT_CARD`
3. **User cancels:** `POST /api/orders/{id}/cancel`
4. **Admin refunds:** `POST /api/payments/refund?orderId={id}&reason=Customer%20cancelled`

---

## 📊 Use Case 5: Merchant Reviews Orders & Revenue

**Scenario:** Merchant wants to see their orders and earnings.

1. **Merchant logs in:** `POST /api/auth/login`
2. **View their merchant:** `GET /api/merchants/my-merchant`
3. **View orders:** `GET /api/merchants/my-merchant/orders`
4. **Check products:** `GET /api/products/merchant/{merchantId}/all`
5. **Admin calculates revenue:** `GET /api/payments/revenue?startDate={date}&endDate={date}`

---

## 🔍 Use Case 6: Customer Age Verification

**Scenario:** System ensures customer is of legal drinking age.

1. **User registers:** Age verification defaults to `false`
2. **User verifies age:** `POST /api/users/{id}/verify-age` (requires ID check)
3. **User creates order:** System checks `ageVerified` status before processing
4. **Driver double-checks:** `POST /api/deliveries/{id}/verify-age` at delivery

---

> **Note:** For detailed endpoint documentation, parameters, and responses, see [API.md](proj2/docs/API.md)

# Demo Video (Not Done)


# 🧪 Testing

We use JUnit5 for backend testing and Mockito for mocking services.
GitHub Actions automatically runs all tests on every push to main.

Run tests locally:

mvn test


Test coverage reports are automatically generated and verified through CI.

# Collaboration:
All commits, issues, and PRs are tracked on GitHub

Issues are discussed before closure, with summaries posted in comments.

We communicate via Discord, and summaries of key decisions are documented in issues and in a shared google drive and in a discord chat.
We also communicated in person with two meetings every week plus additional meetings added if needed.


