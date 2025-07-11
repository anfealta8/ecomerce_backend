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
import java.util.List; // Para la relación OneToMany con OrdenDetalle

@Data // Anotación de Lombok para getters, setters, toString, equals, hashCode
@Builder // Anotación de Lombok para el patrón Builder
@NoArgsConstructor // Anotación de Lombok para constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para constructor con todos los argumentos
@Entity // Indica que esta clase es una entidad JPA
@Table(name = "productos") // Define el nombre de la tabla en la base de datos
@EntityListeners(AuditingEntityListener.class) // Habilita la auditoría automática (@CreatedDate, @LastModifiedDate)
public class Producto {

    @Id // Marca este campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática de ID (autoincremento)
    private Long id;

    @Column(nullable = false, length = 255) // No nulo, longitud máxima 255
    private String nombre;

    @Column(length = 500) // Longitud máxima 500, puede ser nulo
    private String descripcion;

    @Column(nullable = false, length = 100) // No nulo, longitud máxima 100
    private String categoria;

    @Column(nullable = false, unique = true, length = 50) // No nulo, **único**, longitud máxima 50 (para SKU)
    private String sku; // Stock Keeping Unit: Identificador único del producto

    @Column(nullable = false, precision = 10, scale = 2) // No nulo, 10 dígitos en total, 2 decimales
    private BigDecimal precio;

    // Requisito: "e) i. Productos activos" -> Campo para saber si el producto está activo
    @Column(nullable = false)
    private Boolean activo; // True si el producto está disponible para la venta

    // Requisito: "g) Implementación de auditoria" -> Fecha de creación
    @CreatedDate
    @Column(nullable = false, updatable = false) // No nulo, no se puede actualizar después de la creación
    private LocalDateTime fechaCreacion;

    // Requisito: "g) Implementación de auditoria" -> Fecha de última actualización
    @LastModifiedDate
    @Column(nullable = false) // No nulo
    private LocalDateTime fechaActualizacion;

    // --- Relaciones (Opcional por ahora, pero las dejo comentadas para referencia futura) ---

    // Un producto puede estar en muchos detalles de órdenes (OrdenDetalle)
    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<OrdenDetalle> detallesDeOrden;

    // Un producto tiene un único inventario (relación OneToOne si la entidad Inventario es directa del producto)
     @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private Inventario inventario;
}
