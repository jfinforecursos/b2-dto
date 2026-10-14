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

import com.example.apibasica.model.Cliente;
import com.example.apibasica.service.ClienteService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Clientes", description = "Gestión de clientes y búsqueda por email")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    // Inyección de dependencias por constructor manual (sin @Autowired ni Lombok)
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // 1. Obtener todos los clientes (READ)
    @GetMapping
    public List<Cliente> obtenerTodos() {
        return clienteService.obtenerTodos();
    }

    // 2. Obtener un cliente por su ID (READ)
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(cliente);
    }

    // 3. Crear un nuevo cliente (CREATE)
    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.crear(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    // 4. Actualizar un cliente por su ID (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        Cliente clienteActualizado = clienteService.actualizar(id, cliente);
        return ResponseEntity.ok(clienteActualizado);
    }

    // 5. Eliminar un cliente por su ID (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: obtener cliente por email
    @GetMapping("/email/{email}")
    public ResponseEntity<Cliente> obtenerPorEmail(@PathVariable String email) {
        Cliente cliente = clienteService.obtenerPorEmail(email);
        return ResponseEntity.ok(cliente);
    }
}
