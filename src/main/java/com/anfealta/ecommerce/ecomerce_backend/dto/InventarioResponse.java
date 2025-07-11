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
    private Long productoId; // ID del producto asociado
    private String nombreProducto; // Opcional: para mostrar el nombre del producto directamente
    private String skuProducto; // Opcional: para mostrar el SKU del producto directamente
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer cantidadMinima;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
