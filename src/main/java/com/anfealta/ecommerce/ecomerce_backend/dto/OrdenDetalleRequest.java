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
public class OrdenDetalleRequest {

    @NotNull(message = "El ID del producto no puede ser nulo.")
    @Min(value = 1, message = "El ID del producto debe ser mayor o igual a 1.")
    private Long productoId; 

    @NotNull(message = "La cantidad no puede ser nula.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    private Integer cantidad; 
}