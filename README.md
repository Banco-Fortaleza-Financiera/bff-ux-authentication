# bff-ux-authentication

Proyecto Java 21 con Gradle Groovy, Spring Boot 3.x, Spring Cloud OpenFeign, OpenAPI Generator, pruebas con JUnit 5 y JaCoCo.

Este modulo contiene el esqueleto base del BFF de autenticacion y deja preparada la generacion de interfaces/modelos desde la especificacion OpenAPI.

## Requisitos

- Java 21
- Gradle Wrapper incluido en el proyecto
- Git para instalar el hook de pre-commit

## Como ejecutar

```sh
./gradlew bootRun
```

La aplicacion inicia en `http://localhost:8080`.

## Como correr pruebas

```sh
./gradlew test
```

Para ejecutar el build completo:

```sh
./gradlew clean build
```

## Cobertura JaCoCo

Generar reporte:

```sh
./gradlew jacocoTestReport
```

Ver el reporte HTML en:

```text
build/reports/jacoco/test/html/index.html
```

Validar cobertura minima del 80%:

```sh
./gradlew jacocoTestCoverageVerification
```

## Regenerar OpenAPI

La especificacion vive en:

```text
src/main/resources/openapi.yaml
```

Regenerar codigo:

```sh
./gradlew buildSpringServer
```

El codigo generado queda en `build/generated` y se agrega al `sourceSets.main`. No se mezcla con el codigo manual.

## Instalar pre-commit

```sh
./gradlew installGitHooks
```

El hook ejecuta:

```sh
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

## Arquitectura

La aplicacion sigue la organizacion base usada por los BFF del repositorio:

- `domain`: modelos y excepciones de dominio sin dependencias de Spring.
- `services`: casos de uso y logica de autenticacion.
- `controller`: adaptadores REST.
- `handler`: manejo de errores.
- `configuration`: configuracion Spring.
- `utils`: mappers y utilidades.

## Docker

Construir imagen:

```sh
docker build -t bff-openapi-ux-authentication .
```

Ejecutar contenedor:

```sh
docker run --rm -p 8080:8080 bff-openapi-ux-authentication
```
