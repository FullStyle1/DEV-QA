package mx.edu.utch.devqa.producto.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        String id,
        String sku,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String categoria
) {
}
