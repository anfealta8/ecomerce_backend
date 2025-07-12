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
import java.util.ArrayList; 
import java.util.List; 

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "ordenes") 
@EntityListeners(AuditingEntityListener.class) 
public class Orden {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false) 
    private Usuario usuario;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion; 

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion; 

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING) 
    private EstadoOrden estado; 

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; 

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoTotal; 

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total; 

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDetalle> detalles = new ArrayList<>(); 

    public enum EstadoOrden {
        PENDIENTE,
        COMPLETADA,
        CANCELADA,
        ENVIADA,
        ENTREGADA
    }

    public void addDetalle(OrdenDetalle detalle) {
        detalles.add(detalle);
        detalle.setOrden(this);
    }

    public void removeDetalle(OrdenDetalle detalle) {
        detalles.remove(detalle);
        detalle.setOrden(null);
    }
}
