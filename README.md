[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

# World Cup Sticker Catalog

MVP for a digital World Cup sticker catalog that exposes public stock availability and provides a protected administrative area for inventory management.

The main goal is to reduce repetitive WhatsApp questions by allowing customers to check sticker availability before contacting the seller.

## Stack

- Java 21
- Spring Boot 3
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- PostgreSQL
- Flyway
- Maven
- Docker Compose

## Planned Features

- Public sticker catalog
- Advanced filters for stock lookup
- Support for regular stickers
- Support for Legends variants: Bronze, Silver, and Gold
- Protected administrative area
- Stock management
- Inventory control
- Database schema versioning with migrations

## Requirements

Before running the project locally, make sure you have:

- JDK 21
- Docker and Docker Compose
- Maven Wrapper, already included in the project

To check which Java version Maven is using:

```bash
./mvnw -version
```

## Running Locally

Start the local dependencies:

```bash
docker compose up -d
```

Run the tests:

```bash
./mvnw test
```

Start the application:

```bash
./mvnw spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

To stop the local dependencies:

```bash
docker compose down
```

## Database

The project uses PostgreSQL as its main database.

During local development, the database should run separately from the application, preferably through Docker Compose. In production, the intended approach is to use a managed PostgreSQL service, such as Railway.

Schema changes must be versioned with Flyway. The application should not rely on automatic schema generation by Hibernate in relevant environments.

Expected JPA configuration:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

## Domain Guidelines

The domain should separate concepts that may look similar but have different responsibilities:

- **Sticker**: catalog definition, such as code, national team, player, type, and variant.
- **Stock item**: commercial availability of a sticker.
- **Inventory movement**: history of stock entries, exits, and adjustments.

This separation avoids mixing catalog, sales, and inventory rules in the same entity.

## Expected Architecture

The project should evolve incrementally while preserving low coupling between layers:

- Controllers for web entry points
- Services or application services for use case coordination
- Domain model for core rules
- Repositories for persistence
- Thymeleaf templates for server-side rendering

Dynamic catalog filters should preferably be implemented with Spring Data JPA Specification, avoiding rigid combinations of repository methods.

## Status

Project in early MVP stage.

Important next technical decisions:

- Consolidate the local PostgreSQL environment
- Define the first migration
- Model stickers, variants, and stock
- Create the public catalog
- Structure the administrative area
