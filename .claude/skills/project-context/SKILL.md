---
name: project-context
description: Contexto del proyecto "sublyx", un tracker de suscripciones online (API con Java + Spring Boot, frontend ligero). Usa este skill SIEMPRE al empezar cualquier tarea en este repositorio, y sobre todo cuando la petición sea vaga ("set up database for project", "añade una feature", "organiza el proyecto", "qué falta por hacer") o cuando pregunten por el stack, la estructura de paquetes, las convenciones de código o qué entra y qué no en el MVP.
---

# Contexto del proyecto

## Qué es
App web para centralizar las suscripciones online de una persona, avisar antes de que se renueven (o de que acabe una prueba gratuita) y llevar el gasto total. Es un proyecto personal del reto "un producto al mes". **El objetivo principal es aprender Spring Boot y tener algo publicable para el portfolio**, no maximizar funcionalidades. Mantén el alcance pequeño.

## Stack
- Backend: Java 21, Spring Boot 4.x, Gradle (Kotlin DSL).
- Base de datos: PostgreSQL, esquema gestionado solo con Flyway.
- Persistencia: Spring Data JPA (`ddl-auto: validate`, `open-in-view: false`).
- Seguridad: Spring Security con sesión por cookie HttpOnly (no JWT).
- Frontend (más adelante): web ligera, preferiblemente SvelteKit estático, como PWA. Nada de Next.js.
- Desarrollo local: `compose.yaml` en la raíz; Spring Boot lo levanta con `spring-boot-docker-compose`.

Antes de añadir una dependencia o asumir un nombre de starter, **lee `build.gradle.kts`**. No inventes versiones. Spring Boot 4 reorganizó los starters en módulos y cambió cosas respecto a la rama 3.x (por ejemplo Jackson), así que muchos tutoriales antiguos no valen tal cual: verifica los imports contra lo que hay en el classpath.

## Estructura de paquetes (por funcionalidad, no por capas)
Usa el paquete raíz que ya exista en `src/main/java` y organiza dentro así:
```
auth          login, registro, configuración de seguridad
subscription  entity, repository, service, controller, dto
reminder      entity, job programado, envío de email
dashboard     resumen de gasto mensual y anual
calendar      feed .ics
common        errores, configuración, utilidades de fechas
```

## MVP (lo que sí entra)
- Registro, login y logout.
- CRUD de suscripciones: nombre, precio, moneda, ciclo, próxima renovación, categoría, estado (activa / prueba gratuita / cancelada), fin de prueba, notas.
- Panel con gasto mensual y anual y próximas renovaciones.
- Job diario con `@Scheduled` que envía email antes de renovar o de que acabe una prueba.
- Feed `.ics` para el calendario.

## Fuera del MVP (no lo hagas sin que se pida)
Importar CSV del banco, leer Gmail, multidivisa con conversión, PDF, API pública, notificaciones push, app nativa.

## Convenciones de código
- Identificadores, nombres de tablas y mensajes de commit en inglés. Las explicaciones al usuario, en español.
- DTOs como `record`. Sin Lombok.
- Inyección por constructor. Servicios con `@Transactional` donde toque.
- Dinero: `BigDecimal` (columna `NUMERIC(10,2)`). Fechas de calendario: `LocalDate`. Instantes: `Instant`.
- Guarda solo la próxima fecha de renovación y calcula las siguientes a partir del ciclo; no almacenes todas las fechas futuras.
- Errores de la API con `@RestControllerAdvice` y `ProblemDetail`.
- Validación con Bean Validation en los DTOs de entrada.
- Nunca subas secretos a Git. Contraseñas y claves van en `.env` (ignorado por Git) o variables de entorno.

## Cómo trabajar
- Cambios pequeños y verificables. Después de cada paso, comprueba que la app arranca y que los tests pasan.
- Si una petición es ambigua, propón el siguiente paso concreto en vez de listar capacidades genéricas.
- Antes de tocar el esquema, usa el skill `flyway-migrations`. Para la base de datos local, `database-setup`. Para una funcionalidad nueva, `spring-feature`.
