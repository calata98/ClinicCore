# ClinicCore

ClinicCore es un MVP backend para que una clinica pequena gestione pacientes, agenda, tratamientos, notas de sesion, bonos, ejercicios, dashboard y auditoria de accesos.

El proyecto esta creado con Java 25 y Spring Boot 4.0.5. Spring Boot 4.0.x declara soporte para Java 17 hasta Java 26, asi que Java 25 encaja dentro del rango soportado.

## Estado del MVP

Incluye:

- Login con BCrypt, JWT de acceso y refresh tokens rotados.
- Roles base: `OWNER`, `ADMIN`, `PROFESSIONAL`, `RECEPTIONIST`, `PATIENT`.
- Tenant por clinica en las operaciones principales.
- Clinicas, profesionales, salas y servicios.
- Pacientes con separacion de notas administrativas.
- Agenda con creacion, reprogramacion, cancelacion, completado y no-show.
- Validacion de solapamientos por profesional y sala.
- Episodios de tratamiento y notas evolutivas.
- Bonos de sesiones y consumo de sesiones.
- Catalogo de ejercicios y planes asignados al paciente.
- Dashboard basico de actividad.
- Auditoria de eventos relevantes.
- Flyway, PostgreSQL, Swagger/OpenAPI, tests, Docker Compose, Dockerfile y CI en GitHub Actions.

No incluye todavia facturacion legal, documentos adjuntos, firma digital, recordatorios por email, portal real de paciente ni integraciones externas.

## Stack

- Java 25
- Spring Boot 4.0.5
- Spring Security 7
- Spring Data JPA / Hibernate 7
- PostgreSQL 18
- Flyway
- Maven Wrapper
- JUnit 6, MockMvc, H2 en tests
- Springdoc OpenAPI

## Arranque local

Requisitos:

- JDK 25
- Docker, o una instancia PostgreSQL local compatible

Levantar PostgreSQL:

```powershell
docker compose up -d
```

Ejecutar tests:

```powershell
.\mvnw.cmd test
```

Arrancar la API:

```powershell
.\mvnw.cmd spring-boot:run
```

Construir imagen Docker:

```powershell
docker build -t cliniccore:local .
```

La API queda en:

- `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Usuario demo

Con `CLINICCORE_BOOTSTRAP_DEMO=true`, la aplicacion crea datos iniciales:

- Clinica: `Fisio Norte`
- Usuario: `owner@cliniccore.local`
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

Usa el `accessToken` devuelto como:

```http
Authorization: Bearer <accessToken>
```

## Configuracion

Puedes copiar `.env.example` o definir estas variables:

```text
CLINICCORE_DB_URL=jdbc:postgresql://localhost:5432/cliniccore
CLINICCORE_DB_USERNAME=cliniccore
CLINICCORE_DB_PASSWORD=cliniccore
CLINICCORE_JWT_SECRET=replace-this-with-a-long-random-secret-for-local-dev
CLINICCORE_BOOTSTRAP_DEMO=true
```

## Endpoints principales

Autenticacion:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`

Clinica:

- `GET /api/clinics/current`
- `POST /api/professionals`
- `GET /api/professionals`
- `POST /api/rooms`
- `GET /api/rooms`
- `POST /api/services`
- `GET /api/services`

Pacientes:

- `POST /api/patients`
- `GET /api/patients?search=juan`
- `GET /api/patients/{patientId}`
- `PATCH /api/patients/{patientId}`
- `DELETE /api/patients/{patientId}`

Agenda:

- `POST /api/appointments`
- `GET /api/appointments?from=2026-04-28T08:00:00Z&to=2026-04-29T08:00:00Z`
- `PATCH /api/appointments/{appointmentId}/reschedule`
- `PATCH /api/appointments/{appointmentId}/cancel`
- `PATCH /api/appointments/{appointmentId}/complete`
- `PATCH /api/appointments/{appointmentId}/no-show`

Tratamientos:

- `POST /api/patients/{patientId}/episodes`
- `PATCH /api/episodes/{episodeId}/close`
- `POST /api/episodes/{episodeId}/session-notes`
- `GET /api/patients/{patientId}/timeline`

Bonos:

- `POST /api/packages`
- `GET /api/packages`
- `POST /api/patients/{patientId}/packages`
- `GET /api/patients/{patientId}/packages`
- `PATCH /api/patient-packages/{patientPackageId}/consume-session`

Ejercicios:

- `POST /api/exercises`
- `GET /api/exercises`
- `POST /api/patients/{patientId}/exercise-plans`
- `GET /api/patients/{patientId}/exercise-plans`

Dashboard y auditoria:

- `GET /api/dashboard/clinic`
- `GET /api/audit-logs`

## Arquitectura

El proyecto sigue un monolito modular con direccion hexagonal:

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

Dentro de cada modulo:

```text
api              REST controllers y DTOs
application      casos de uso
domain           reglas y conceptos de negocio
infrastructure   JPA, seguridad y adaptadores
```

Ver tambien `docs/adr/0001-architecture.md`.

## Seguridad y cumplimiento

El MVP aplica una base seria:

- Passwords con BCrypt.
- JWT de vida corta.
- Refresh tokens guardados con hash SHA-256 y rotacion.
- Roles por endpoint.
- Operaciones filtradas por `clinicId`.
- Auditoria de login, lectura de paciente, timeline clinico, agenda, notas, bonos y dashboard.
- Flyway para evolucion controlada de esquema.

La aplicacion evita interpretar datos clinicos. Las notas evolutivas son texto introducido por el profesional. Aun faltan piezas para un producto sanitario completo: gestion avanzada de consentimientos/documentos, politicas de retencion, exportacion completa, cifrado de adjuntos, permisos granulares y revision legal especifica.

## Tests

Actualmente hay:

- `ClinicCoreApplicationTests`: arranque de contexto con H2 y Flyway.
- `AuthControllerTests`: login demo y lectura de perfil con JWT.
- `AppointmentRulesTests`: reglas de solapamiento y rangos invalidos.

Ejecutar:

```powershell
.\mvnw.cmd test
```

## Roadmap sugerido

1. Crear usuarios y asignar roles desde API.
2. Disponibilidad de profesionales, vacaciones y ausencias.
3. Portal de paciente.
4. Documentos, consentimientos versionados y adjuntos.
5. Recordatorios por email.
6. Exportacion PDF de resumen clinico.
7. Testcontainers con PostgreSQL real en CI.
8. Permisos granulares.
9. Busqueda avanzada y filtros de agenda.
