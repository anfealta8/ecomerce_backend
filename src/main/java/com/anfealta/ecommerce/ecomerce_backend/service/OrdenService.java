package com.anfealta.ecommerce.ecomerce_backend.service;

import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Orden; 
import java.util.List;
import java.util.Optional;

public interface OrdenService {
    OrdenResponse crearOrden(OrdenRequest request);
    Optional<OrdenResponse> obtenerOrdenPorId(Long id);
    List<OrdenResponse> obtenerTodasLasOrdenes();
    List<OrdenResponse> obtenerOrdenesPorUsuario(Long usuarioId);
    Optional<OrdenResponse> actualizarEstadoOrden(Long id, Orden.EstadoOrden nuevoEstado);
    boolean eliminarOrden(Long id);
}