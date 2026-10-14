package com.example.apibasica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de Salida (Response DTO):
 * Representa los datos que la API devuelve al cliente cuando consulta o guarda un producto.
 * Incluye el 'id' asignado por la base de datos.
 */
@Schema(description = "Datos de salida que la API retorna sobre un producto")
public record ProductoResponseDTO(
        @Schema(description = "Identificador único asignado por la base de datos", example = "1")
        Long id,

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

