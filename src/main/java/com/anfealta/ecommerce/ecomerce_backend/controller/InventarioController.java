package com.anfealta.ecommerce.ecomerce_backend.controller;

import com.anfealta.ecommerce.ecomerce_backend.dto.InventarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.InventarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/inventarios") // Define la ruta base para todos los endpoints de inventarios
public class InventarioController {

    private final InventarioService inventarioService;

    // Inyección de dependencias a través del constructor
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Endpoint para crear un nuevo registro de inventario. (CRUD: CREATE)
     * Requiere autenticación.
     * @param request El DTO con los datos del inventario a crear.
     * @return ResponseEntity con el InventarioResponse del inventario creado y status 201.
     */
    @PostMapping
    public ResponseEntity<InventarioResponse> crearInventario(@Valid @RequestBody InventarioRequest request) {
        try {
            InventarioResponse nuevoInventario = inventarioService.crearInventario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoInventario);
        } catch (RuntimeException e) {
            // Maneja excepciones como "Producto no encontrado" o "Ya existe inventario para este producto"
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para obtener un registro de inventario por su ID. (CRUD: READ)
     * Requiere autenticación.
     * @param id El ID del registro de inventario.
     * @return ResponseEntity con el InventarioResponse si se encuentra, y status 200, o 404 si no.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponse> obtenerInventarioPorId(@PathVariable Long id) {
        Optional<InventarioResponse> inventario = inventarioService.obtenerInventarioPorId(id);
        return inventario.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de inventario no encontrado con ID: " + id));
    }

    /**
     * Endpoint para obtener un registro de inventario por el ID de su producto asociado.
     * Requiere autenticación.
     * @param productoId El ID del producto.
     * @return ResponseEntity con el InventarioResponse si se encuentra, y status 200, o 404 si no.
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<InventarioResponse> obtenerInventarioPorProductoId(@PathVariable Long productoId) {
        Optional<InventarioResponse> inventario = inventarioService.obtenerInventarioPorProductoId(productoId);
        return inventario.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de inventario no encontrado para el Producto con ID: " + productoId));
    }

    /**
     * Endpoint para obtener todos los registros de inventario. (CRUD: READ ALL)
     * Requiere autenticación.
     * @return ResponseEntity con una lista de InventarioResponse y status 200.
     */
    @GetMapping
    public ResponseEntity<List<InventarioResponse>> obtenerTodosLosInventarios() {
        List<InventarioResponse> inventarios = inventarioService.obtenerTodosLosInventarios();
        return ResponseEntity.ok(inventarios);
    }

    /**
     * Endpoint para actualizar un registro de inventario existente. (CRUD: UPDATE)
     * Requiere autenticación.
     * @param id El ID del registro de inventario a actualizar.
     * @param request El DTO con los nuevos datos del inventario.
     * @return ResponseEntity con el InventarioResponse del inventario actualizado y status 200, o 404 si no.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InventarioResponse> actualizarInventario(@PathVariable Long id, @Valid @RequestBody InventarioRequest request) {
        try {
            Optional<InventarioResponse> inventarioActualizado = inventarioService.actualizarInventario(id, request);
            return inventarioActualizado.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de inventario no encontrado con ID: " + id));
        } catch (RuntimeException e) {
            // Maneja errores de negocio como el intento de asociar un producto ya inventariado
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para eliminar un registro de inventario por su ID. (CRUD: DELETE)
     * Requiere autenticación.
     * @param id El ID del registro de inventario a eliminar.
     * @return ResponseEntity con status 204 No Content si se elimina, o 404 si no se encuentra.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id) {
        boolean eliminado = inventarioService.eliminarInventario(id);
        if (eliminado) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de inventario no encontrado con ID: " + id); // 404 Not Found
        }
    }
}