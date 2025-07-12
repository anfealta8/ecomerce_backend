package com.anfealta.ecommerce.ecomerce_backend.controller;

import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoResponse;
import com.anfealta.ecommerce.ecomerce_backend.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Endpoint para crear un nuevo producto. (CRUD: CREATE)
     * Solo los usuarios con el rol ADMIN pueden crear productos.
     * @param request El DTO con los datos del producto a crear.
     * @return ResponseEntity con el ProductoResponse del producto creado y status 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        try {
            ProductoResponse nuevoProducto = productoService.crearProducto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para obtener un producto por su ID. (CRUD: READ)
     * Cualquier usuario autenticado (ADMIN o USER) puede ver un producto específico.
     * Si quieres que sea público, también tendrías que modificar SecurityConfig.
     * @param id El ID del producto.
     * @return ResponseEntity con el ProductoResponse si se encuentra, y status 200, o 404 si no.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") 
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Long id) {
        Optional<ProductoResponse> producto = productoService.obtenerProductoPorId(id);
        return producto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id));
    }

    /**
     * Endpoint para obtener todos los productos. (CRUD: READ ALL)
     * Haremos este endpoint público, para que cualquier persona pueda ver los productos disponibles.
     * Esto también requiere un cambio en SecurityConfig.
     * @return ResponseEntity con una lista de ProductoResponse y status 200.
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> obtenerTodosLosProductos() {
        List<ProductoResponse> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Endpoint para actualizar un producto existente. (CRUD: UPDATE)
     * Solo los usuarios con el rol ADMIN pueden actualizar productos.
     * @param id El ID del producto a actualizar.
     * @param request El DTO con los nuevos datos del producto.
     * @return ResponseEntity con el ProductoResponse del producto actualizado y status 200, o 404 si no.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        try {
            Optional<ProductoResponse> productoActualizado = productoService.actualizarProducto(id, request);
            return productoActualizado.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para eliminar un producto por su ID. (CRUD: DELETE)
     * Solo los usuarios con el rol ADMIN pueden eliminar productos.
     * @param id El ID del producto a eliminar.
     * @return ResponseEntity con status 204 No Content si se elimina, o 404 si no se encuentra.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Solo un usuario con el rol 'ADMIN'
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        boolean eliminado = productoService.eliminarProducto(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id);
        }
    }

    /**
     * Endpoint para buscar productos por nombre. (Búsqueda por criterios)
     * Haremos este endpoint público, para que cualquier persona pueda buscar productos.
     * Esto también requiere un cambio en SecurityConfig.
     * @param nombre El nombre (o parte del nombre) a buscar.
     * @return Lista de ProductoResponse que coinciden.
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<ProductoResponse>> buscarProductosPorNombre(@RequestParam String nombre) {
        List<ProductoResponse> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    /**
     * Endpoint para buscar productos por categoría. (Búsqueda por criterios)
     * Haremos este endpoint público, para que cualquier persona pueda buscar productos.
     * Esto también requiere un cambio en SecurityConfig.
     * @param categoria La categoría a buscar.
     * @return Lista de ProductoResponse que coinciden.
     */
    @GetMapping("/buscar/categoria")
    
    public ResponseEntity<List<ProductoResponse>> buscarProductosPorCategoria(@RequestParam String categoria) {
        List<ProductoResponse> productos = productoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    /**
     * Endpoint para obtener productos activos. (Reporte: Productos Activos)
     * Haremos este endpoint público, para que cualquier persona pueda ver los productos activos.
     * Esto también requiere un cambio en SecurityConfig.
     * @return Lista de ProductoResponse de productos activos.
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosActivos() {
        List<ProductoResponse> productos = productoService.obtenerProductosActivos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Endpoint para obtener el Top 5 de productos más vendidos.
     * Requiere rol ADMIN o USER.
     * @return Lista de ProductoResponse (o TopSoldProductResponse si lo creaste).
     */
    @GetMapping("/reportes/top5-vendidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ProductoResponse>> obtenerTop5ProductosMasVendidos() {
        List<ProductoResponse> productos = productoService.obtenerTop5ProductosMasVendidos();
        return ResponseEntity.ok(productos);
    }
}