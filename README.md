# Plataforma de Reservas de Consultorios Médicos Universitarios

**Backend API REST · Spring Boot 4 · PostgreSQL**

**Autores:** Sebastian Alcendra Lopez · Juan David Alvarez Tapias

---

## 1. Descripción general

Este proyecto implementa una API REST para la gestión integral de citas médicas dentro del servicio de salud universitario. Permite administrar pacientes, doctores, especialidades, consultorios, tipos de cita, horarios de atención y citas médicas, garantizando reglas de negocio reales, control de disponibilidad y reportes operativos.

La aplicación está construida con arquitectura por capas (repository → service), base de datos relacional PostgreSQL y un conjunto completo de pruebas automatizadas con Testcontainers, JUnit 5 y Mockito.

> **Nota:** La capa de controladores REST no fue implementada en esta entrega. Los DTOs de request/response están definidos en el paquete `api/dto` pero no hay endpoints expuestos. La lógica de negocio está completa en los servicios.

---

## 2. Stack tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21 |
| Spring Boot | 4.x |
| PostgreSQL | Latest (via Docker) |
| Testcontainers | Pruebas de integración con PostgreSQL real |
| JUnit 5 | Pruebas unitarias de servicios |
| Mockito | Mocking de dependencias en unit tests |
| MapStruct | Mapeo automático entre entidades y DTOs |
| Lombok | Reducción de código boilerplate |
| Maven | Gestión de dependencias y build |

---

## 3. Estructura del proyecto

Paquete base: `com.githubzs.plataforma_reservas_medicas`

| Paquete | Contenido |
|---|---|
| `api/dto` | DTOs de request y response agrupados por entidad (AppointmentDtos, DoctorDtos, etc.) |
| `domine/entities` | Entidades JPA (Patient, Doctor, Office, Appointment, Specialty, AppointmentType, DoctorSchedule) |
| `domine/enums` | Enumeraciones (AppointmentStatus, PatientStatus, DoctorStatus, OfficeStatus) |
| `domine/repositories` | Interfaces JPA con query methods y JPQL |
| `domine/dto` | DTOs internos usados en proyecciones JPQL (StatsDto, RankingDto, etc.) |
| `service` | Interfaces de servicio con el contrato público de cada módulo |
| `service/impl` | Implementaciones de servicios con toda la lógica de negocio |
| `service/mapper` | Interfaces MapStruct para conversión entidad ↔ DTO |
| `exception` | Excepciones personalizadas (ConflictException, ResourceNotFoundException) y GlobalExceptionHandler |

---

## 4. Modelo de datos

### 4.1 Entidades principales

| Entidad | Campos clave | Descripción |
|---|---|---|
| **Patient** | id (UUID), fullName, email, phoneNumber, documentNumber, studentCode, status, createdAt, updatedAt | Representa a los pacientes universitarios con código estudiantil opcional |
| **Doctor** | id (UUID), fullName, licenseNumber, documentNumber, email, status, specialty, createdAt, updatedAt | Médicos con especialidad asignada y número de licencia único |
| **Office** | id (UUID), name, location, description, roomNumber, status, createdAt, updatedAt | Consultorios con ubicación física y número de habitación |
| **Specialty** | id (UUID), name, description | Especialidades médicas con nombre único |
| **Appointment** | id (UUID), patient, doctor, office, appointmentType, startAt, endAt, status, cancelReason, observations, createdAt, updatedAt | Citas médicas con estado y observaciones |
| **AppointmentType** | id (UUID), name, durationMinutes, description | Tipos de cita con duración definida |
| **DoctorSchedule** | id (UUID), doctor, dayOfWeek, startTime, endTime | Horarios de atención semanal de cada doctor |

### 4.2 Enumeraciones

| Enum | Valores |
|---|---|
| **AppointmentStatus** | SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW |
| **PatientStatus** | ACTIVE, SUSPENDED, INACTIVE |
| **DoctorStatus** | ACTIVE, INACTIVE |
| **OfficeStatus** | AVAILABLE, UNAVAILABLE, MAINTENANCE |

---

## 5. Servicios implementados

