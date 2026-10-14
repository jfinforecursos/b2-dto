# Documentación de APIs con OpenAPI, Swagger y Scalar en Spring Boot

Este tutorial explica paso a paso qué son **OpenAPI**, **Swagger UI** y **Scalar**, cómo configurarlos en Spring Boot dentro de la capa `config` y cómo visualizar la documentación interactiva de tus endpoints.

---

## 1. Conceptos Fundamentales: OpenAPI vs Swagger vs Scalar

Para los principiantes, estos tres términos suelen confundirse:

| Término | ¿Qué es? | Rol en la aplicación |
| :--- | :--- | :--- |
| **OpenAPI** | Es una **especificación estándar** (un archivo JSON o YAML). | Define formalmente qué endpoints existen, qué parámetros reciben y qué respuestas devuelven (por defecto en `/v3/api-docs`). |
| **Swagger UI** | Es un **visor web clásico**. | Lee el JSON de OpenAPI y dibuja una página web interactiva para probar los endpoints (`/swagger-ui.html`). |
| **Scalar** | Es un **visor web moderno y elegante**. | Una alternativa de última generación a Swagger UI: más rápida, con cliente HTTP integrado para probar peticiones, temas claros/oscuros y generación de código de ejemplo en cURL, Python, JS, etc. (`/scalar`). |

> **En resumen:** `springdoc-openapi` genera la información de tus endpoints (la especificación), mientras que **Swagger** y **Scalar** son los rostros visuales para leerla e interactuar con ella.

---

## 2. Paso 1: Agregar las Dependencias en `pom.xml`

En el archivo `pom.xml` agregamos `springdoc-openapi` y el módulo oficial de `scalar-webmvc`:

```xml
<!-- Generador de especificación OpenAPI 3 y Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>

<!-- Visor moderno Scalar para Spring WebMVC -->
<dependency>
    <groupId>com.scalar.maven</groupId>
    <artifactId>scalar-webmvc</artifactId>
    <version>0.6.74</version>
</dependency>
```

---

## 3. Paso 2: Crear la Configuración en `config/OpenApiConfig.java`

Para darle información personalizada a nuestra API (título, versión, descripción, contacto), creamos una clase con `@Configuration` que expone un `@Bean` de tipo `OpenAPI`.

Archivo: `src/main/java/com/example/apibasica/config/OpenApiConfig.java`

```java
package com.example.apibasica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Básica de Clientes y Pedidos")
                        .version("1.0.0")
                        .description("API REST para la gestión de Clientes y sus Pedidos asociados (Relación 1:N), con manejo global de excepciones, inyección por constructor y siembra de datos.")
                        .contact(new Contact()
                                .name("Soporte Técnico")
                                .email("contacto@ejemplo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

### ¿Qué hace este código?
- Spring detecta el bean `OpenAPI` y lo fusiona automáticamente con la inspección de tus controladores.
- La información de autoría y descripción aparecerá destacada en la cabecera tanto de Swagger como de Scalar.

---

## 4. Paso 3: Configurar `application.properties`

En `src/main/resources/application.properties` enlazamos las rutas y activamos Scalar:

```properties
# Configuración de OpenAPI (SpringDoc)
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Configuración de Scalar
scalar.enabled=true
scalar.url=/v3/api-docs
scalar.path=/scalar
```

- `scalar.enabled=true`: Habilita la interfaz de Scalar.
- `scalar.url=/v3/api-docs`: Le indica a Scalar de dónde debe leer la definición de los endpoints.
- `scalar.path=/scalar`: Define en qué URL del navegador estará disponible la documentación.

---

## 5. Paso 4: Organizar los Endpoints con `@Tag`

Para que la documentación se vea ordenada por módulos, usamos la anotación `@Tag` sobre los controladores:

### En `ClienteController.java`:
```java
@Tag(name = "Clientes", description = "Gestión de clientes y búsqueda por email")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    // ...
}
```

### En `PedidoController.java`:
```java
@Tag(name = "Pedidos", description = "Gestión de pedidos y consulta por cliente")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    // ...
}
```

Gracias a `@Tag`, tanto Swagger como Scalar agrupan todos los endpoints de clientes bajo una sección y todos los de pedidos bajo otra.

---

## 6. Paso 5: Cómo Probar y Ver la Documentación

Una vez que inicias tu proyecto con:
```bash
./mvnw spring-boot:run
```

Abre tu navegador y explora las siguientes URLs:

### 1. Documentación con Scalar (Recomendado)
👉 **`http://localhost:8080/scalar`**

**Lo que encontrarás en Scalar:**
- **Diseño Ultra-Moderno:** Conmutador de modo oscuro y claro.
- **Cliente HTTP Integrado ("Test Request"):** Puedes ingresar valores y probar directamente peticiones `GET`, `POST`, `PUT`, `DELETE` como si estuvieras en Postman.
- **Generador de Código Multilenguaje:** Muestra cómo consumir cada endpoint en cURL, JavaScript (Fetch / Axios), Python (Requests), Java, Go, etc.
- **Buscador Rápido:** Presiona `Ctrl + K` (o `Cmd + K`) para buscar cualquier endpoint al instante.

### 2. Documentación Clásica con Swagger UI
👉 **`http://localhost:8080/swagger-ui.html`**
- La vista tradicional de Swagger con botones "Try it out".

### 3. Especificación OpenAPI en Bruto (JSON)
👉 **`http://localhost:8080/v3/api-docs`**
- El JSON puro que contiene toda la estructura técnica que consumen Swagger, Scalar y herramientas como Insomnia o Postman.

---

## 7. Comparativa: Swagger UI vs Scalar

| Característica | Swagger UI | Scalar |
| :--- | :--- | :--- |
| **Estética Visual** | Básica / Clásica | Moderna / Minimalista |
| **Modo Oscuro** | No nativo | Nativo (Dark/Light toggle) |
| **Generación de Código** | Limitada | Ejemplos en +10 lenguajes |
| **Buscador Rápido (Cmd+K)** | No | Sí |
| **Facilidad de Integración** | Automática | Automática vía `scalar-webmvc` |
| **Cliente de Pruebas** | Básico | Completo estilo cliente REST |
