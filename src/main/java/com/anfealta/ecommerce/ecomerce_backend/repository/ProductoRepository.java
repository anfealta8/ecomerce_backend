package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.Producto; // Importa tu entidad Producto
import org.springframework.data.jpa.repository.JpaRepository; // La interfaz base de Spring Data JPA
import org.springframework.stereotype.Repository; // Marca esta interfaz como un componente de repositorio de Spring

import java.util.List;
import java.util.Optional;

@Repository // Anotación que indica a Spring que esta interfaz es un repositorio
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // JpaRepository ya te proporciona métodos básicos para CRUD (save, findById, findAll, delete, etc.)

    // --- Métodos de Búsqueda de Productos por Distintos Criterios (Requisito 'f') ---

    // Buscar un producto por su SKU (Stock Keeping Unit). SKU debe ser único.
    Optional<Producto> findBySku(String sku);

    // Buscar productos por su nombre (puede haber nombres similares, por eso List)
    List<Producto> findByNombreContainingIgnoreCase(String nombre); // Busca nombres que contengan la cadena, ignorando mayúsculas/minúsculas

    // Buscar productos por categoría
    List<Producto> findByCategoriaIgnoreCase(String categoria); // Busca por categoría exacta, ignorando mayúsculas/minúsculas

    // Buscar productos que estén activos (Requisito 'e.i. Productos activos')
    List<Producto> findByActivoTrue();

    // Puedes combinar criterios, por ejemplo:
    List<Producto> findByCategoriaIgnoreCaseAndActivoTrue(String categoria); // Productos activos en una categoría específica

    // Para un CRUD completo y búsqueda avanzada, también puedes considerar:
    // Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable); // Para paginación
    // List<Producto> findByPrecioBetween(BigDecimal minPrecio, BigDecimal maxPrecio); // Rango de precios
}