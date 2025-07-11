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

@Data // Anotación de Lombok para getters, setters, toString, equals, hashCode
@Builder // Anotación de Lombok para el patrón Builder
@NoArgsConstructor // Anotación de Lombok para constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para constructor con todos los argumentos
@Entity // Indica que esta clase es una entidad JPA
@Table(name = "inventarios") // Define el nombre de la tabla en la base de datos
@EntityListeners(AuditingEntityListener.class) // Habilita la auditoría automática
public class Inventario {

    @Id // Marca este campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática de ID
    private Long id;

    // Relación OneToOne con Producto: Cada Inventario corresponde a un único Producto
    // y cada Producto puede tener un único registro de Inventario.
    // 'fetch = FetchType.LAZY' para cargar el producto solo cuando sea necesario.
    // 'optional = false' indica que un registro de inventario siempre debe estar asociado a un producto.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true) // Clave foránea a la tabla de productos
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidadDisponible; // Cantidad de unidades de este producto en inventario

    @Column(nullable = false)
    private Integer cantidadReservada = 0; // Cantidad reservada para órdenes pendientes

    @Column(nullable = false)
    private Integer cantidadMinima; // Umbral para alertar sobre bajo stock

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    // Métodos de conveniencia (opcional, se pueden manejar en el servicio)
    public Integer getCantidadTotal() {
        return cantidadDisponible + cantidadReservada;
    }

    public boolean isBajoStock() {
        return cantidadDisponible <= cantidadMinima;
    }
}
