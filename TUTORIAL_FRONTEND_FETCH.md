# Consumo de API REST con JavaScript Fetch y Vite

Esta guía explica paso a paso cómo consumir la API de Spring Boot (`Cliente` y `Pedido`) desde una aplicación web creada con **Vite**, usando únicamente **JavaScript nativo (`fetch`)** y **Vanilla CSS**, con un enfoque 100% didáctico y sin frameworks complejos.

---

## 1. ¿Cómo Iniciar la Aplicación Frontend?

Abre una terminal en la carpeta del frontend y ejecuta:

```bash
cd frontend
npm install
npm run dev
```

Vite iniciará el servidor de desarrollo en:
👉 **`http://localhost:5173`**

*(Asegúrate de que tu backend de Spring Boot esté corriendo simultáneamente en el puerto `8080`)*.

---

## 2. Conceptos Fundamentales de `fetch()`

`fetch()` es la herramienta nativa que traen todos los navegadores modernos para comunicarse con servidores web a través del protocolo HTTP.

### Reglas básicas que todo estudiante debe memorizar:
1. **Siempre es asíncrono:** Devuelve una `Promise`. Por eso usamos `async` y `await`.
2. **`response.ok`:** Es un booleano (`true` o `false`). Es `true` cuando el código de respuesta HTTP está entre `200` y `299`.
3. **Conversión a JSON:**
   - Para enviar datos al servidor: `JSON.stringify(objeto)` (convierte objeto JS a texto).
   - Para recibir datos del servidor: `await response.json()` (convierte texto recibido a objeto JS).
4. **Cabecera `Content-Type`:** Siempre que envíes un cuerpo en `POST` o `PUT`, debes indicar `'Content-Type': 'application/json'` para que Spring Boot entienda el contenido.

---

## 3. Ejemplos de Código: Las 4 Operaciones CRUD

Todo el código de comunicación se encuentra en el archivo:
📁 [`frontend/src/api.js`](file:///c:/Users/Jhon/Desktop/apibasica/frontend/src/api.js)

### A. GET: Obtener Información (READ)
```javascript
// Obtener todos los clientes
async function listarClientes() {
  const response = await fetch('/api/clientes');
  const data = await response.json();
  
  if (!response.ok) {
    throw new Error(data.mensaje || 'Error al listar clientes');
  }
  return data;
}

// Buscar cliente por Email (Método personalizado)
async function buscarPorEmail(email) {
  const response = await fetch(`/api/clientes/email/${encodeURIComponent(email)}`);
  const data = await response.json();
  
  if (!response.ok) {
    throw new Error(data.mensaje); // "Cliente no encontrado con email: ..."
  }
  return data;
}
```

---

### B. POST: Crear un Nuevo Registro (CREATE)
```javascript
async function crearCliente(nuevoCliente) {
  const response = await fetch('/api/clientes', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json' // Indicamos que el cuerpo es JSON
    },
    body: JSON.stringify(nuevoCliente) // Objeto JS -> Texto JSON
  });

  const data = await response.json();

  // Si el correo ya existía, Spring devuelve HTTP 400
  if (!response.ok) {
    throw new Error(data.mensaje); // "Ya existe un cliente con el email: ..."
  }
  return data;
}
```

---

### C. PUT: Actualizar Registro Existente (UPDATE)
```javascript
async function actualizarCliente(id, datosActualizados) {
  const response = await fetch(`/api/clientes/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(datosActualizados)
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.mensaje);
  }
  return data;
}
```

---

### D. DELETE: Eliminar Registro (DELETE)
```javascript
async function eliminarCliente(id) {
  const response = await fetch(`/api/clientes/${id}`, {
    method: 'DELETE'
  });

  // El código 204 No Content significa éxito pero sin cuerpo de respuesta
  if (!response.ok) {
    const data = await response.json();
    throw new Error(data.mensaje || 'Error al eliminar');
  }
  return true;
}
```

---

## 4. ¿Cómo se conectan los Errores de Spring Boot con el Frontend?

En nuestro backend creamos un `GlobalExceptionHandler` que devuelve:
```json
{
  "mensaje": "Ya existe un cliente con el email: juan.perez@example.com"
}
```

En el frontend capturamos este mensaje exactamente así:
```javascript
try {
  await apiClientes.crear({
    nombre: "Juan Falso",
    email: "juan.perez@example.com", // Email duplicado
    telefono: "111-2222"
  });
} catch (error) {
  // error.message contendrá el texto devuelto por Spring Boot:
  alert("Error del servidor: " + error.message);
}
```

---

## 5. El Problema del CORS y su Solución

Cuando el frontend corre en `http://localhost:5173` y el backend en `http://localhost:8080`, el navegador bloquea las peticiones por seguridad (**CORS**).

Para solucionar esto, se implementaron dos mecanismos:

1. **Proxy en Vite ([`vite.config.js`](file:///c:/Users/Jhon/Desktop/apibasica/frontend/vite.config.js)):**
   Cualquier petición que empiece con `/api` se redirige internamente a `http://localhost:8080/api`:
   ```javascript
   server: {
     proxy: {
       '/api': {
         target: 'http://localhost:8080',
         changeOrigin: true
       }
     }
   }
   ```

2. **Configuración en Spring Boot ([`CorsConfig.java`](file:///c:/Users/Jhon/Desktop/apibasica/src/main/java/com/example/apibasica/config/CorsConfig.java)):**
   Permite peticiones externas desde cualquier origen y método (`GET, POST, PUT, DELETE`).

---

## 6. Funcionalidades de la Aplicación Frontend

1. **Pestaña Clientes:**
   - Listado en tarjetas interactivas con conteo de pedidos.
   - Búsqueda en tiempo real por ID o por Email.
   - Modal para crear y editar clientes.
   - Eliminación con confirmación.
2. **Pestaña Pedidos:**
   - Listado de pedidos mostrando monto y cliente asociado.
   - Filtro dinámico por cliente.
   - Modal para registrar pedidos seleccionando el cliente desde un menú desplegable.
3. **Pestaña Laboratorio de Errores (Ideal para enseñar):**
   - Botones preparados para provocar errores 400 (email duplicado, monto negativo, pedido sin cliente) y 404 (ID inexistente).
4. **Inspector HTTP en Vivo (Barra inferior):**
   - Muestra en tiempo real qué método HTTP (`GET`, `POST`, etc.), qué URL, qué código de estado y qué respuesta JSON devolvió el servidor en cada clic.
