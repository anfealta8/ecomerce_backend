package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.Orden; 
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuario(Usuario usuario); 
    List<Orden> findByFechaCreacionBetween(LocalDateTime startDate, LocalDateTime endDate); 
    List<Orden> findByEstado(Orden.EstadoOrden estado); 

    @Query("SELECT o.usuario.id, o.usuario.nombreUsuario, COUNT(o.id) AS totalOrdenes " +
           "FROM Orden o GROUP BY o.usuario.id, o.usuario.nombreUsuario " +
           "ORDER BY totalOrdenes DESC")
    List<Object[]> findTop5FrequentCustomers(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Orden o WHERE o.usuario.id = :usuarioId AND o.fechaCreacion >= :fechaDesde")
    long countByUsuarioIdAndFechaCreacionAfter(Long usuarioId, LocalDateTime fechaDesde);


}