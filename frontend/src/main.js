import { apiClientes, apiPedidos, setLogCallback } from './api.js';

// ============================================================================
// ELEMENTOS DEL DOM
// ============================================================================
const tabBtnClientes = document.getElementById('tab-btn-clientes');
const tabBtnPedidos = document.getElementById('tab-btn-pedidos');
const tabBtnLab = document.getElementById('tab-btn-lab');

const panelClientes = document.getElementById('panel-clientes');
const panelPedidos = document.getElementById('panel-pedidos');
const panelLab = document.getElementById('panel-lab');

const gridClientes = document.getElementById('grid-clientes');
const gridPedidos = document.getElementById('grid-pedidos');

// Buscador clientes
const inputBuscarCliente = document.getElementById('input-buscar-cliente');
const btnBuscarEmail = document.getElementById('btn-buscar-email');
const btnBuscarId = document.getElementById('btn-buscar-id');
const btnResetClientes = document.getElementById('btn-reset-clientes');

// Modales
const modalCliente = document.getElementById('modal-cliente');
const modalClienteTitulo = document.getElementById('modal-cliente-titulo');
const formCliente = document.getElementById('form-cliente');
const formClienteId = document.getElementById('form-cliente-id');
const formClienteNombre = document.getElementById('form-cliente-nombre');
const formClienteEmail = document.getElementById('form-cliente-email');
const formClienteTelefono = document.getElementById('form-cliente-telefono');
const btnModalCrearCliente = document.getElementById('btn-modal-crear-cliente');
const btnCerrarModalCliente = document.getElementById('btn-cerrar-modal-cliente');
const btnCancelarCliente = document.getElementById('btn-cancelar-cliente');

const modalPedido = document.getElementById('modal-pedido');
const formPedido = document.getElementById('form-pedido');
const formPedidoCliente = document.getElementById('form-pedido-cliente');
const formPedidoDesc = document.getElementById('form-pedido-desc');
const formPedidoTotal = document.getElementById('form-pedido-total');
const btnModalCrearPedido = document.getElementById('btn-modal-crear-pedido');
const btnCerrarModalPedido = document.getElementById('btn-cerrar-modal-pedido');
const btnCancelarPedido = document.getElementById('btn-cancelar-pedido');
const selectFiltroCliente = document.getElementById('select-filtro-cliente');
const btnResetPedidos = document.getElementById('btn-reset-pedidos');

// Inspector HTTP
const inspectorTimestamp = document.getElementById('inspector-timestamp');
const inspectorMethod = document.getElementById('inspector-method');
const inspectorUrl = document.getElementById('inspector-url');
const inspectorStatus = document.getElementById('inspector-status');
const inspectorResponse = document.getElementById('inspector-response');

// Toast
const toastContainer = document.getElementById('toast-container');

// Estado local
let clientesCache = [];

// ============================================================================
// INSPECTOR DIDÁCTICO DE PETICIONES FETCH
// ============================================================================
setLogCallback(({ timestamp, method, url, body, status, result }) => {
  inspectorTimestamp.textContent = timestamp;
  inspectorMethod.textContent = method;
  inspectorUrl.textContent = url;
  inspectorStatus.textContent = `${status} ${getStatusText(status)}`;

  // Color del status
  if (status >= 200 && status < 300) {
    inspectorStatus.className = 'status-badge status-2xx';
  } else {
    inspectorStatus.className = 'status-badge status-4xx';
  }

  const payloadText = body ? `// Cuerpo Enviado (Payload):\n${JSON.stringify(body, null, 2)}\n\n` : '';
  inspectorResponse.textContent = `${payloadText}// Respuesta Recibida:\n${JSON.stringify(result, null, 2)}`;
});

function getStatusText(status) {
  const map = {
    200: 'OK',
    201: 'Created',
    204: 'No Content',
    400: 'Bad Request',
    404: 'Not Found',
    500: 'Internal Server Error'
  };
  return map[status] || '';
}

function showToast(message, type = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  toastContainer.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 4000);
}