| Servicio | Métodos principales | Responsabilidad |
|---|---|---|
| **PatientService** | create, findById, findAll, update, changeStatus | Gestión CRUD de pacientes con validación de estado |
| **DoctorService** | create, findById, findAll, update, changeStatus | Gestión CRUD de doctores con especialidad |
| **OfficeService** | create, findById, findAll, update, existsByIdAndStatus | Gestión CRUD de consultorios |
| **SpecialtyService** | create, findById, findAll, update | Gestión CRUD de especialidades médicas |
| **AppointmentTypeService** | create, findById, findAll, update | Gestión CRUD de tipos de cita |
| **DoctorScheduleService** | create, findById, findAll, update, delete | Gestión de horarios de doctores |
| **AppointmentService** | create, findById, findAll, updateStatus, cancel, confirm, complete, markNoShow | Lógica completa de ciclo de vida de citas |
| **AvailabilityService** | getAvailableSlots | Cálculo de slots disponibles por doctor, fecha y tipo de cita |
| **ReportService** | getOfficeOccupancy, getDoctorProductivity, getNoShowPatients | Generación de reportes operativos |

---

## 6. Ejecución del proyecto

### 6.1 Requisitos previos

- Java 21 instalado y configurado en PATH
- Docker Desktop instalado y en ejecución
- Maven 3.9+ (o usar el wrapper incluido `./mvnw`)

### 6.2 Base de datos

