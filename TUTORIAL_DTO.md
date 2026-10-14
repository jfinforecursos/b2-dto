# Tutorial: Uso de DTOs (Data Transfer Objects) en Spring Boot

Este tutorial explica de manera clara, visual y sencilla qué es el patrón **DTO**, por qué es una de las mejores prácticas en el desarrollo de APIs profesionales y cómo implementarlo paso a paso usando la nueva entidad **`Producto`** con **DTO de entrada** y **DTO de salida**.

---

## 1. ¿Qué es un DTO (Data Transfer Object)?

Un **DTO (Objeto de Transferencia de Datos)** es una clase simple cuyo único propósito es transportar datos entre el cliente (frontend, móvil, Postman) y el servidor (tu API en Spring Boot).

### 🍔 Analogía de la vida real: El Restaurante
- **La Entidad JPA (`Producto`):** Es la **cocina y la bodega** del restaurante. Tiene los ingredientes crudos, el costo de compra al por mayor, la receta secreta y los utensilios internos.
- **El DTO de Entrada (`ProductoRequestDTO`):** Es la **comanda o pedido** que tú marcas en la mesa ("Quiero una hamburguesa con queso"). Solo pides lo necesario.
- **El DTO de Salida (`ProductoResponseDTO`):** Es el **plato servido en la mesa**. Te entregan la hamburguesa lista, bonita y limpia, sin que veas la factura del proveedor ni la cocina sucia.

---

## 2. ¿Por qué NO debemos usar directamente las Entidades JPA en los Controllers?

Cuando empezamos a programar APIs, es común usar la misma clase `@Entity` en los endpoints (`@RequestBody Producto producto`). Sin embargo, en proyectos reales esto causa grandes problemas:

| Problema al exponer la Entidad | ¿Por qué es peligroso? | ¿Cómo lo soluciona el DTO? |
| :--- | :--- | :--- |
| **Seguridad (*Mass Assignment*)** | Un atacante podría enviar en el JSON campos que no debería modificar (ej. `id: 999`, `saldo: 1000000` o roles de administrador). | El **DTO de entrada solo contiene los campos permitidos** que el usuario puede enviar. |
| **Bucle Infinito en JSON** | Al serializar relaciones bidireccionales (`Cliente` tiene `Pedidos` y `Pedido` tiene `Cliente`), Jackson entra en un bucle infinito y revienta la memoria. | El DTO rompe el ciclo y solo incluye la información exacta que se desea mostrar. |
| **Acoplamiento con la BD** | Si renombras una columna en tu tabla de base de datos, inmediatamente rompes la API y las aplicaciones móviles de tus clientes. | Los DTOs actúan como un **escudo**: la base de datos puede evolucionar sin afectar a los consumidores de la API. |
| **IDs innecesarios al crear** | Al hacer un `POST` para crear un recurso, la base de datos es la que debe generar el `id`. Pasar una entidad confunde al cliente sugiriendo que debe mandar un ID. | El DTO de entrada **no tiene campo ID**. |

---

## 3. Arquitectura Simple: DTO de Entrada vs DTO de Salida

Para mantener las cosas limpias y sencillas, dividimos el flujo en dos DTOs:

