package com.anfealta.ecommerce.ecomerce_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "inventarios") 
@EntityListeners(AuditingEntityListener.class) 
public class Inventario {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true) 
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidadDisponible; 

    @Column(nullable = false)
    private Integer cantidadReservada = 0; 

    @Column(nullable = false)
    private Integer cantidadMinima; 

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    public Integer getCantidadTotal() {
        return cantidadDisponible + cantidadReservada;
    }

    public boolean isBajoStock() {
        return cantidadDisponible <= cantidadMinima;
    }
}
