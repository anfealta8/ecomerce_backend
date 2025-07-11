package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.Inventario; // Importa tu entidad Inventario
import org.springframework.data.jpa.repository.JpaRepository; // La interfaz base de Spring Data JPA
import org.springframework.stereotype.Repository; // Marca esta interfaz como un componente de repositorio de Spring

import java.util.Optional; // Importa Optional

@Repository // Anotación que indica a Spring que esta interfaz es un repositorio
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    // JpaRepository ya te proporciona métodos básicos para CRUD (save, findById, findAll, delete, etc.)

    // Podemos añadir métodos personalizados si son necesarios, por ejemplo,
    // buscar un registro de inventario por el ID de su producto asociado.
    Optional<Inventario> findByProductoId(Long productoId);

    // O para encontrar inventarios con baja cantidad de stock (ej. para reportes)
    // List<Inventario> findByCantidadDisponibleLessThanEqual(Integer cantidad);
}
