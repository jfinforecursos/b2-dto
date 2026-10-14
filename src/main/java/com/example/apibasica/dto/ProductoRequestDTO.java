package com.example.apibasica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de Entrada (Request DTO):
 * Representa los datos que el cliente envía para crear o actualizar un producto.
 * Nota: No incluye 'id' porque lo genera automáticamente la base de datos.
 */
@Schema(description = "Datos de entrada para crear o actualizar un producto")
public record ProductoRequestDTO(
        @Schema(description = "Nombre del producto", example = "Teclado Mecánico RGB")
        String nombre,

        @Schema(description = "Descripción detallada del producto", example = "Teclado mecánico con switches táctiles y retroiluminación")
        String descripcion,

        @Schema(description = "Precio unitario en USD", example = "79.99")
        Double precio,

        @Schema(description = "Cantidad disponible en inventario", example = "25")
        Integer stock
) {
}

