package com.anfealta.ecommerce.ecomerce_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioRequest {

    @NotNull(message = "El ID del producto no puede ser nulo.")
    @Min(value = 1, message = "El ID del producto debe ser mayor o igual a 1.")
    private Long productoId; // El ID del producto al que se asocia este inventario

    @NotNull(message = "La cantidad disponible no puede ser nula.")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa.")
    private Integer cantidadDisponible;

    @NotNull(message = "La cantidad reservada no puede ser nula.")
    @Min(value = 0, message = "La cantidad reservada no puede ser negativa.")
    private Integer cantidadReservada; // Cantidad de stock que ya está asignada a órdenes pendientes

    @NotNull(message = "La cantidad mínima no puede ser nula.")
    @Min(value = 0, message = "La cantidad mínima no puede ser negativa.")
    private Integer cantidadMinima; // Umbral para alertas de bajo stock
}