package com.anfealta.ecommerce.ecomerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String sku;
    private BigDecimal precio;
    private Boolean activo;
    private LocalDateTime fechaCreacion; 
    private LocalDateTime fechaActualizacion; 
}