# Manejo de Excepciones en Spring Boot

Este tutorial explica paso a paso cómo funciona el manejo de excepciones en una API REST básica, usando como referencia directa el código implementado en este proyecto (`Cliente`, `Pedido`, `ClienteService`, `PedidoService` y `GlobalExceptionHandler`).

---

## 1. ¿Por qué necesitamos manejar excepciones?

Cuando ocurre un error en una API (por ejemplo, buscar un usuario que no existe o enviar datos inválidos), si no manejamos el error, Spring Boot por defecto puede:
- Devolver un código de error genérico **500 Internal Server Error** (haciendo creer que el servidor se rompió).
- Exponer información interna del sistema (*stack trace*), lo cual es poco profesional e inseguro.

**El objetivo del manejo de excepciones es:**
1. Interceptar los errores en la lógica de negocio.
2. Traducirlos a un código de estado HTTP adecuado (`404 Not Found`, `400 Bad Request`, etc.).
3. Devolver un mensaje en formato JSON claro y amigable para el cliente (frontend, Postman, app móvil).

---

## 2. El Flujo de una Excepción en Spring Boot

```
[ Cliente / Postman ]
       │
       ▼ (1. Petición HTTP)
[ Controlador ]
       │
       ▼ (2. Llama al método)
[ Servicio ]  ──(3. Falla una condición: lanza la excepción)──► [ throw new ... ]
                                                                       │
                                                                       ▼
[ GlobalExceptionHandler ] ◄──────(4. Spring intercepta el error)──────┘
       │
       ▼ (5. Convierte a JSON + Código HTTP)
[ Respuesta limpia: 404 / 400 con {"mensaje": "..."} ]
```

---

## 3. Paso 1: Crear Excepciones Personalizadas

En Java, creamos clases que heredan de `RuntimeException` (excepciones no verificadas). No obligan a usar bloques `try/catch` molestos en cada línea de código.

### A. Para elementos no encontrados (HTTP 404)
Archivo: `src/main/java/com/example/apibasica/exception/RecursoNoEncontradoException.java`

```java
package com.example.apibasica.exception;

// Hereda de RuntimeException para poder lanzarse en cualquier punto
public class RecursoNoEncontradoException extends RuntimeException {

    // Recibe el mensaje descriptivo del error
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

### B. Para validaciones y reglas de negocio rotas (HTTP 400)
Archivo: `src/main/java/com/example/apibasica/exception/ReglaDeNegocioException.java`

```java
package com.example.apibasica.exception;

public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 4. Paso 2: Lanzar las Excepciones en el Servicio (`throw new ...`)

La capa de **Servicio** es donde vive la lógica. Aquí evaluamos las condiciones y, si algo no se cumple, disparamos la excepción con `throw new`.

### Ejemplo 1: Buscar por ID con `.orElseThrow()`
En `ClienteService.java`:
```java
public Cliente obtenerPorId(Long id) {
    return clienteRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + id));
}
```
*Si el cliente existe, lo devuelve; si no, lanza `RecursoNoEncontradoException` inmediatamente.*

### Ejemplo 2: Regla de negocio (Evitar correo duplicado)
En `ClienteService.java`:
```java
public Cliente crear(Cliente cliente) {
    // Verificamos si ya existe alguien con ese email
    if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
        throw new ReglaDeNegocioException("Ya existe un cliente con el email: " + cliente.getEmail());
    }
    return clienteRepository.save(cliente);
}
```

### Ejemplo 3: Validar relaciones entre entidades
En `PedidoService.java`:
```java
public Pedido crear(Pedido pedido) {
    // 1. Validar que venga el cliente
    if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
        throw new ReglaDeNegocioException("El pedido debe estar asociado a un cliente con ID válido");
    }
    
    // 2. Validar monto positivo
    if (pedido.getTotal() == null || pedido.getTotal() <= 0) {
        throw new ReglaDeNegocioException("El monto total del pedido debe ser mayor a 0");
    }

    // 3. Validar que el cliente realmente exista en la base de datos
    Cliente cliente = clienteRepository.findById(pedido.getCliente().getId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + pedido.getCliente().getId()));

    pedido.setCliente(cliente);
    return pedidoRepository.save(pedido);
}
```

> **Nota:** ¡El controlador se mantiene totalmente limpio! No necesita `if/else` ni bloques `try-catch`.

---

## 5. Paso 3: Capturar las Excepciones Globalmente

Usamos una clase especial con la anotación `@RestControllerAdvice`. Esta clase actúa como un "escudo" o "radar" que atrapa las excepciones lanzadas en cualquier parte de la aplicación.

Archivo: `src/main/java/com/example/apibasica/exception/GlobalExceptionHandler.java`

```java
package com.example.apibasica.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Atrapa cuando un recurso no existe y devuelve HTTP 404 NOT_FOUND
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    // 2. Atrapa cuando se viola una regla y devuelve HTTP 400 BAD_REQUEST
    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<Map<String, String>> manejarReglaDeNegocio(ReglaDeNegocioException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}
```

### ¿Qué significan estas anotaciones?
* `@RestControllerAdvice`: Le dice a Spring que esta clase interceptará excepciones ocurridas en cualquier `@RestController`.
* `@ExceptionHandler(MiExcepcion.class)`: Define qué método específico se encargará de resolver qué tipo de excepción.
* `ResponseEntity.status(...).body(...)`: Permite elegir exactamente el código de estado HTTP y el cuerpo JSON de respuesta.

---

## 6. Pruebas Reales: Peticiones y Respuestas

### Caso 1: Buscar un cliente que no existe
* **Petición:** `GET /api/clientes/999`
* **Código de respuesta:** `404 Not Found`
* **Cuerpo JSON:**
  ```json
  {
    "mensaje": "Cliente no encontrado con id: 999"
  }
  ```

### Caso 2: Registrar un cliente con email ya registrado
* **Petición:** `POST /api/clientes`
  ```json
  {
    "nombre": "Carlos",
    "email": "juan@ejemplo.com",
    "telefono": "123456"
  }
  ```
* **Código de respuesta:** `400 Bad Request`
* **Cuerpo JSON:**
  ```json
  {
    "mensaje": "Ya existe un cliente con el email: juan@ejemplo.com"
  }
  ```

### Caso 3: Crear pedido con total inválido o cliente inexistente
* **Petición:** `POST /api/pedidos`
  ```json
  {
    "descripcion": "Teclado mecánico",
    "total": -10.0,
    "cliente": { "id": 1 }
  }
  ```
* **Código de respuesta:** `400 Bad Request`
* **Cuerpo JSON:**
  ```json
  {
    "mensaje": "El monto total del pedido debe ser mayor a 0"
  }
  ```

---

