# Siembra de Datos (Data Seeding) en Spring Boot

Este tutorial explica paso a paso qué es una **Seed**, para qué sirve y cómo implementarla en Spring Boot de la manera más limpia, profesional y sencilla posible, tomando como referencia directa el código de este proyecto.

---

## 1. ¿Qué es una "Seed" o Siembra de Datos?

En el desarrollo de software, **sembrar datos (Data Seeding)** significa poblar la base de datos automáticamente con información de prueba inicial cuando la aplicación arranca.

### ¿Por qué es tan útil?
- **Ahorra tiempo:** Evita tener que abrir Postman o una interfaz visual para crear manualmente clientes y pedidos cada vez que inicias o reseteas la base de datos.
- **Pruebas inmediatas:** Nada más arrancar la API, ya puedes probar consultas (`GET`), filtros, actualizaciones (`PUT`) o eliminaciones (`DELETE`).
- **Consistencia:** Todos los desarrolladores del equipo tienen exactamente los mismos datos de prueba iniciales.

---

## 2. La Forma Recomendada en Spring Boot: `CommandLineRunner`

Spring Boot proporciona una interfaz especial llamada `CommandLineRunner`. Cualquier `@Bean` que devuelva un `CommandLineRunner` ejecutará su código **automáticamente una vez que la aplicación y la conexión a la base de datos estén listas**.

### Ventajas de este enfoque:
1. **100% Java y Orientado a Objetos:** Usas tus propias entidades (`Cliente`, `Pedido`) y repositorios (`save()`, `count()`).
2. **Validaciones automáticas:** Si cambias el nombre de un atributo en una entidad, el compilador te avisa de inmediato (a diferencia de un archivo `.sql` plano que fallaría en tiempo de ejecución).
3. **Independiente del motor de base de datos:** Funciona igual en PostgreSQL, MySQL, H2 u Oracle.

---

## 3. Implementación Paso a Paso

Todo el código de la seed se encuentra en el paquete de configuración:
`src/main/java/com/example/apibasica/config/DataSeederConfig.java`

```java
package com.example.apibasica.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.apibasica.model.Cliente;
import com.example.apibasica.model.Pedido;
import com.example.apibasica.repository.ClienteRepository;
import com.example.apibasica.repository.PedidoRepository;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner sembrarDatos(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        return args -> {
            // 1. Verificación para no duplicar datos
            if (clienteRepository.count() == 0) {

                // 2. Crear los clientes (Entidad Padre)
                Cliente cliente1 = new Cliente();
                cliente1.setNombre("Juan Pérez");
                cliente1.setEmail("juan.perez@example.com");
                cliente1.setTelefono("555-1234");

                Cliente cliente2 = new Cliente();
                cliente2.setNombre("María Gómez");
                cliente2.setEmail("maria.gomez@example.com");
                cliente2.setTelefono("555-5678");

                // Guardar los clientes primero para generar sus IDs
                clienteRepository.save(cliente1);
                clienteRepository.save(cliente2);

                // 3. Crear 5 pedidos para el Cliente 1 (Entidad Hija)
                Pedido p1 = new Pedido(null, "Laptop Lenovo ThinkPad", 850.0, cliente1);
                Pedido p2 = new Pedido(null, "Mouse inalámbrico Logitech", 25.5, cliente1);
                Pedido p3 = new Pedido(null, "Teclado mecánico Redragon", 65.0, cliente1);
                Pedido p4 = new Pedido(null, "Monitor 24 pulgadas LG", 180.0, cliente1);
                Pedido p5 = new Pedido(null, "Auriculares Bluetooth Sony", 45.0, cliente1);

                // 4. Crear 5 pedidos para el Cliente 2
                Pedido p6 = new Pedido(null, "Smartphone Samsung Galaxy", 600.0, cliente2);
                Pedido p7 = new Pedido(null, "Funda de cuero protectora", 15.0, cliente2);
                Pedido p8 = new Pedido(null, "Cargador rápido 45W", 30.0, cliente2);
                Pedido p9 = new Pedido(null, "Soporte metálico de escritorio", 20.0, cliente2);
                Pedido p10 = new Pedido(null, "Mochila para laptop", 50.0, cliente2);

                // 5. Guardar todos los pedidos en la base de datos
                pedidoRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));

                System.out.println(">>> SEED EXITOSA: Se han sembrado 2 clientes y 5 pedidos para cada uno.");
            }
        };
    }
}
```

