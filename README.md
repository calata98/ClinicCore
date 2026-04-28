# ClinicCore

ClinicCore is a backend MVP for small clinics to manage patients, scheduling, treatment episodes, session notes, session packages, exercise plans, clinic dashboards, and audit logs.

The project is built with Java 25 and Spring Boot 4.0.5. Spring Boot 4.0.x supports Java 17 through Java 26, so Java 25 is within the supported range.

## MVP Status

Included:

- Login with BCrypt, short-lived JWT access tokens, and rotated refresh tokens.
- Base roles: `OWNER`, `ADMIN`, `PROFESSIONAL`, `RECEPTIONIST`, `PATIENT`.
- Clinic tenant isolation in the main workflows.
- Clinics, professionals, rooms, and services.
- Patients with administrative notes kept separate from clinical notes.
- Scheduling with create, reschedule, cancel, complete, and no-show actions.
- Overlap validation by professional and room.
- Treatment episodes and clinical progress notes.
- Session packages and session consumption.
- Exercise catalog and exercise plans assigned to patients.
- Basic clinic activity dashboard.
- Audit events for relevant operations.
- Flyway, PostgreSQL, Swagger/OpenAPI, tests, Docker Compose, Dockerfile, and GitHub Actions CI.

Not included yet: legal invoicing, file attachments, digital signatures, email reminders, a real patient portal, or external integrations.

## Stack

- Java 25
- Spring Boot 4.0.5
- Spring Security 7
- Spring Data JPA / Hibernate 7
- PostgreSQL 18
- Flyway
- Maven Wrapper
- JUnit 6, MockMvc, and H2 for tests
- Springdoc OpenAPI

## Local Setup

Requirements:

- JDK 25
- Docker, or a compatible local PostgreSQL instance

Start PostgreSQL:

```powershell
docker compose up -d
```

Run tests:

```powershell
.\mvnw.cmd test
```

Start the API:

```powershell
.\mvnw.cmd spring-boot:run
```

Build the Docker image:

```powershell
docker build -t cliniccore:local .
```

The API will be available at:

- `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Demo User

With `CLINICCORE_BOOTSTRAP_DEMO=true`, the application creates initial demo data:

- Clinic: `Fisio Norte`
- User: `owner@cliniccore.local`
- Password: `ChangeMe123!`

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "owner@cliniccore.local",
  "password": "ChangeMe123!"
}
```

Use the returned `accessToken` as:

```http
Authorization: Bearer <accessToken>
```

## Configuration

You can copy `.env.example` or define these environment variables:

```text
CLINICCORE_DB_URL=jdbc:postgresql://localhost:5432/cliniccore
CLINICCORE_DB_USERNAME=cliniccore
CLINICCORE_DB_PASSWORD=cliniccore
CLINICCORE_JWT_SECRET=replace-this-with-a-long-random-secret-for-local-dev
CLINICCORE_BOOTSTRAP_DEMO=true
```

## Main Endpoints

Authentication:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`

Clinic:

- `GET /api/clinics/current`
- `POST /api/professionals`
- `GET /api/professionals`
- `POST /api/rooms`
- `GET /api/rooms`
- `POST /api/services`
- `GET /api/services`

Patients:

- `POST /api/patients`
- `GET /api/patients?search=juan`
- `GET /api/patients/{patientId}`
- `PATCH /api/patients/{patientId}`
- `DELETE /api/patients/{patientId}`

Scheduling:

- `POST /api/appointments`
- `GET /api/appointments?from=2026-04-28T08:00:00Z&to=2026-04-29T08:00:00Z`
- `PATCH /api/appointments/{appointmentId}/reschedule`
- `PATCH /api/appointments/{appointmentId}/cancel`
- `PATCH /api/appointments/{appointmentId}/complete`
- `PATCH /api/appointments/{appointmentId}/no-show`

Treatments:

- `POST /api/patients/{patientId}/episodes`
- `PATCH /api/episodes/{episodeId}/close`
- `POST /api/episodes/{episodeId}/session-notes`
- `GET /api/patients/{patientId}/timeline`

Packages:

- `POST /api/packages`
- `GET /api/packages`
- `POST /api/patients/{patientId}/packages`
- `GET /api/patients/{patientId}/packages`
- `PATCH /api/patient-packages/{patientPackageId}/consume-session`

Exercises:

- `POST /api/exercises`
- `GET /api/exercises`
- `POST /api/patients/{patientId}/exercise-plans`
- `GET /api/patients/{patientId}/exercise-plans`

Dashboard and audit:

- `GET /api/dashboard/clinic`
- `GET /api/audit-logs`

## Architecture

The project follows a modular monolith with hexagonal direction:

```text
src/main/java/com/cliniccore
  analytics/
  audit/
  billing/
  clinic/
  exercise/
  identity/
  patient/
  scheduling/
  shared/
  treatment/
```

Inside each module:

```text
api              REST controllers and DTOs
application      use cases
domain           business rules and domain concepts
infrastructure   JPA, security, and adapters
```

See also `docs/adr/0001-architecture.md`.

## Security and Compliance Notes

The MVP includes a serious baseline:

- Passwords are hashed with BCrypt.
- Access tokens are short-lived JWTs.
- Refresh tokens are stored as SHA-256 hashes and rotated.
- Endpoints are protected by roles.
- Main operations are scoped by `clinicId`.
- Login, patient reads, clinical timeline reads, scheduling, notes, packages, and dashboard access are audited.
- Flyway controls schema evolution.

The application does not interpret clinical data. Progress notes are free text written by the professional. A production healthcare product would still need more work: advanced consent/document management, retention policies, full data export, attachment encryption, granular permissions, and a dedicated legal review.

## Tests

Current tests:

- `ClinicCoreApplicationTests`: application context startup with H2 and Flyway.
- `AuthControllerTests`: demo login and JWT-protected profile read.
- `AppointmentRulesTests`: overlap and invalid time range rules.

Run:

```powershell
.\mvnw.cmd test
```

## Suggested Roadmap

1. User creation and role assignment API.
2. Professional availability, holidays, and absences.
3. Patient portal.
4. Versioned consent documents and attachments.
5. Email reminders.
6. Clinical summary PDF export.
7. Testcontainers with real PostgreSQL in CI.
8. Granular permissions.
9. Advanced search and scheduling filters.