// ============================================================================
// GESTIÓN DE PESTAÑAS
// ============================================================================
function switchTab(activeTab, activePanel) {
  [tabBtnClientes, tabBtnPedidos, tabBtnLab].forEach(btn => btn.classList.remove('active'));
  [panelClientes, panelPedidos, panelLab].forEach(panel => panel.classList.remove('active'));

  activeTab.classList.add('active');
  activePanel.classList.add('active');
}

tabBtnClientes.addEventListener('click', () => switchTab(tabBtnClientes, panelClientes));
tabBtnPedidos.addEventListener('click', () => {
  switchTab(tabBtnPedidos, panelPedidos);
  cargarPedidos();
});
tabBtnLab.addEventListener('click', () => switchTab(tabBtnLab, panelLab));

// ============================================================================
// FUNCIONES DE CLIENTES
// ============================================================================

async function cargarClientes() {
  try {
    gridClientes.innerHTML = '<p style="color: var(--text-muted)">Cargando clientes con fetch()...</p>';
    const clientes = await apiClientes.listar();
    clientesCache = clientes;
    actualizarSelectsClientes(clientes);
    renderClientes(clientes);
  } catch (error) {
    gridClientes.innerHTML = `<p style="color: var(--accent-danger)">Error: ${error.message}</p>`;
    showToast(error.message, 'error');
  }
}

function renderClientes(lista) {
  gridClientes.innerHTML = '';
  if (!lista || lista.length === 0) {
    gridClientes.innerHTML = '<p style="color: var(--text-muted)">No hay clientes para mostrar.</p>';
    return;
  }

  lista.forEach(cliente => {
    const card = document.createElement('div');
    card.className = 'card';
    const totalPedidos = cliente.pedidos ? cliente.pedidos.length : 0;

    card.innerHTML = `
      <div class="card-header">
        <div>
          <span class="card-title">${cliente.nombre}</span>
          <div style="font-size: 0.75rem; color: var(--text-muted)">ID: #${cliente.id}</div>
        </div>
        <span class="card-badge">📦 ${totalPedidos} pedidos</span>
      </div>
      <div class="card-body">
        <span>📧 <strong>Email:</strong> ${cliente.email}</span>
        <span>📞 <strong>Teléfono:</strong> ${cliente.telefono}</span>
      </div>
      <div class="card-actions">
        <button class="btn btn-secondary btn-sm btn-ver-pedidos" data-id="${cliente.id}">Ver Pedidos</button>
        <button class="btn btn-secondary btn-sm btn-editar-cliente" data-id="${cliente.id}">Editar</button>
        <button class="btn btn-danger btn-sm btn-eliminar-cliente" data-id="${cliente.id}">Eliminar</button>
      </div>
    `;

    // Eventos de botones
    card.querySelector('.btn-ver-pedidos').addEventListener('click', () => {
      selectFiltroCliente.value = cliente.id;
      switchTab(tabBtnPedidos, panelPedidos);
      filtrarPedidosPorCliente(cliente.id);
    });

    card.querySelector('.btn-editar-cliente').addEventListener('click', () => {
      abrirModalEditarCliente(cliente);
    });

    card.querySelector('.btn-eliminar-cliente').addEventListener('click', () => {
      eliminarClienteConfirmado(cliente.id, cliente.nombre);
    });

    gridClientes.appendChild(card);
  });
}

function actualizarSelectsClientes(clientes) {
  // Select filtro de pedidos
  selectFiltroCliente.innerHTML = '<option value="">-- Todos los Pedidos --</option>';
  // Select en modal de crear pedido
  formPedidoCliente.innerHTML = '<option value="">-- Elige un cliente --</option>';

  clientes.forEach(c => {
    const optFiltro = new Option(`${c.nombre} (ID: ${c.id})`, c.id);
    const optModal = new Option(`${c.nombre} (${c.email})`, c.id);
    selectFiltroCliente.add(optFiltro);
    formPedidoCliente.add(optModal);
  });
}