La base de datos de desarrollo debe ser una instancia PostgreSQL accesible localmente. Configura las variables en `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/test1
    username: admin
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

El nombre de la base de datos, usuario y contraseña pueden variar por desarrollador. Cada integrante debe configurar su propio entorno local.

### 6.3 Compilar y ejecutar
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

---

## 7. Pruebas automatizadas

### 7.1 Configuración de Testcontainers

Las pruebas de integración utilizan Testcontainers para levantar automáticamente un contenedor PostgreSQL real durante la ejecución. No es necesario tener una base de datos local configurada para correr los tests — solo Docker Desktop debe estar activo.

La configuración usa `@ServiceConnection` para conectar automáticamente el contenedor al contexto de Spring Boot:
```java
@Bean
@ServiceConnection
PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
}
```

### 7.2 Ejecutar las pruebas
```bash
./mvnw test
```

### 7.3 Cobertura de pruebas

| Tipo de prueba | Clases cubiertas |
|---|---|
| Integration tests (Repository) | PatientRepositoryIntegrationTest, DoctorRepositoryIntegrationTest, DoctorScheduleRepositoryIntegrationTest, AppointmentRepositoryIntegrationTest, OfficeRepositoryIntegrationTest, SpecialtyRepositoryIntegrationTest |
| Unit tests (Service) | AppointmentServiceImplTest, AvailabilityServiceImplTest, DoctorScheduleServiceImplTest, DoctorServiceImplTest, OfficeServiceImplTest |
| Unit tests (Mapper) | PatientMapperTest, DoctorMapperTest, OfficeMapperTest, AppointmentMapperTest, AppointmentTypeMapperTest, SpecialtyMapperTest, DoctorScheduleMapperTest, PatientSummaryMapperTest, DoctorSummaryMapperTest, OfficeSummaryMapperTest, AppointmentSummaryMapperTest, DoctorScheduleSummaryMapperTest, SpecialtySummaryMapperTest |

---

## 8. Reglas de negocio implementadas

### 8.1 Creación de citas

- El paciente debe existir y estar en estado `ACTIVE`.
- El doctor debe existir y estar en estado `ACTIVE`.
- El consultorio debe existir y estar en estado `AVAILABLE`.
- La fecha y hora de inicio no puede ser pasada ni igual al momento actual.
- La cita debe quedar dentro del horario laboral configurado para el doctor en ese día de la semana.
- El campo `endAt` es calculado por el servicio usando la duración del tipo de cita; no lo envía el cliente.
- No puede existir traslape de horario para el doctor en el mismo rango temporal.
- No puede existir traslape de horario para el consultorio en el mismo rango temporal.
- Un paciente no puede tener dos citas activas que se crucen en el tiempo.
- Toda cita nueva se crea con estado inicial `SCHEDULED`.

### 8.2 Confirmación — `SCHEDULED → CONFIRMED`

- Solo una cita en estado `SCHEDULED` puede pasar a `CONFIRMED`.
- No se puede confirmar una cita `CANCELLED`, `COMPLETED` o `NO_SHOW`.
- La confirmación actualiza el campo `updatedAt`.

### 8.3 Cancelación — `SCHEDULED | CONFIRMED → CANCELLED`

- Solo se pueden cancelar citas en estado `SCHEDULED` o `CONFIRMED`.
- No se puede cancelar una cita `COMPLETED` ni `NO_SHOW`.
- El motivo de cancelación es obligatorio (campo `cancelReason`).

### 8.4 Finalización — `CONFIRMED → COMPLETED`

- Solo una cita `CONFIRMED` puede pasar a `COMPLETED`.
- No se puede completar una cita si la hora actual es anterior al inicio programado.
- Se pueden registrar observaciones administrativas al finalizar.

### 8.5 No asistencia — `CONFIRMED → NO_SHOW`

- Solo una cita `CONFIRMED` puede pasar a `NO_SHOW`.
- No se puede marcar como `NO_SHOW` antes de la hora de inicio programada.

### 8.6 Disponibilidad

- Los slots disponibles se calculan combinando el horario laboral del doctor, las citas existentes activas y la duración del tipo de cita.
- Solo se devuelven bloques completos y libres; no horas aproximadas.

### 8.7 Gestión de estados

- **Pacientes**: Solo pacientes `ACTIVE` pueden agendar citas. Estados: `ACTIVE`, `SUSPENDED`, `INACTIVE`.
- **Doctores**: Solo doctores `ACTIVE` pueden tener citas asignadas. Estados: `ACTIVE`, `INACTIVE`.
- **Consultorios**: Solo consultorios `AVAILABLE` pueden ser asignados a citas. Estados: `AVAILABLE`, `UNAVAILABLE`, `MAINTENANCE`.

---

## 9. Decisiones de diseño

### 9.1 Separación de responsabilidades

Cada capa tiene responsabilidades claramente delimitadas. Los DTOs con anotaciones `@NotNull` y `@NotBlank` validan la forma estructural del dato (presencia, formato). Los servicios validan exclusivamente lógica de negocio (existencia en base de datos, estado de entidades, traslapes, reglas de flujo). No se duplican validaciones entre capas.

### 9.2 Borrado lógico

Ninguna entidad se elimina físicamente de la base de datos. Pacientes, doctores y consultorios manejan estados (`ACTIVE/INACTIVE`, `AVAILABLE/UNAVAILABLE`). Las citas manejan un ciclo de vida completo vía transiciones de estado. Esto garantiza trazabilidad y consistencia del historial médico.

### 9.3 Cálculo de `endAt` en el servidor

El campo `endAt` de cada cita nunca es enviado por el cliente. El servicio lo calcula internamente como `startAt + durationMinutes` del `AppointmentType` seleccionado. Esto evita inconsistencias y garantiza que la duración siempre corresponde al tipo de cita configurado.

### 9.4 MapStruct para mapeos

Se usa MapStruct para la conversión entre entidades y DTOs. Los mappers complejos (`AppointmentMapper`, `AppointmentSummaryMapper`) delegan a submappers específicos (`PatientSummaryMapper`, `DoctorSummaryMapper`, `OfficeSummaryMapper`) para mantener cada mapper con una responsabilidad única.

### 9.5 Proyecciones JPQL con DTOs internos

Las queries de agregación y reportes (ocupación de consultorios, ranking de doctores, inasistencias por paciente) retornan proyecciones a DTOs internos del dominio (`OfficeOccupancyDto`, `DoctorRankingStatsDto`, `PatientNoShowStatsDto`). El servicio transforma esos DTOs internos a los DTOs de respuesta pública.

### 9.6 Testcontainers con `@ServiceConnection`

Las pruebas de integración usan `@ServiceConnection` de Spring Boot junto con Testcontainers. Esto elimina la necesidad de configurar manualmente las propiedades de conexión en los tests — el contenedor PostgreSQL levantado por Testcontainers se conecta automáticamente al contexto de prueba.

### 9.7 Excepciones personalizadas

Se implementan excepciones específicas (`ConflictException` para conflictos de negocio, `ResourceNotFoundException` para recursos inexistentes) manejadas por un `GlobalExceptionHandler` que retorna respuestas HTTP apropiadas.

### 9.8 UUID como identificadores

Todas las entidades usan `UUID` como clave primaria para evitar colisiones en entornos distribuidos y mejorar la seguridad al no exponer secuencias predecibles.

---

## 10. Contribución

1. Clona el repositorio
2. Crea una rama para tu feature: `git checkout -b feature/nueva-funcionalidad`
3. Implementa los cambios siguiendo la arquitectura por capas
4. Agrega pruebas unitarias e integración para nueva funcionalidad
5. Ejecuta `./mvnw test` para verificar que todo pasa
6. Crea un Pull Request con descripción detallada

---

## 11. Licencia

Proyecto educativo - No aplica licencia comercial.
