# ADR 0001 - Modular Hexagonal Monolith

## Status

Accepted for the MVP.

## Context

ClinicCore needs real domain boundaries without the operational cost of microservices. The MVP includes identity, clinic management, patients, scheduling, treatment notes, packages, exercise plans, analytics and audit logs.

## Decision

Use a modular monolith with hexagonal direction:

- `api`: REST controllers and request/response DTOs.
- `application`: use cases and orchestration.
- `domain`: business concepts, enums and rules.
- `infrastructure`: persistence, security and adapters.

The database model stores foreign keys as IDs between modules. That keeps module boundaries explicit and avoids leaking JPA relationships into API workflows.

## Consequences

- The project is simple to run locally and easy to test.
- Business rules can move from services to richer domain objects as the product grows.
- Future adapters for email, storage, PDF generation or notifications can be added without changing controllers.
- A future split into services is possible, but not part of the initial plan.
