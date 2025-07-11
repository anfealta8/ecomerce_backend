package com.anfealta.ecommerce.ecomerce_backend.controller;

import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoResponse;
import com.anfealta.ecommerce.ecomerce_backend.service.ProductoService;
import jakarta.validation.Valid; // Para validar los DTOs de entrada
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/productos") // Define la ruta base para todos los endpoints de productos
public class ProductoController {

    private final ProductoService productoService;

    // Inyección de dependencias a través del constructor
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Endpoint para crear un nuevo producto. (CRUD: CREATE)
     * Requiere autenticación.
     * @param request El DTO con los datos del producto a crear.
     * @return ResponseEntity con el ProductoResponse del producto creado y status 201.
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        try {
            ProductoResponse nuevoProducto = productoService.crearProducto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
        } catch (RuntimeException e) {
            // Maneja específicamente la excepción de SKU duplicado u otros errores de negocio
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para obtener un producto por su ID. (CRUD: READ)
     * Requiere autenticación.
     * @param id El ID del producto.
     * @return ResponseEntity con el ProductoResponse si se encuentra, y status 200, o 404 si no.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Long id) {
        Optional<ProductoResponse> producto = productoService.obtenerProductoPorId(id);
        return producto.map(ResponseEntity::ok) // Si el producto está presente, devuelve 200 OK
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id)); // Si no, 404 Not Found
    }

    /**
     * Endpoint para obtener todos los productos. (CRUD: READ ALL)
     * Requiere autenticación.
     * @return ResponseEntity con una lista de ProductoResponse y status 200.
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> obtenerTodosLosProductos() {
        List<ProductoResponse> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Endpoint para actualizar un producto existente. (CRUD: UPDATE)
     * Requiere autenticación.
     * @param id El ID del producto a actualizar.
     * @param request El DTO con los nuevos datos del producto.
     * @return ResponseEntity con el ProductoResponse del producto actualizado y status 200, o 404 si no.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        try {
            Optional<ProductoResponse> productoActualizado = productoService.actualizarProducto(id, request);
            return productoActualizado.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id));
        } catch (RuntimeException e) {
            // Maneja específicamente la excepción de SKU duplicado u otros errores de negocio
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para eliminar un producto por su ID. (CRUD: DELETE)
     * Requiere autenticación.
     * @param id El ID del producto a eliminar.
     * @return ResponseEntity con status 204 No Content si se elimina, o 404 si no se encuentra.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        boolean eliminado = productoService.eliminarProducto(id);
        if (eliminado) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id); // 404 Not Found
        }
    }

    /**
     * Endpoint para buscar productos por nombre. (Búsqueda por criterios)
     * Requiere autenticación.
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
     * Requiere autenticación.
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
     * Requiere autenticación.
     * @return Lista de ProductoResponse de productos activos.
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosActivos() {
        List<ProductoResponse> productos = productoService.obtenerProductosActivos();
        return ResponseEntity.ok(productos);
    }
}