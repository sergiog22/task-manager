# Task Manager API

API REST para administrar tareas, construida con Java 21, Spring Boot, Spring Security y PostgreSQL. Es un proyecto de muestra para explorar una API en capas, autenticación JWT, empaquetado Docker y un flujo CI/CD con GitHub Actions y GitHub Container Registry (GHCR).

## Funcionalidad

- Crear, consultar, actualizar y eliminar tareas.
- Listar tareas con paginación y ordenamiento.
- Autenticar usuarios existentes y emitir tokens JWT.
- Restringir las operaciones de tareas a usuarios autenticados con el rol `USER` o `ADMIN`. La prioridad `ALTA` requiere `ADMIN`.
- Publicar métricas y estado de salud mediante Spring Boot Actuator.

La aplicación no ofrece registro de usuarios. El usuario inicial se prepara en PostgreSQL usando [scripts/seed-base-user.sql](scripts/seed-base-user.sql); edita los valores de ejemplo antes de ejecutar el script y protege la contraseña como cualquier otro secreto.

## Requisitos

- Java 21 para ejecutar localmente.
- Docker con el plugin Docker Compose para ejecutar el contenedor.
- Una instancia PostgreSQL accesible desde la aplicación. Compose no crea ni administra un servidor PostgreSQL: solo ejecuta el servicio API.
- Un usuario y una base PostgreSQL con permisos para que Hibernate cree o actualice las tablas (`spring.jpa.hibernate.ddl-auto=update`).

## Configuración y ejecución local

1. Copia `.env.example` como `.env` y configura host, base, usuario y contraseña de PostgreSQL. Define también una clave JWT larga y aleatoria; no conserves los valores de ejemplo en un entorno real.
2. Para iniciar la app en el host, exporta las variables de `.env` a la sesión de shell y ejecuta:

	 ```bash
	 set -a
	 . ./.env
	 set +a
	 ./mvnw spring-boot:run
	 ```

3. Para iniciar la imagen publicada mediante Docker Compose:

	 ```bash
	 docker compose pull
	 docker compose up -d
	 docker compose logs -f task-manager
	 ```

	 Compose carga `.env` automáticamente. Para compilar la imagen localmente desde el código en lugar de descargar GHCR, ejecuta `docker build -t task-manager:local .` y cambia temporalmente `image` en `compose.yaml` a `task-manager:local`.

En desarrollo, la aplicación escucha en el puerto `8080` y usa el contexto `/task-manager`. Comprueba su estado en `http://localhost:8080/task-manager/actuator/health`.

## Variables de entorno

Compose y `application.properties` utilizan estas variables:

| Variable | Propósito |
| --- | --- |
| `DB_HOST` | Hostname PostgreSQL visible desde la app. |
| `DB_PORT` | Puerto PostgreSQL, normalmente `5432`. |
| `DB_NAME` | Nombre de la base de datos. |
| `DB_USERNAME` | Usuario de conexión PostgreSQL. |
| `DB_PASSWORD` | Contraseña PostgreSQL. |
| `SHOW_SQL` | Muestra SQL de Hibernate si se establece en `true`. |
| `CORS_ALLOWED_ORIGINS` | Orígenes web permitidos, separados según la configuración de la aplicación. Usa el origen real del frontend. |
| `JWT_SECRET` | Clave secreta para firmar JWT. Configura una clave fuerte y privada en despliegues. |
| `JWT_EXPIRATION_MINUTES` | Tiempo de vida del token en minutos; predeterminado: `60`. |

`spring.application.name` identifica la aplicación para Spring. `server.servlet.context-path=/task-manager` agrega el prefijo `/task-manager` a las rutas HTTP.

## API y autenticación

La URL base es `http://localhost:8080/task-manager` en desarrollo. En el despliegue de ejemplo es `http://160.34.219.68:8080/task-manager`. Las rutas indicadas en la tabla son relativas a esa base.

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | Autentica y devuelve un JWT. No requiere token. |
| GET | `/api/v1/tasks?page=0&size=20&sort=creadaEn,desc` | Lista tareas paginadas. |
| GET | `/api/v1/tasks/{id}` | Obtiene una tarea. |
| POST | `/api/v1/tasks` | Crea una tarea. |
| PUT | `/api/v1/tasks/{id}` | Actualiza una tarea. |
| DELETE | `/api/v1/tasks/{id}` | Elimina una tarea. |

El login recibe `username` y `password`. Su respuesta contiene `token`, `tokenType` (`Bearer`) y `expiresIn` en segundos. Envía el token como `Authorization: Bearer <token>` en las llamadas de tareas.

Ejemplo de login y listado con `curl` y `jq`:

```bash
BASE_URL=http://localhost:8080/task-manager

TOKEN=$(curl -fsS -X POST "$BASE_URL/api/v1/auth/login" \
	-H 'Content-Type: application/json' \
	-d '{"username":"task-admin","password":"REEMPLAZAR_POR_TU_PASSWORD"}' \
	| jq -r '.token')

curl -fsS "$BASE_URL/api/v1/tasks?page=0&size=20&sort=creadaEn,desc" \
	-H "Authorization: Bearer $TOKEN"
```

Ejemplo de creación:

