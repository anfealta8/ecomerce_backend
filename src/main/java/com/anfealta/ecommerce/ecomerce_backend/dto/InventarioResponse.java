package com.anfealta.ecommerce.ecomerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponse {

    private Long id;
    private Long productoId; 
    private String nombreProducto; 
    private String skuProducto; 
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer cantidadMinima;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
