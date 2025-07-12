package com.anfealta.ecommerce.ecomerce_backend.controller;

import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Orden; // Para el enum EstadoOrden
import com.anfealta.ecommerce.ecomerce_backend.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController 
@RequestMapping("/api/ordenes") 
public class OrdenController {

    private final OrdenService ordenService;

    
    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    /**
     * Endpoint para crear una nueva orden de compra. (CRUD: CREATE)
     * Requiere autenticación.
     * @param request El DTO con los datos de la orden (usuarioId y detalles de productos).
     * @return ResponseEntity con el OrdenResponse de la orden creada y status 201.
     */
    @PostMapping
    public ResponseEntity<OrdenResponse> crearOrden(@Valid @RequestBody OrdenRequest request) {
        try {
            OrdenResponse nuevaOrden = ordenService.crearOrden(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaOrden);
        } catch (ResponseStatusException e) {
            
            throw e;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear la orden: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener una orden por su ID. (CRUD: READ)
     * Requiere autenticación.
     * @param id El ID de la orden.
     * @return ResponseEntity con el OrdenResponse si se encuentra, y status 200, o 404 si no.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtenerOrdenPorId(@PathVariable Long id) {
        Optional<OrdenResponse> orden = ordenService.obtenerOrdenPorId(id);
        return orden.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con ID: " + id));
    }

    /**
     * Endpoint para obtener todas las órdenes. (CRUD: READ ALL)
     * Requiere autenticación.
     * @return ResponseEntity con una lista de OrdenResponse y status 200.
     */
    @GetMapping
    public ResponseEntity<List<OrdenResponse>> obtenerTodasLasOrdenes() {
        List<OrdenResponse> ordenes = ordenService.obtenerTodasLasOrdenes();
        return ResponseEntity.ok(ordenes);
    }

    /**
     * Endpoint para obtener todas las órdenes de un usuario específico.
     * Requiere autenticación.
     * @param usuarioId El ID del usuario.
     * @return ResponseEntity con una lista de OrdenResponse del usuario y status 200.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<OrdenResponse>> obtenerOrdenesPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<OrdenResponse> ordenes = ordenService.obtenerOrdenesPorUsuario(usuarioId);
            return ResponseEntity.ok(ordenes);
        } catch (ResponseStatusException e) {
            throw e; 
        }
    }

    /**
     * Endpoint para actualizar el estado de una orden.
     * Requiere autenticación.
     * @param id El ID de la orden a actualizar.
     * @param nuevoEstado El nuevo estado (ej. "COMPLETADA", "ENVIADA").
     * @return ResponseEntity con el OrdenResponse de la orden actualizada y status 200, o 404 si no.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<OrdenResponse> actualizarEstadoOrden(@PathVariable Long id, @RequestParam String nuevoEstado) {
        try {
            Orden.EstadoOrden estadoEnum = Orden.EstadoOrden.valueOf(nuevoEstado.toUpperCase()); // Convertir String a Enum
            Optional<OrdenResponse> ordenActualizada = ordenService.actualizarEstadoOrden(id, estadoEnum);
            return ordenActualizada.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con ID: " + id));
        } catch (IllegalArgumentException e) {
            
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de orden inválido: " + nuevoEstado + ". Estados permitidos: " + java.util.Arrays.toString(Orden.EstadoOrden.values()));
        } catch (ResponseStatusException e) {
            throw e; 
        }
    }

    /**
     * Endpoint para eliminar una orden por su ID. (CRUD: DELETE)
     * Requiere autenticación.
     * @param id El ID de la orden a eliminar.
     * @return ResponseEntity con status 204 No Content si se elimina, o 404 si no se encuentra.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrden(@PathVariable Long id) {
        boolean eliminado = ordenService.eliminarOrden(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con ID: " + id); // 404 Not Found
        }
    }
}