---
name: flyway-migrations
description: Crea, revisa y depura migraciones de Flyway para el esquema PostgreSQL del proyecto. Usa este skill siempre que haya que crear o cambiar tablas, columnas, índices o restricciones, que se mencionen migraciones, Flyway, "V1__init.sql", "flyway_schema_history", errores de checksum o de esquema no vacío, o cuando una entidad JPA no encaje con la base de datos y falle la validación de Hibernate.
---

# Migraciones con Flyway

## Dónde y cómo se nombran
- Carpeta exacta: `src/main/resources/db/migration/`.
- Nombre: `V<número>__<descripcion_en_snake_case>.sql` con **V mayúscula y dos guiones bajos**. Ej.: `V2__add_cancel_url_to_subscriptions.sql`.
- Si el nombre o la carpeta están mal, Flyway ignora el archivo **sin avisar**.
- El número es correlativo: mira cuál es la última versión antes de crear una nueva.

## Regla de oro
**Nunca edites una migración que ya se ha ejecutado.** El checksum dejaría de coincidir y Flyway no arrancará. Para cualquier cambio, crea una migración nueva (`ALTER TABLE ...`).

Excepción solo en desarrollo local, cuando solo existe la base de este usuario: se puede corregir la última migración reseteando la base (`docker compose down -v`, ver skill `database-setup`). Pide confirmación antes.

## Flujo para un cambio de esquema
1. Lee las migraciones existentes y las entidades JPA afectadas.
2. Crea el archivo `V<n+1>__...sql` con el cambio.
3. Actualiza la entidad JPA para que coincida con el esquema (Hibernate valida con `ddl-auto: validate`, no crea nada).
4. Arranca la app y confirma en el log que Flyway aplica la migración.
5. Ejecuta los tests.

## Convenciones de esquema
- Claves primarias: `BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`.
- Dinero: `NUMERIC(10,2)`. Moneda: `CHAR(3)` (código ISO, p. ej. `EUR`).
- Fechas de calendario: `DATE`. Momentos exactos: `TIMESTAMPTZ DEFAULT now()`.
- Enumerados (ciclo de facturación, estado) como `VARCHAR` con el nombre del enum de Java; valida en la app.
- Claves foráneas con `ON DELETE CASCADE` cuando el hijo no tiene sentido sin el padre.
- Crea índices en las columnas por las que se filtra (`user_id`, `next_renewal`).
- Restricciones `UNIQUE` para evitar duplicados peligrosos, como `reminders(subscription_id, send_on)`, para que el job diario no envíe el mismo aviso dos veces.
- Nombres de tablas y columnas en inglés y `snake_case`.

## Esquema base (V1)
Tablas: `users`, `subscriptions`, `reminders`. Si alguna cambia, la fuente de verdad es `V1__init.sql` y las migraciones posteriores, no esta lista.

## Dependencias necesarias
En `build.gradle.kts` debe haber el starter de Flyway del proyecto y `org.flywaydb:flyway-database-postgresql`. Sin la segunda, Flyway no soporta PostgreSQL en versiones recientes. Spring Boot ejecuta Flyway solo; no hace falta código.

## Errores típicos
| Error | Causa | Arreglo |
|---|---|---|
| No se ejecuta ninguna migración y no hay líneas de Flyway en el log | Falta la dependencia, `spring.flyway.enabled=false`, o carpeta/nombre mal | Revisar dependencias, configuración y ruta |
| `Found non-empty schema(s) without schema history table` | Hay tablas creadas a mano | Borrarlas o recrear la base |
| `Migration checksum mismatch` | Se editó una migración ya aplicada | Revertir el cambio; en local, resetear la base |
| `Detected failed migration` | Una migración falló a medias | Corregir el SQL; en local, resetear la base |
| `Schema-validation: missing table/column` | La entidad no coincide con el esquema | Corregir la entidad o añadir migración |
| Se aplica pero las tablas no se ven | Se mira otra base o hay que refrescar | Ver skill `database-setup` |