```
                         ┌──────────────────────────────────────────┐
                         │              CLIENTE (HTTP)              │
                         │      (Navegador, Postman, Frontend)      │
                         └──────────────────┬───────────────────────┘
                                            │
                                            │ 1. POST /api/productos con JSON
                                            │    { "nombre": "Teclado", "precio": 75.0, ... }
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │           ProductoRequestDTO             │ ◄─── DTO DE ENTRADA
                         │     (Solo campos que el usuario envía)    │      (¡Sin ID!)
                         └──────────────────┬───────────────────────┘
                                            │
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │            ProductoService               │
                         │  - Valida datos de negocio               │
                         │  - Convierte DTO ──► Entidad             │
                         └──────────────────┬───────────────────────┘
                                            │
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │           Producto (Entidad JPA)         │ ◄─── ENTIDAD
                         │     (Mapea a la tabla 'productos')       │      (Con ID generado)
                         └──────────────────┬───────────────────────┘
                                            │
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │           ProductoRepository             │ ◄─── BASE DE DATOS
                         │             (Guarda en BD)               │
                         └──────────────────┬───────────────────────┘
                                            │
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │            ProductoService               │
                         │  - Convierte Entidad ──► DTO de Salida   │
                         └──────────────────┬───────────────────────┘
                                            │
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │           ProductoResponseDTO            │ ◄─── DTO DE SALIDA
                         │        (Datos limpios + ID generado)      │      (¡Con ID!)
                         └──────────────────┬───────────────────────┘
                                            │
                                            │ 2. Retorna JSON HTTP 201 Created
                                            │    { "id": 1, "nombre": "Teclado", ... }
                                            ▼
                         ┌──────────────────────────────────────────┐
                         │              CLIENTE (HTTP)              │
                         └──────────────────────────────────────────┘
```

---

## 4. ¿Por qué usamos Java `record` para los DTOs?

A partir de **Java 16/17+**, Java introdujo los `record`. Un `record` es la forma más moderna, concisa y limpia de definir clases inmutables portadoras de datos:

- Genera automáticamente los getters (ej. `dto.nombre()`), constructor, `equals()`, `hashCode()` y `toString()`.
- Requiere solo **3 líneas de código**, sin necesidad de código repetitivo (*boilerplate*).
- Es 100% compatible con Spring Boot y serialización JSON (Jackson).

> **Nota:** Si en algún proyecto usas clases normales de Java, puedes lograr lo mismo creando una clase tradicional con getters/setters o usando las anotaciones de Lombok `@Getter` y `@Setter`.

---

## 5. Implementación Código por Código

### Paso 1: La Entidad de Base de Datos (`Producto.java`)
**Archivo:** `src/main/java/com/example/apibasica/model/Producto.java`

Representa la tabla en la base de datos con sus anotaciones JPA (`@Entity`, `@Id`, `@GeneratedValue`).

```java
package com.example.apibasica.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;

    // 🔒 DATO CONFIDENCIAL / INTERNO:
    // Este campo existe en la base de datos para la contabilidad interna de la empresa,
    // pero NUNCA debe ser visto por el cliente ni enviado en el JSON de la API.
    private Double costoProveedor;

    public Producto(Long id, String nombre, String descripcion, Double precio, Integer stock, Double costoProveedor) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.costoProveedor = costoProveedor;
    }
}
```

---

### Paso 2: Los DTOs (Entrada y Salida)

#### A. DTO de Entrada (`ProductoRequestDTO.java`)
**Archivo:** `src/main/java/com/example/apibasica/dto/ProductoRequestDTO.java`

Este es el objeto que el cliente nos envía para crear o actualizar un producto.
Observa que **NO incluye el atributo `id`**, porque el ID no lo decide el cliente, lo asigna la base de datos.

```java
package com.example.apibasica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de entrada para crear o actualizar un producto")
public record ProductoRequestDTO(
        @Schema(description = "Nombre del producto", example = "Teclado Mecánico RGB")
        String nombre,

        @Schema(description = "Descripción detallada del producto", example = "Teclado mecánico con switches táctiles")
        String descripcion,

        @Schema(description = "Precio unitario en USD", example = "79.99")
        Double precio,

        @Schema(description = "Cantidad disponible en inventario", example = "25")
        Integer stock
) {
}
```

#### B. DTO de Salida (`ProductoResponseDTO.java`)
**Archivo:** `src/main/java/com/example/apibasica/dto/ProductoResponseDTO.java`

Este es el objeto que la API responde. Aquí **SÍ incluimos el `id`** generado.