```bash
curl -i -X POST "$BASE_URL/api/v1/tasks" \
	-H "Authorization: Bearer $TOKEN" \
	-H 'Content-Type: application/json' \
	-d '{
		"titulo": "Preparar entrega",
		"descripcion": "Revisar pendientes",
		"estado": "PENDIENTE",
		"prioridad": "MEDIA",
		"fechaLimite": "2026-10-30"
	}'
```

`titulo` es obligatorio (máximo 150 caracteres); `descripcion` acepta hasta 500. Los estados válidos son `PENDIENTE`, `EN_PROGRESO` y `COMPLETADA`; las prioridades son `BAJA`, `MEDIA` y `ALTA`. `fechaLimite` es opcional y usa formato `AAAA-MM-DD`. La API responde con detalles de validación y dominio en formato Problem Details (`application/problem+json`).

La documentación OpenAPI y Swagger UI están en `/v3/api-docs` y `/swagger-ui.html`, respectivamente, bajo la URL base. Actuator expone `/actuator/health`, `/actuator/info` y `/actuator/metrics`; el health check no requiere autenticación. Los demás endpoints no declarados explícitamente están denegados por Spring Security.

## Estructura del código

El código Java está bajo `src/main/java/com/serchcodev/task_manager`:

| Paquete | Responsabilidad |
| --- | --- |
| `auth/api` | Contrato HTTP para login y respuesta JWT. |
| `auth/application` | Carga de usuarios, validación y emisión/lectura de JWT. |
| `auth/domain` y `auth/infrastructure` | Entidad de usuario y acceso a usuarios en PostgreSQL. |
| `config` | Seguridad, CORS, propiedades y documentación OpenAPI. |
| `task/api` | Rutas REST, DTOs y conversión de errores HTTP. |
| `task/application` | Casos de uso, reglas de negocio y mapeo con MapStruct. |
| `task/domain` | Entidades, estados, prioridades, excepciones y puerto de persistencia. |
| `task/infrastructure` | Adaptador de persistencia basado en Spring Data JPA. |

Las capas de aplicación dependen del puerto del dominio; el adaptador de infraestructura implementa el acceso persistente. Al completar una tarea, el servicio publica `TaskCompletedEvent`, manteniendo desacoplada esa notificación de la operación principal.

Las pruebas viven en `src/test/java` y usan H2 en memoria para el slice de API y los casos de uso. Ejecutarlas no requiere una instancia PostgreSQL ni valida por sí solo el despliegue real sobre PostgreSQL.

## Docker

El [Dockerfile](Dockerfile) usa una construcción multi-stage:

1. La etapa `build` usa JDK 21, descarga dependencias Maven y empaqueta el JAR con `./mvnw clean package -DskipTests`.
2. La etapa final usa JRE 21, copia solo el JAR y arranca `java -jar app.jar`; el contenedor expone `8080`.

La etapa de imagen omite pruebas para no duplicar trabajo: el pipeline de CI las ejecuta antes de construir y publicar. [compose.yaml](compose.yaml) mapea `8080:8080`, carga configuración desde `.env` y descarga `ghcr.io/sergiog22/task-manager:latest`. La base PostgreSQL es un servicio independiente y debe ser accesible desde el contenedor. En un despliegue público, limita el acceso de red y usa HTTPS mediante un proxy inverso; no expongas credenciales o claves JWT en el repositorio.

## CI/CD con GitHub Actions y GHCR

El workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) configura el ciclo automatizado:

1. Se activa con eventos `push` y `pull_request`.
2. Descarga el repositorio, configura Java 21 con caché Maven y ejecuta `./mvnw verify` (compilación y pruebas).
3. Configura Docker Buildx y construye una imagen para `linux/arm64`.
4. Publica en GHCR las etiquetas `ghcr.io/sergiog22/task-manager:<commit-sha>` y `ghcr.io/sergiog22/task-manager:latest`.
5. Se conecta por SSH a la VM, descarga `latest` y ejecuta `docker compose up -d`.

El despliegue requiere el secreto `VM_SSH_KEY` configurado en GitHub Actions. El host y el usuario SSH están escritos directamente en el workflow; antes de reutilizarlo, muévelos a variables de repositorio o de entorno y restringe el acceso de la clave. La VM necesita Docker Compose, el archivo `.env` de producción y acceso a la base PostgreSQL. La imagen debe ser pública para que la VM pueda descargarla sin autenticación, o la VM debe iniciar sesión en GHCR.

Consideraciones para un pipeline de producción:

- El workflow actual también intenta publicar y desplegar en eventos `pull_request`. Conviene separar validación y publicación/despliegue, y permitir despliegue solo desde la rama protegida (por ejemplo, `main`).
- La VM debe ser compatible con ARM64, porque esa es la plataforma que construye el pipeline.
- `latest` es mutable. Para trazabilidad y rollback, despliega una etiqueta inmutable basada en el SHA del commit o un release.
- Usa secretos robustos, HTTPS y backups de PostgreSQL. El valor de respaldo de `JWT_SECRET` en `application.properties` es solo para desarrollo.

## Comandos de desarrollo

```bash
./mvnw test       # Ejecuta las pruebas
./mvnw verify     # Ejecuta las verificaciones del pipeline
docker build -t task-manager:local .
docker compose logs -f task-manager
```
