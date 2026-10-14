package com.example.apibasica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.apibasica.exception.RecursoNoEncontradoException;
import com.example.apibasica.exception.ReglaDeNegocioException;
import com.example.apibasica.model.Cliente;
import com.example.apibasica.model.Pedido;
import com.example.apibasica.repository.ClienteRepository;
import com.example.apibasica.repository.PedidoRepository;

@Service
public class PedidoService {

    // Inyección de dependencias por constructor manual (sin @Autowired ni Lombok)
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id: " + id));
    }

    public Pedido crear(Pedido pedido) {
        // Validación básica de regla de negocio
        if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
            throw new ReglaDeNegocioException("El pedido debe estar asociado a un cliente con ID válido");
        }
        if (pedido.getTotal() == null || pedido.getTotal() <= 0) {
            throw new ReglaDeNegocioException("El monto total del pedido debe ser mayor a 0");
        }

        // Verificar que el cliente exista en la base de datos
        Cliente cliente = clienteRepository.findById(pedido.getCliente().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + pedido.getCliente().getId()));

        pedido.setCliente(cliente);
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(Long id, Pedido datosPedido) {
        Pedido pedido = obtenerPorId(id);

        if (datosPedido.getTotal() != null && datosPedido.getTotal() <= 0) {
            throw new ReglaDeNegocioException("El monto total del pedido debe ser mayor a 0");
        }

        pedido.setDescripcion(datosPedido.getDescripcion());
        pedido.setTotal(datosPedido.getTotal());

        // Si se envió un cliente para reasignar el pedido
        if (datosPedido.getCliente() != null && datosPedido.getCliente().getId() != null) {
            Cliente nuevoCliente = clienteRepository.findById(datosPedido.getCliente().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + datosPedido.getCliente().getId()));
            pedido.setCliente(nuevoCliente);
        }

        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = obtenerPorId(id);
        pedidoRepository.delete(pedido);
    }

    // Método personalizado para consultar pedidos de un cliente
    public List<Pedido> obtenerPorClienteId(Long clienteId) {
        // Validar que el cliente exista
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con id: " + clienteId);
        }
        return pedidoRepository.findByClienteId(clienteId);
    }
}