// Búsqueda por Email
btnBuscarEmail.addEventListener('click', async () => {
  const query = inputBuscarCliente.value.trim();
  if (!query) {
    showToast('Ingresa un email para buscar', 'error');
    return;
  }
  try {
    const cliente = await apiClientes.obtenerPorEmail(query);
    renderClientes([cliente]);
    showToast(`Cliente encontrado: ${cliente.nombre}`);
  } catch (error) {
    showToast(error.message, 'error');
  }
});

// Búsqueda por ID
btnBuscarId.addEventListener('click', async () => {
  const query = inputBuscarCliente.value.trim();
  if (!query || isNaN(query)) {
    showToast('Ingresa un ID numérico válido', 'error');
    return;
  }
  try {
    const cliente = await apiClientes.obtenerPorId(query);
    renderClientes([cliente]);
    showToast(`Cliente encontrado con ID ${cliente.id}`);
  } catch (error) {
    showToast(error.message, 'error');
  }
});

btnResetClientes.addEventListener('click', () => {
  inputBuscarCliente.value = '';
  cargarClientes();
});

// Modal Cliente
btnModalCrearCliente.addEventListener('click', () => {
  formCliente.reset();
  formClienteId.value = '';
  modalClienteTitulo.textContent = 'Nuevo Cliente';
  modalCliente.classList.add('active');
});

function abrirModalEditarCliente(cliente) {
  formClienteId.value = cliente.id;
  formClienteNombre.value = cliente.nombre;
  formClienteEmail.value = cliente.email;
  formClienteTelefono.value = cliente.telefono;
  modalClienteTitulo.textContent = `Editar Cliente #${cliente.id}`;
  modalCliente.classList.add('active');
}

btnCerrarModalCliente.addEventListener('click', () => modalCliente.classList.remove('active'));
btnCancelarCliente.addEventListener('click', () => modalCliente.classList.remove('active'));