```java
package com.example.apibasica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de salida que la API retorna sobre un producto")
public record ProductoResponseDTO(
        @Schema(description = "Identificador único asignado por la base de datos", example = "1")
        Long id,

        @Schema(description = "Nombre del producto", example = "Teclado Mecánico RGB")
        String nombre,

        @Schema(description = "Descripción detallada del producto", example = "Teclado mecánico con switches táctiles")
        String descripcion,

        @Schema(description = "Precio unitario en USD", example = "79.99")
        Double precio,

        @Schema(description = "Cantidad disponible en inventario", example = "25")
        Integer stock
) {
}
```

#### 🛡️ ¿Qué dato ocultamos en los DTOs? El caso de `costoProveedor`

> **Observa la gran diferencia de seguridad:**
> - En la base de datos, la tabla `productos` tiene la columna `costo_proveedor` (lo que le cuesta a la tienda comprar el producto).
> - En **`ProductoRequestDTO`**: El cliente **NO puede enviar** un costo de proveedor ficticio.
> - En **`ProductoResponseDTO`**: El cliente **NUNCA ve** el margen de ganancia ni el costo del proveedor.
> 
> Si expusiéramos directamente la entidad JPA sin DTOs, ¡cualquier usuario podría ver nuestros costos confidenciales o hackear el precio de costo de compra!

---

### Paso 3: El Repositorio (`ProductoRepository.java`)
**Archivo:** `src/main/java/com/example/apibasica/repository/ProductoRepository.java`

El repositorio se comunica con la base de datos usando siempre la **Entidad JPA**.

```java
package com.example.apibasica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apibasica.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
```

---

### Paso 4: El Servicio (`ProductoService.java`)
**Archivo:** `src/main/java/com/example/apibasica/service/ProductoService.java`

El servicio es el **puente de transformación**:
1. Recibe el `ProductoRequestDTO`.
2. Valida las reglas de negocio (precios mayores a cero, texto no vacío).
3. Lo transforma en una entidad `Producto`, asigna datos internos (como `costoProveedor`) y lo guarda en el repositorio.
4. Toma la entidad guardada, la transforma en `ProductoResponseDTO` (excluyendo datos sensibles) y la retorna.

```java
package com.example.apibasica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.apibasica.dto.ProductoRequestDTO;
import com.example.apibasica.dto.ProductoResponseDTO;
import com.example.apibasica.exception.RecursoNoEncontradoException;
import com.example.apibasica.exception.ReglaDeNegocioException;
import com.example.apibasica.model.Producto;
import com.example.apibasica.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // 1. Obtener todos los productos (convierte cada Entidad a DTO de salida)
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // 2. Obtener por ID
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
        return convertirADTO(producto);
    }

    // 3. Crear nuevo producto a partir del DTO de entrada
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        validarDTO(dto);

        // Convertimos DTO de entrada a Entidad JPA
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(dto.nombre());
        nuevoProducto.setDescripcion(dto.descripcion());
        nuevoProducto.setPrecio(dto.precio());
        nuevoProducto.setStock(dto.stock());

        // Dato interno/confidencial que NUNCA viene en el DTO de entrada ni se expone en el DTO de salida
        nuevoProducto.setCostoProveedor(dto.precio() * 0.60); // Ejemplo: costo estimado interno del 60%

        // Guardamos en la base de datos
        Producto guardado = productoRepository.save(nuevoProducto);

        // Retornamos el DTO de salida (sin costoProveedor)
        return convertirADTO(guardado);
    }

    // 4. Actualizar producto existente
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        validarDTO(dto);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));

        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

    // 5. Eliminar producto
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    // Validación de negocio sencilla
    private void validarDTO(ProductoRequestDTO dto) {
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new ReglaDeNegocioException("El nombre del producto no puede estar vacío");
        }
        if (dto.precio() == null || dto.precio() <= 0) {
            throw new ReglaDeNegocioException("El precio del producto debe ser mayor a 0");
        }
        if (dto.stock() == null || dto.stock() < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser negativo");
        }
    }

    // Método de conversión manual: Entidad -> DTO de salida
    private ProductoResponseDTO convertirADTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock()
        );
    }
}
```

---

