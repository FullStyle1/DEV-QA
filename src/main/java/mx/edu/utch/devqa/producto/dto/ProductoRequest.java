package mx.edu.utch.devqa.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El SKU es obligatorio")
        @Size(max = 40, message = "El SKU no debe exceder 40 caracteres")
        String sku,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no debe exceder 120 caracteres")
        String nombre,
        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 500, message = "La descripcion no debe exceder 500 caracteres")
        String descripcion,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2,
                message = "El precio admite hasta 10 digitos enteros y 2 decimales")
        BigDecimal precio,
        @NotBlank(message = "La categoria es obligatoria")
        @Size(max = 80, message = "La categoria no debe exceder 80 caracteres")
        String categoria
) {
}