---

## 4. Desglose Detallado del Código

### A. `@Configuration` y `@Bean`
- `@Configuration`: Le avisa a Spring que esta clase contiene definiciones de configuración y componentes.
- `@Bean`: Hace que el método retorne un objeto administrado por el ciclo de vida de Spring. Al ser de tipo `CommandLineRunner`, Spring lo ejecuta automáticamente tras el inicio.

### B. Inyección de Repositorios en los Parámetros del Método
```java
public CommandLineRunner sembrarDatos(ClienteRepository clienteRepository, PedidoRepository pedidoRepository)
```
Spring inyecta automáticamente los repositorios como parámetros del método sin necesidad de anotaciones adicionales ni variables globales.

### C. La Condición de Seguridad: `if (clienteRepository.count() == 0)`
Esta línea es fundamental:
- Si la base de datos está vacía (`count() == 0`), realiza la siembra.
- Si reinicias el servidor y la base de datos ya conserva los registros anteriores, **la condición evita duplicar los clientes y pedidos**.

### D. El Orden de Creación en Relaciones (Padre -> Hijo)
Al existir una relación de **Uno a Muchos (1:N)**:
1. **Primero se guarda el padre (`Cliente`):**
   ```java
   clienteRepository.save(cliente1);
   ```
   Al ejecutarse este `save()`, la base de datos asigna un `id` numérico (1, 2...).
2. **Luego se crean los hijos (`Pedido`) asignándoles la referencia del padre:**
   ```java
   new Pedido(null, "Laptop Lenovo", 850.0, cliente1);
   ```
   JPA toma el `id` del `cliente1` y lo coloca automáticamente en la columna `cliente_id` de la tabla `pedidos`.
3. **Finalmente se guardan los hijos:**
   Con `pedidoRepository.saveAll(...)` guardamos la lista completa de una sola vez.

---

## 5. Cómo Verificar que la Siembra Funcionó

### 1. Iniciar la aplicación
Ejecuta en tu terminal:
```bash
./mvnw spring-boot:run
```

### 2. Revisar la consola
Verás el mensaje impreso al final del arranque:
```text
>>> SEED EXITOSA: Se han sembrado 2 clientes y 5 pedidos para cada uno.
```

### 3. Probar los Endpoints en tu Navegador o Postman

* **Ver todos los clientes con sus pedidos embebidos:**
  `GET http://localhost:8080/api/clientes`
  *Respuesta:* Lista de los 2 clientes, donde cada uno contiene su arreglo `"pedidos"` con sus 5 pedidos.

* **Ver todos los pedidos:**
  `GET http://localhost:8080/api/pedidos`
  *Respuesta:* Lista de los 10 pedidos con la información de su respectivo cliente.

* **Ver solo los pedidos del Cliente 1 (Juan Pérez):**
  `GET http://localhost:8080/api/pedidos/cliente/1`
  *Respuesta:* Únicamente los 5 pedidos de Juan.

* **Ver solo los pedidos del Cliente 2 (María Gómez):**
  `GET http://localhost:8080/api/pedidos/cliente/2`
  *Respuesta:* Únicamente los 5 pedidos de María.

---

## 6. Buenas Prácticas para Principiantes

1. **Ubicación limpia:** Coloca siempre los seeders dentro de un paquete dedicado como `config` o `seed`, nunca en los controladores o servicios.
2. **Siempre verifica antes de insertar:** Usa `count() == 0` o `existsBy...()` para evitar errores por claves duplicadas al reiniciar la aplicación.
3. **Para ambientes de producción:** En proyectos reales, si no quieres que la seed se ejecute en producción, puedes agregar la anotación `@Profile("!prod")` o `@Profile("dev")` sobre la clase `@Configuration`. Así solo se ejecutará en entornos locales de desarrollo.
