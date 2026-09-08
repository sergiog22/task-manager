# Task Manager API

API REST para administrar tareas, construida con Java 21 y Spring Boot.

## Ejecución local

1. Copia `.env.example` a `.env` y completa las credenciales de SQL Server. No subas este archivo al repositorio.
2. Exporta las variables o configúralas desde tu IDE: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `APP_SECURITY_USERNAME`, `APP_SECURITY_PASSWORD` y `APP_SECURITY_ROLES`.
3. Ejecuta `./mvnw spring-boot:run`.

También puedes levantar la aplicación con `docker compose up --build`; `compose.yaml` toma las variables desde `.env`.

## API

Todos los endpoints de negocio requieren HTTP Basic Authentication. El usuario predeterminado es solo útil para desarrollo; establece secretos fuertes en los entornos reales.

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/v1/tasks?page=0&size=20&sort=creadaEn,desc` | Lista paginada |
| GET | `/api/v1/tasks/{id}` | Obtiene una tarea |
| POST | `/api/v1/tasks` | Crea una tarea |
| PUT | `/api/v1/tasks/{id}` | Actualiza una tarea |
| DELETE | `/api/v1/tasks/{id}` | Elimina una tarea |

La documentación interactiva está en `/swagger-ui.html`; la especificación OpenAPI está en `/v3/api-docs`. Los checks operativos están disponibles en `/actuator/health`, `/actuator/info` y `/actuator/metrics`.

Las respuestas de validación y de dominio siguen RFC 7807 (`application/problem+json`). Las tareas de prioridad `ALTA` exigen el rol `ADMIN`.

## Arquitectura

`api` contiene el contrato HTTP; `application` implementa los casos de uso y el mapeo MapStruct; `domain` define entidades, eventos y el puerto de persistencia; `infrastructure` adapta ese puerto a Spring Data JPA. `TaskCompletedEvent` desacopla reacciones posteriores al cambio de estado.

## Calidad

Ejecuta `./mvnw test`. El pipeline de GitHub Actions ejecuta pruebas, verificación y construcción de la imagen Docker en cada push y pull request.
