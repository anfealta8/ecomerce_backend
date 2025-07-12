package com.anfealta.ecommerce.ecomerce_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenRequest {

    @NotNull(message = "El ID del usuario no puede ser nulo.")
    @Min(value = 1, message = "El ID del usuario debe ser mayor o igual a 1.")
    private Long usuarioId; 

    @NotEmpty(message = "La orden debe contener al menos un detalle de producto.")
    @Size(min = 1, message = "Debe especificar al menos un producto para la orden.")
    @Valid 
    private List<OrdenDetalleRequest> detalles; 

    private Boolean aplicarDescuentoAleatorio = false;
}