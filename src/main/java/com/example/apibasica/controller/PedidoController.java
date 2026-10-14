package com.example.apibasica.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apibasica.model.Pedido;
import com.example.apibasica.service.PedidoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pedidos", description = "Gestión de pedidos y consulta por cliente")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    // Inyección de dependencias por constructor manual (sin @Autowired ni Lombok)
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // 1. Obtener todos los pedidos (READ)
    @GetMapping
    public List<Pedido> obtenerTodos() {
        return pedidoService.obtenerTodos();
    }

    // 2. Obtener un pedido por ID (READ)
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        return ResponseEntity.ok(pedido);
    }

    // 3. Crear un nuevo pedido asignado a un cliente (CREATE)
    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody Pedido pedido) {
        Pedido nuevoPedido = pedidoService.crear(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPedido);
    }

    // 4. Actualizar un pedido por ID (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizar(@PathVariable Long id, @RequestBody Pedido pedido) {
        Pedido pedidoActualizado = pedidoService.actualizar(id, pedido);
        return ResponseEntity.ok(pedidoActualizado);
    }

    // 5. Eliminar un pedido por ID (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: obtener todos los pedidos de un cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pedido>> obtenerPorClienteId(@PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.obtenerPorClienteId(clienteId);
        return ResponseEntity.ok(pedidos);
    }
}
