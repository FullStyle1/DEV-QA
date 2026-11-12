# DEV-QA: CRUD de productos

Microservicio REST para administrar productos con Java 21, Spring Boot, MongoDB y Maven. Incluye validacion de entradas, errores JSON, pruebas unitarias con Mockito, pruebas HTTP con MockMvc, cobertura JaCoCo y analisis estatico con SonarQube.

## Requisitos

- JDK 21
- Maven 3.9 o posterior
- Docker Desktop con Docker Compose

## Clonar y seleccionar la version completa

La rama `main` conserva deliberadamente el core inicial. El trabajo integrado de pruebas, JaCoCo y SonarQube se encuentra en `develop`:

```powershell
git clone https://github.com/FullStyle1/DEV-QA.git
Set-Location DEV-QA
git switch develop
```

## Iniciar MongoDB y la API

Desde la carpeta del proyecto:

```powershell
docker compose up -d mongodb
mvn spring-boot:run
```

La API escucha en `http://localhost:8080`. MongoDB utiliza `mongodb://localhost:27017/devqa` por defecto. Para cambiar la conexion, define `MONGODB_URI` antes de iniciar Spring Boot.

```powershell
$env:MONGODB_URI = "mongodb://localhost:27017/devqa"
mvn spring-boot:run
```

Deten la aplicacion con `Ctrl+C`. Para detener MongoDB sin borrar sus datos:

```powershell
docker compose stop mongodb
```

## Documentacion y cliente HTTP

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API REST

| Metodo | Ruta | Descripcion | Respuesta correcta |
|---|---|---|---|
| POST | `/api/productos` | Crear producto | `201 Created` |
| GET | `/api/productos` | Listar productos | `200 OK` |
| GET | `/api/productos/{id}` | Obtener por ID | `200 OK` |
| PUT | `/api/productos/{id}` | Reemplazar los datos del producto | `200 OK` |
| DELETE | `/api/productos/{id}` | Eliminar por ID | `204 No Content` |

Ejemplo de cuerpo JSON para POST y PUT:

```json
{
  "sku": "CAPTURA-001",
  "nombre": "Producto de prueba",
  "descripcion": "Evidencia CRUD",
  "precio": 99.90,
  "categoria": "Pruebas"
}
```

Todos los campos son obligatorios; el SKU no debe exceder 40 caracteres y debe ser unico; el nombre admite hasta 120 caracteres, la descripcion hasta 500 y la categoria hasta 80. El precio debe ser mayor que cero y admite hasta 10 digitos enteros y 2 decimales.

Los errores usan un cuerpo JSON con fecha, estado HTTP, descripcion, mensaje, ruta y detalles de validacion. Se devuelven `400 Bad Request` para datos o JSON invalidos, `404 Not Found` si el producto no existe y `409 Conflict` si el SKU ya esta registrado.

## Pruebas automatizadas

Las pruebas unitarias de servicio usan Mockito; las pruebas de endpoints usan MockMvc. No requieren una instancia de MongoDB porque el repositorio se reemplaza por un mock en las pruebas web.

Ejecuta todas las pruebas:

```powershell
mvn clean test
```

Para empaquetar y ejecutar tambien la verificacion de cobertura:

```powershell
mvn clean verify
```

Las pruebas actuales incluyen casos exitosos y de error para creacion, lectura, actualizacion, eliminacion, validacion, SKU duplicado y producto inexistente.

## Reporte de cobertura JaCoCo

`mvn clean verify` genera los reportes HTML y XML en `target/site/jacoco/`. Abre el reporte HTML localmente con:

```powershell
Start-Process target/site/jacoco/index.html
```

La compilacion falla si la cobertura del paquete no alcanza el 80% de lineas o de ramas. El reporte XML para SonarQube es `target/site/jacoco/jacoco.xml`.

## Analisis SonarQube

Inicia SonarQube Community localmente en Docker:

```powershell
docker run --name devqa-sonarqube --restart unless-stopped -d -p 9000:9000 -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true sonarqube:community
```

Espera a que `http://localhost:9000` termine su inicializacion. Inicia sesion con la cuenta local configurada, crea un token de analisis en **My Account > Security** y usalo desde una terminal. No guardes el token en el repositorio ni lo compartas:

```powershell
$env:SONAR_TOKEN = "PEGA_AQUI_TU_TOKEN"
mvn clean verify sonar:sonar '-Dsonar.qualitygate.wait=true'
Remove-Item Env:\SONAR_TOKEN
```

El proyecto usa la clave `FullStyle1_DEV-QA`, configurada en `pom.xml` y `sonar-project.properties`. El reporte JaCoCo se importa durante el analisis. El Quality Gate local `DEV-QA Quality Gate` verifica al menos 80% de cobertura de lineas y ramas, y cero bugs, vulnerabilidades y nuevas violaciones. Consulta el panel en `http://localhost:9000/dashboard?id=FullStyle1_DEV-QA`.

## Ramas de trabajo

- `main`: core inicial del microservicio.
- `develop`: integracion de las fases completadas.
- `feat/<nombre-corto>`: cada mejora se desarrolla en una rama desde `develop`, se verifica y luego se integra en `develop`.

`main` se actualiza al cierre del proyecto, despues de revisar y aprobar la integracion final.
