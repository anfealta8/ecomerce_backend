package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.OrdenDetalle; 
import com.anfealta.ecommerce.ecomerce_backend.entity.Orden; 
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenDetalleRepository extends JpaRepository<OrdenDetalle, Long> {
    List<OrdenDetalle> findByOrden(Orden orden); 
    List<OrdenDetalle> findByProducto(Producto producto); 

    @Query("SELECT od.producto.id, od.producto.nombre, SUM(od.cantidad) AS totalVendido " +
           "FROM OrdenDetalle od GROUP BY od.producto.id, od.producto.nombre " +
           "ORDER BY totalVendido DESC")
    List<Object[]> findTop5MostSoldProducts(Pageable pageable);
}