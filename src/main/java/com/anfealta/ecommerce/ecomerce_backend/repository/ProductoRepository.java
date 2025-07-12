package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.Producto; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
import java.util.List;
import java.util.Optional;

@Repository 
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);

    boolean existsBySku(String sku); 

    List<Producto> findByNombreContainingIgnoreCase(String nombre); 

    List<Producto> findByCategoriaIgnoreCase(String categoria); 

    List<Producto> findByActivoTrue();

    List<Producto> findByCategoriaIgnoreCaseAndActivoTrue(String categoria); 

}