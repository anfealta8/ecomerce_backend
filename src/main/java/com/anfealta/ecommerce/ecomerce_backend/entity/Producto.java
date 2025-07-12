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
import java.util.List; 

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "productos") 
@EntityListeners(AuditingEntityListener.class) 
public class Producto {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false, length = 255) 
    private String nombre;

    @Column(length = 500) 
    private String descripcion;

    @Column(nullable = false, length = 100) 
    private String categoria;

    @Column(nullable = false, unique = true, length = 50) 
    private String sku; 

    @Column(nullable = false, precision = 10, scale = 2) 
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean activo;

    @CreatedDate
    @Column(nullable = false, updatable = false) 
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(nullable = false) // No nulo
    private LocalDateTime fechaActualizacion;

    
     @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<OrdenDetalle> detallesDeOrden;

     @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private Inventario inventario;
}
