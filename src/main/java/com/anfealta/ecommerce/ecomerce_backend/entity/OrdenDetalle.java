package com.anfealta.ecommerce.ecomerce_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "orden_detalles") 
@EntityListeners(AuditingEntityListener.class) 
public class OrdenDetalle {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false) 
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false) 
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad; 

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario; 

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalLinea; 

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion; 

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion; 
}