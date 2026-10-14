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

    // Inyección de dependencias por constructor
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // 1. Obtener todos los productos (convirtiendo cada Entidad a DTO de salida)
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // 2. Obtener un producto por ID
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
        return convertirADTO(producto);
    }

    // 3. Crear un nuevo producto a partir del DTO de entrada
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        validarDTO(dto);

        // Convertimos el DTO de entrada a la Entidad JPA
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(dto.nombre());
        nuevoProducto.setDescripcion(dto.descripcion());
        nuevoProducto.setPrecio(dto.precio());
        nuevoProducto.setStock(dto.stock());

        // Dato interno/confidencial que NUNCA viene en el DTO de entrada ni se expone en el DTO de salida
        nuevoProducto.setCostoProveedor(dto.precio() * 0.60); // Ejemplo: costo estimado interno del 60%

        // Guardamos en base de datos
        Producto guardado = productoRepository.save(nuevoProducto);

        // Retornamos el DTO de salida (el cliente NUNCA ve el costoProveedor)
        return convertirADTO(guardado);
    }

    // 4. Actualizar un producto existente
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

    // 5. Eliminar un producto
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    // Validaciones de reglas de negocio
    private void validarDTO(ProductoRequestDTO dto) {
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new ReglaDeNegocioException("El nombre del producto no puede estar vacío");
        }
        if (dto.precio() == null || dto.precio() <= 0) {
            throw new ReglaDeNegocioException("El precio del producto debe ser mayor a 0");
        }
        if (dto.stock() == null || dto.stock() < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser un número negativo");
        }
    }

    // Método auxiliar: convierte la Entidad JPA a DTO de salida
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

