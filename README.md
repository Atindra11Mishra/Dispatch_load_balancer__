# Dispatch Load Balancer API 🚚

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A smart logistics dispatch system that optimizes delivery order allocation to vehicles using a **priority-based greedy algorithm** with **distance minimization** (Haversine formula).

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Algorithm Explanation](#algorithm-explanation)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

---

## 🎯 Overview

The Dispatch Load Balancer API accepts delivery orders and vehicle fleet data, then generates an optimized dispatch plan that:

- ✅ Prioritizes HIGH → MEDIUM → LOW priority orders
- ✅ Assigns orders to nearest available vehicles
- ✅ Respects vehicle capacity constraints
- ✅ Minimizes total distance traveled
- ✅ Maximizes fleet utilization

**Use Cases:**
- Last-mile delivery optimization
- Food delivery dispatch
- E-commerce logistics
- Courier service routing

---

## ✨ Features

### Core Functionality
- **Smart Order Assignment**: Priority-based greedy algorithm
- **Distance Calculation**: Haversine formula for geographic accuracy
- **Capacity Management**: Automatic constraint validation
- **Real-time Planning**: Generate dispatch plans on-demand

### Technical Features
- RESTful API with OpenAPI 3.0 documentation
- Input validation with detailed error messages
- Comprehensive exception handling
- H2 in-memory database for testing
- 58 automated tests (100% core algorithm coverage)

---

## 🛠️ Tech Stack

### Backend
- **Java 21** - Modern Java LTS version
- **Spring Boot 3.5** - Application framework
- **Spring Data JPA** - ORM and data access
- **PostgreSQL** - Production database
- **H2 Database** - In-memory testing database

### API & Documentation
- **SpringDoc OpenAPI** - Auto-generated API documentation
- **Swagger UI** - Interactive API testing interface

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **MockMvc** - Controller testing
- **TestRestTemplate** - Integration testing

### Build & Tools
- **Maven** - Dependency management
- **Lombok** - Boilerplate reduction
- **SLF4J + Logback** - Logging

---

## 🏗️ Architecture