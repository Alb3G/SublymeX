---
name: spring-feature
description: Guía para implementar una funcionalidad nueva de la API (entidad, repositorio, servicio, controlador, DTOs, validación y tests) en el estilo del proyecto Spring Boot. Usa este skill siempre que el usuario pida añadir o modificar un endpoint, un CRUD, una entidad JPA, un servicio, el job de recordatorios, el feed .ics, el dashboard o la seguridad, aunque no diga la palabra "feature".
---

# Añadir una funcionalidad a la API

Sigue este orden. No saltes pasos, y verifica que todo compila y arranca antes de pasar al siguiente.

## 1. Esquema primero
Si hace falta una tabla o columna nueva, crea la migración con el skill `flyway-migrations`. La base de datos manda; la entidad la refleja.

## 2. Paquete
Trabaja dentro del paquete de la funcionalidad (`subscription`, `reminder`, `dashboard`...), con el paquete raíz que ya exista en `src/main/java`.

## 3. Capas
- **Entity**: JPA, sin lógica de negocio. Relaciones perezosas por defecto. No expongas entidades por la API.
- **Repository**: interfaz de Spring Data. Consultas derivadas por nombre cuando sean simples; `@Query` cuando no. Todas las consultas de datos de usuario filtran por `user_id`.
- **DTOs**: `record` separados para entrada (con Bean Validation: `@NotBlank`, `@Positive`, `@NotNull`...) y salida. Sin Lombok.
- **Service**: la lógica de negocio y `@Transactional`. Inyección por constructor.
- **Controller**: fino. Recibe el DTO validado (`@Valid`), llama al servicio y devuelve el DTO de salida con el código HTTP correcto (201 al crear, 204 al borrar, 404 si no existe o no es del usuario).

## 4. Errores
Excepciones propias de dominio y un `@RestControllerAdvice` que las convierte en `ProblemDetail`. No devuelvas trazas al cliente.

## 5. Seguridad
Un usuario solo puede ver y modificar sus propios datos: comprueba la pertenencia en el servicio, no solo en el controlador. Sesión con cookie HttpOnly. Si la API la consume un frontend en otro origen, configura CORS explícitamente y piensa en CSRF.

## 6. Reglas de negocio del dominio
- Se guarda solo `next_renewal`; las siguientes se calculan según el ciclo (mensual, anual...). Cuidado con meses de distinta duración (31 de enero + 1 mes) y con años bisiestos: ten tests para esos casos.
- Una suscripción en estado de prueba gratuita usa `trial_ends_on` para decidir cuándo avisar.
- El job diario de recordatorios debe ser **idempotente**: si se ejecuta dos veces el mismo día, no envía dos emails (apóyate en `UNIQUE (subscription_id, send_on)`).

## 7. Tests
- Tests de servicio con la lógica de fechas y ciclos (rápidos, sin base de datos).
- Tests de repositorio y de integración con **Testcontainers + PostgreSQL**, que ya están en `build.gradle.kts`, para que Flyway se ejecute de verdad contra una base real.
- Tests de controlador para códigos HTTP y validación.
- Spring Boot 4 movió paquetes de las anotaciones y utilidades de test: comprueba los imports con los starters de test que hay en `build.gradle.kts` en lugar de copiarlos de tutoriales de la rama 3.x.

## 8. Antes de terminar
- `./gradlew build` pasa (compila y tests).
- La app arranca y Flyway no da errores.
- Resume al usuario, en español, qué has creado y qué queda pendiente. Si algo se salía del MVP (ver `project-context`), dilo en lugar de hacerlo.
