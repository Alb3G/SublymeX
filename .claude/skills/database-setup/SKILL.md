---
name: database-setup
description: Configura, arranca, revisa o reinicia la base de datos PostgreSQL local del proyecto con Docker Compose. Usa este skill siempre que el usuario hable de "set up database", "configurar la base de datos", postgres, docker compose, puertos, conexión a la BD, DBeaver, "no veo las tablas", "resetear la base de datos" o errores de conexión al arrancar la app, aunque no mencione Docker.
---

# Base de datos local

## Reglas del proyecto
- Motor: PostgreSQL 16 (fija la versión, no uses `latest`).
- Nombre de la base: `sublyx-db`. Usa **un único nombre** en `compose.yaml`, en la configuración y en DBeaver. Los desajustes de una letra ya causaron confusión una vez.
- Desde la máquina del usuario se entra por el puerto **5433** (mapeado al 5432 del contenedor).
- El esquema lo crea **solo Flyway** al arrancar la app. No crees tablas a mano. Mira el skill `flyway-migrations`.
- La contraseña no se escribe en el repositorio: se lee de `.env` o se usa un valor de desarrollo.

## `compose.yaml` de referencia (raíz del proyecto)
```yaml
services:
  postgres:
    image: 'postgres:16'
    environment:
      - 'POSTGRES_DB=sublyx-db'
      - 'POSTGRES_USER=postgres'
      - 'POSTGRES_PASSWORD=${DB_PASSWORD:-postgres}'
    ports:
      - '5433:5432'
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```
Cuidado con `ports`: un número suelto (`'5433'`) NO significa "puerto 5433 del host": Docker asigna un puerto aleatorio. Usa siempre `host:contenedor`.

## Configuración de Spring
`spring-boot-docker-compose` levanta el contenedor al arrancar la app y saca la URL, usuario y contraseña del `compose.yaml`, así que **no hace falta `spring.datasource.*`** en desarrollo. Si se quiere que la base siga viva al parar la app:
```yaml
spring:
  docker:
    compose:
      lifecycle-management: start-only
```

## Pasos para dejarlo funcionando
1. Comprueba qué hay corriendo: `docker ps`. Si hay otro Postgres ocupando el 5433, páralo o cambia el puerto.
2. Revisa `compose.yaml` contra la referencia de arriba.
3. Asegúrate de que `.env` está en `.gitignore`.
4. Arranca la app (`./gradlew bootRun`) y busca en el log líneas de Flyway ("Successfully applied ... migration").
5. Verifica con `docker compose exec postgres psql -U postgres -d sublyx-db -c '\dt'`: deben salir `users`, `subscriptions`, `reminders` y `flyway_schema_history`.

## Reiniciar la base desde cero (solo desarrollo)
`POSTGRES_DB` solo se aplica la primera vez que se crea el volumen, así que para cambiar el nombre o empezar limpio:
```
docker compose down -v
```
Y volver a arrancar la app. **`-v` borra los datos**: pide confirmación al usuario antes de ejecutarlo.

## Problemas típicos
- "No veo la tabla en DBeaver": refrescar con F5; comprobar que la conexión apunta a `localhost:5433` y a la base `sublyx-db`; comprobar que no está en modo commit manual.
- "Found non-empty schema(s) without schema history table": alguien creó tablas a mano. Borrarlas o recrear la base.
- La app conecta pero a otra base: hay dos Postgres o dos nombres distintos de base. Revisa `docker ps` y el nombre en `compose.yaml`.
- Connection refused: contenedor parado o puerto equivocado.
