/**
 * ============================================================================
 * GUÍA DIDÁCTICA: SERVICIO DE API CON FETCH
 * ============================================================================
 * 
 * En este archivo se concentran todas las llamadas HTTP a nuestro backend Spring Boot.
 * Conceptos clave que todo estudiante debe saber:
 * 
 * 1. fetch() es una función nativa de JavaScript para realizar peticiones HTTP.
 * 2. Devuelve una Promesa (Promise) que resolvemos con `async / await`.
 * 3. response.ok es true si el código HTTP está entre 200 y 299.
 * 4. Si el servidor responde con 400 (Bad Request) o 404 (Not Found), 
 *    nuestro GlobalExceptionHandler en Spring Boot devuelve un JSON con { mensaje: "..." }.
 *    Podemos leerlo con `await response.json()`.
 */

// Usamos la ruta relativa '/api' gracias al proxy configurado en vite.config.js,
// o directamente 'http://localhost:8080/api'
export const API_BASE = '/api';

// Callback opcional para mostrar a los estudiantes en pantalla qué fetch se ejecutó
let onFetchLog = null;
export function setLogCallback(callback) {
  onFetchLog = callback;
}

function logRequest(method, url, body, status, result) {
  if (onFetchLog) {
    onFetchLog({
      timestamp: new Date().toLocaleTimeString(),
      method,
      url,
      body,
      status,
      result
    });
  }
}

// ============================================================================
// 1. CLIENTES (CRUD y Búsqueda personalizada)
// ============================================================================

export const apiClientes = {

  // GET: Obtener todos los clientes
  async listar() {
    const url = `${API_BASE}/clientes`;
    const response = await fetch(url);
    const data = await response.json();
    logRequest('GET', url, null, response.status, data);

    if (!response.ok) {
      throw new Error(data.mensaje || 'Error al listar clientes');
    }
    return data;
  },

  // GET: Obtener cliente por ID
  async obtenerPorId(id) {
    const url = `${API_BASE}/clientes/${id}`;
    const response = await fetch(url);
    const data = await response.json();
    logRequest('GET', url, null, response.status, data);

    if (!response.ok) {
      throw new Error(data.mensaje || `No se encontró el cliente con ID ${id}`);
    }
    return data;
  },

  // GET: Método personalizado (Buscar por email)
  async obtenerPorEmail(email) {
    const url = `${API_BASE}/clientes/email/${encodeURIComponent(email)}`;
    const response = await fetch(url);
    const data = await response.json();
    logRequest('GET', url, null, response.status, data);

    if (!response.ok) {
      throw new Error(data.mensaje || `No se encontró cliente con email ${email}`);
    }
    return data;
  },

  // POST: Crear nuevo cliente
  async crear(cliente) {
    const url = `${API_BASE}/clientes`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json' // Indica a Spring Boot que el cuerpo es JSON
      },
      body: JSON.stringify(cliente) // Convertimos el objeto JavaScript a texto JSON
    });

    const data = await response.json();
    logRequest('POST', url, cliente, response.status, data);

    if (!response.ok) {
      // Si el email está duplicado, Spring lanzará ReglaDeNegocioException (400)
      throw new Error(data.mensaje || 'Error al crear el cliente');
    }
    return data;
  },

  // PUT: Actualizar cliente existente
  async actualizar(id, datosActualizados) {
    const url = `${API_BASE}/clientes/${id}`;
    const response = await fetch(url, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(datosActualizados)
    });

    const data = await response.json();
    logRequest('PUT', url, datosActualizados, response.status, data);

    if (!response.ok) {
      throw new Error(data.mensaje || 'Error al actualizar el cliente');
    }
    return data;
  },

  // DELETE: Eliminar cliente por ID
  async eliminar(id) {
    const url = `${API_BASE}/clientes/${id}`;
    const response = await fetch(url, {
      method: 'DELETE'
    });

    // Código 204 (No Content) no tiene cuerpo JSON
    let data = null;
    if (response.status !== 204) {
      data = await response.json();
    }
    logRequest('DELETE', url, null, response.status, data || { mensaje: 'Eliminado con éxito' });

    if (!response.ok) {
      throw new Error(data?.mensaje || 'Error al eliminar cliente');
    }
    return true;
  }
};

// ============================================================================
// 2. PEDIDOS (CRUD y Relación con Cliente)
// ============================================================================

export const apiPedidos = {

  // GET: Listar todos los pedidos
  async listar() {
    const url = `${API_BASE}/pedidos`;
    const response = await fetch(url);
    const data = await response.json();
    logRequest('GET', url, null, response.status, data);

    if (!response.ok) {
      throw new Error(data.mensaje || 'Error al listar pedidos');
    }
    return data;
  },

  // GET: Obtener pedidos de un cliente específico (Método personalizado)
  async obtenerPorCliente(clienteId) {
    const url = `${API_BASE}/pedidos/cliente/${clienteId}`;
    const response = await fetch(url);
    const data = await response.json();
    logRequest('GET', url, null, response.status, data);

    if (!response.ok) {
      throw new Error(data.mensaje || `Error al obtener pedidos del cliente ${clienteId}`);
    }
    return data;
  },

  // POST: Crear nuevo pedido asociado a un cliente
  async crear({ descripcion, total, clienteId }) {
    const url = `${API_BASE}/pedidos`;
    const payload = {
      descripcion,
      total: Number(total),
      cliente: clienteId ? { id: Number(clienteId) } : null
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    const data = await response.json();
    logRequest('POST', url, payload, response.status, data);

    if (!response.ok) {
      // Captura reglas de negocio: total <= 0, cliente inexistente, etc.
      throw new Error(data.mensaje || 'Error al crear el pedido');
    }
    return data;
  },

  // DELETE: Eliminar un pedido
  async eliminar(id) {
    const url = `${API_BASE}/pedidos/${id}`;
    const response = await fetch(url, {
      method: 'DELETE'
    });

    let data = null;
    if (response.status !== 204) {
      data = await response.json();
    }
    logRequest('DELETE', url, null, response.status, data || { mensaje: 'Pedido eliminado' });

    if (!response.ok) {
      throw new Error(data?.mensaje || 'Error al eliminar pedido');
    }
    return true;
  }
};