formCliente.addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = formClienteId.value;
  const datos = {
    nombre: formClienteNombre.value.trim(),
    email: formClienteEmail.value.trim(),
    telefono: formClienteTelefono.value.trim()
  };

  try {
    if (id) {
      await apiClientes.actualizar(id, datos);
      showToast('Cliente actualizado correctamente');
    } else {
      await apiClientes.crear(datos);
      showToast('Cliente creado exitosamente');
    }
    modalCliente.classList.remove('active');
    cargarClientes();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

async function eliminarClienteConfirmado(id, nombre) {
  if (confirm(`¿Estás seguro de eliminar al cliente "${nombre}"? Sus pedidos asociados también serán eliminados.`)) {
    try {
      await apiClientes.eliminar(id);
      showToast('Cliente eliminado con éxito');
      cargarClientes();
    } catch (error) {
      showToast(error.message, 'error');
    }
  }
}

// ============================================================================
// FUNCIONES DE PEDIDOS
// ============================================================================

async function cargarPedidos() {
  try {
    gridPedidos.innerHTML = '<p style="color: var(--text-muted)">Cargando pedidos con fetch()...</p>';
    const pedidos = await apiPedidos.listar();
    renderPedidos(pedidos);
  } catch (error) {
    gridPedidos.innerHTML = `<p style="color: var(--accent-danger)">Error: ${error.message}</p>`;
    showToast(error.message, 'error');
  }
}

async function filtrarPedidosPorCliente(clienteId) {
  if (!clienteId) {
    cargarPedidos();
    return;
  }
  try {
    gridPedidos.innerHTML = '<p style="color: var(--text-muted)">Filtrando pedidos del cliente...</p>';
    const pedidos = await apiPedidos.obtenerPorCliente(clienteId);
    renderPedidos(pedidos);
    showToast(`Mostrando ${pedidos.length} pedidos del cliente seleccionado`);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

selectFiltroCliente.addEventListener('change', (e) => {
  filtrarPedidosPorCliente(e.target.value);
});

btnResetPedidos.addEventListener('click', () => {
  selectFiltroCliente.value = '';
  cargarPedidos();
});

function renderPedidos(lista) {
  gridPedidos.innerHTML = '';
  if (!lista || lista.length === 0) {
    gridPedidos.innerHTML = '<p style="color: var(--text-muted)">No hay pedidos disponibles.</p>';
    return;
  }

  lista.forEach(pedido => {
    const card = document.createElement('div');
    card.className = 'card';
    const nombreCliente = pedido.cliente ? pedido.cliente.nombre : 'Sin asignar';
    const idCliente = pedido.cliente ? pedido.cliente.id : null;

    card.innerHTML = `
      <div class="card-header">
        <div>
          <span class="card-title">${pedido.descripcion}</span>
          <div style="font-size: 0.75rem; color: var(--text-muted)">Pedido ID: #${pedido.id}</div>
        </div>
        <span class="card-badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399;">
          $${Number(pedido.total).toFixed(2)}
        </span>
      </div>
      <div class="card-body">
        <span>👤 <strong>Cliente:</strong> ${nombreCliente} (ID: ${idCliente})</span>
      </div>
      <div class="card-actions">
        <button class="btn btn-danger btn-sm btn-eliminar-pedido" data-id="${pedido.id}">Eliminar Pedido</button>
      </div>
    `;

    card.querySelector('.btn-eliminar-pedido').addEventListener('click', () => {
      eliminarPedidoConfirmado(pedido.id, pedido.descripcion);
    });

    gridPedidos.appendChild(card);
  });
}

// Modal Pedido
btnModalCrearPedido.addEventListener('click', () => {
  formPedido.reset();
  modalPedido.classList.add('active');
});

btnCerrarModalPedido.addEventListener('click', () => modalPedido.classList.remove('active'));
btnCancelarPedido.addEventListener('click', () => modalPedido.classList.remove('active'));

formPedido.addEventListener('submit', async (e) => {
  e.preventDefault();
  const clienteId = formPedidoCliente.value;
  const descripcion = formPedidoDesc.value.trim();
  const total = formPedidoTotal.value;

  try {
    await apiPedidos.crear({ descripcion, total, clienteId });
    showToast('Pedido registrado con éxito');
    modalPedido.classList.remove('active');
    cargarPedidos();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

async function eliminarPedidoConfirmado(id, desc) {
  if (confirm(`¿Deseas eliminar el pedido "${desc}" (#${id})?`)) {
    try {
      await apiPedidos.eliminar(id);
      showToast('Pedido eliminado correctamente');
      cargarPedidos();
    } catch (error) {
      showToast(error.message, 'error');
    }
  }
}

// ============================================================================
// LABORATORIO DE ERRORES (PRUEBAS EDUCATIVAS PARA ESTUDIANTES)
// ============================================================================

document.getElementById('btn-lab-email-duplicado').addEventListener('click', async () => {
  try {
    await apiClientes.crear({
      nombre: 'Usuario Prueba Duplicado',
      email: 'juan.perez@example.com',
      telefono: '111-2222'
    });
  } catch (err) {
    showToast(`Capturado HTTP 400: ${err.message}`, 'error');
  }
});

document.getElementById('btn-lab-monto-negativo').addEventListener('click', async () => {
  try {
    await apiPedidos.crear({
      descripcion: 'Compra Inválida',
      total: -99.99,
      clienteId: 1
    });
  } catch (err) {
    showToast(`Capturado HTTP 400: ${err.message}`, 'error');
  }
});

document.getElementById('btn-lab-sin-cliente').addEventListener('click', async () => {
  try {
    await apiPedidos.crear({
      descripcion: 'Pedido Huérfano',
      total: 50.0,
      clienteId: null
    });
  } catch (err) {
    showToast(`Capturado HTTP 400: ${err.message}`, 'error');
  }
});

document.getElementById('btn-lab-cliente-404').addEventListener('click', async () => {
  try {
    await apiClientes.obtenerPorId(9999);
  } catch (err) {
    showToast(`Capturado HTTP 404: ${err.message}`, 'error');
  }
});

document.getElementById('btn-lab-pedido-404').addEventListener('click', async () => {
  try {
    const res = await fetch('/api/pedidos/9999');
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.mensaje || 'Pedido no encontrado');
    }
  } catch (err) {
    showToast(`Capturado HTTP 404: ${err.message}`, 'error');
  }
});

// ============================================================================
// INICIALIZACIÓN
// ============================================================================
cargarClientes();
