package com.anfealta.ecommerce.ecomerce_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {

    @NotBlank(message = "El nombre del producto no puede estar vacío.")
    @Size(max = 255, message = "El nombre del producto no puede exceder los 255 caracteres.")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres.")
    private String descripcion;

    @NotBlank(message = "La categoría del producto no puede estar vacía.")
    @Size(max = 100, message = "La categoría no puede exceder los 100 caracteres.")
    private String categoria;

    @NotBlank(message = "El SKU del producto no puede estar vacío.")
    @Size(max = 50, message = "El SKU no puede exceder los 50 caracteres.")
    private String sku;

    @NotNull(message = "El precio del producto no puede ser nulo.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor que cero.")
    private BigDecimal precio;

    @NotNull(message = "El estado activo del producto no puede ser nulo.")
    private Boolean activo; 
}
