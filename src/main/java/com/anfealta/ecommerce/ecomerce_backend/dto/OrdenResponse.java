package com.anfealta.ecommerce.ecomerce_backend.dto;

import com.anfealta.ecommerce.ecomerce_backend.entity.Orden; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponse {

    private Long id;
    private Long usuarioId;
    private String nombreUsuario; 
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Orden.EstadoOrden estado;
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal total;
    private List<OrdenDetalleResponse> detalles; 
}
