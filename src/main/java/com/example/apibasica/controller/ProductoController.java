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

import com.example.apibasica.dto.ProductoRequestDTO;
import com.example.apibasica.dto.ProductoResponseDTO;
import com.example.apibasica.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Productos", description = "Gestión de productos utilizando DTOs de entrada y salida")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // 1. Obtener todos los productos (READ)
    @Operation(summary = "Obtener todos los productos", description = "Retorna la lista de productos como ProductoResponseDTO")
    @GetMapping
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoService.obtenerTodos();
    }

    // 2. Obtener un producto por ID (READ)
    @Operation(summary = "Obtener producto por ID", description = "Busca y retorna un producto en formato ProductoResponseDTO")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    // 3. Crear un nuevo producto (CREATE)
    @Operation(summary = "Crear un nuevo producto", description = "Recibe un ProductoRequestDTO (sin ID) y retorna el ProductoResponseDTO creado (con ID)")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO nuevoProducto = productoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // 4. Actualizar un producto (UPDATE)
    @Operation(summary = "Actualizar un producto", description = "Recibe el ID y los nuevos datos en ProductoRequestDTO para actualizar el producto")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO actualizado = productoService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    // 5. Eliminar un producto (DELETE)
    @Operation(summary = "Eliminar un producto", description = "Elimina un producto por su identificador único")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

