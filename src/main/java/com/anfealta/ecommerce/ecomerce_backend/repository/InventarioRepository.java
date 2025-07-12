package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.Inventario; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 

import java.util.Optional; 

@Repository 
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    
    Optional<Inventario> findByProductoId(Long productoId);

    
}