### Paso 5: El Controlador REST (`ProductoController.java`)
**Archivo:** `src/main/java/com/example/apibasica/controller/ProductoController.java`

Observa cómo el controlador **nunca expone ni toca la entidad `Producto`**. Solo conoce `ProductoRequestDTO` y `ProductoResponseDTO`.

```java
package com.example.apibasica.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.apibasica.dto.ProductoRequestDTO;
import com.example.apibasica.dto.ProductoResponseDTO;
import com.example.apibasica.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Productos", description = "Gestión de productos utilizando DTOs de entrada y salida")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    // Inyección de dependencias por constructor manual (sin Lombok)
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/productos
    @Operation(summary = "Obtener todos los productos")
    @GetMapping
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoService.obtenerTodos();
    }

    // GET /api/productos/{id}
    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // POST /api/productos
    @Operation(summary = "Crear un nuevo producto")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO nuevoProducto = productoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // PUT /api/productos/{id}
    @Operation(summary = "Actualizar un producto existente")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    // DELETE /api/productos/{id}
    @Operation(summary = "Eliminar un producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 6. Probando la API con DTOs

Puedes probar los nuevos endpoints en la interfaz visual de tu preferencia una vez iniciada la aplicación:
- **Scalar:** `http://localhost:8080/scalar`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

### 1. Petición POST (Crear Producto)
- **URL:** `http://localhost:8080/api/productos`
- **Método:** `POST`
- **Body enviado (ProductoRequestDTO - Sin ID):**
```json
{
  "nombre": "Micrófono USB Profesional",
  "descripcion": "Micrófono de condensador con patrón polar cardioide",
  "precio": 59.99,
  "stock": 15
}
```

- **Respuesta recibida (ProductoResponseDTO - Con ID):**
```json
{
  "id": 5,
  "nombre": "Micrófono USB Profesional",
  "descripcion": "Micrófono de condensador con patrón polar cardioide",
  "precio": 59.99,
  "stock": 15
}
```
**Código HTTP:** `201 Created`

---

### 2. Petición GET (Listar Productos)
- **URL:** `http://localhost:8080/api/productos`
- **Método:** `GET`
- **Respuesta:**
```json
[
  {
    "id": 1,
    "nombre": "Teclado Mecánico RGB",
    "descripcion": "Switches mecánicos táctiles, retroiluminación configurable",
    "precio": 75.0,
    "stock": 30
  },
  {
    "id": 2,
    "nombre": "Mouse Ergonómico Inalámbrico",
    "descripcion": "Sensor óptico de alta precisión con batería recargable",
    "precio": 35.0,
    "stock": 50
  }
]
```

---

### 3. Validación de Negocio (Manejo de Errores)
Si intentas enviar un precio negativo o nombre vacío:
```json
{
  "nombre": "",
  "descripcion": "Sin nombre",
  "precio": -10.0,
  "stock": 5
}
```
El `GlobalExceptionHandler` configurado en el proyecto responderá automáticamente con:
```json
{
  "error": "Regla de Negocio Incumplida",
  "mensaje": "El nombre del producto no puede estar vacío",
  "codigo": 400
}
```

---

## 7. Resumen: Cuándo usar cada objeto

```
┌─────────────────────────┬─────────────────────────┬─────────────────────────┐
│     Entidad JPA         │    DTO de Entrada       │     DTO de Salida       │
│     (Producto)          │  (ProductoRequestDTO)   │  (ProductoResponseDTO)  │
├─────────────────────────┼─────────────────────────┼─────────────────────────┤
│ • Conecta con la BD.    │ • Lo envía el usuario.  │ • Lo devuelve la API.   │
│ • Anotada con @Entity.  │ • Sin ID.               │ • Con ID.               │
│ • No viaja por la red.  │ • Solo campos de entrada│ • Solo datos públicos.  │
│ • Administra relaciones │ • Se valida en el       │ • Formato limpio listo  │
│   (FK, tablas).         │   Service.              │   para el frontend.     │
└─────────────────────────┴─────────────────────────┴─────────────────────────┘
```

