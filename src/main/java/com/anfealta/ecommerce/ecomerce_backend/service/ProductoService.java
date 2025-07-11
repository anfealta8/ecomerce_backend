package com.anfealta.ecommerce.ecomerce_backend.service;

import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto;
import com.anfealta.ecommerce.ecomerce_backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejo de transacciones

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un nuevo producto.
     * @param request DTO con los datos del producto.
     * @return DTO del producto creado.
     */
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        // Validar si el SKU ya existe antes de crear
        if (productoRepository.findBySku(request.getSku()).isPresent()) {
            throw new RuntimeException("El SKU '" + request.getSku() + "' ya existe para otro producto.");
        }

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(request.getCategoria())
                .sku(request.getSku())
                .precio(request.getPrecio())
                .activo(request.getActivo())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        Producto productoGuardado = productoRepository.save(producto);
        return mapToProductoResponse(productoGuardado);
    }

    /**
     * Obtiene un producto por su ID.
     * @param id ID del producto.
     * @return Optional de ProductoResponse.
     */
    @Transactional(readOnly = true)
    public Optional<ProductoResponse> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
                .map(this::mapToProductoResponse);
    }

    /**
     * Obtiene todos los productos.
     * @return Lista de ProductoResponse.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerTodosLosProductos() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToProductoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un producto existente.
     * @param id ID del producto a actualizar.
     * @param request DTO con los nuevos datos del producto.
     * @return Optional de ProductoResponse del producto actualizado.
     */
    @Transactional
    public Optional<ProductoResponse> actualizarProducto(Long id, ProductoRequest request) {
        return productoRepository.findById(id)
                .map(productoExistente -> {
                    // Validar si el SKU actualizado ya existe para otro producto (que no sea el actual)
                    if (productoRepository.findBySku(request.getSku()).isPresent() &&
                        !productoExistente.getSku().equals(request.getSku())) {
                        throw new RuntimeException("El SKU '" + request.getSku() + "' ya existe para otro producto.");
                    }

                    productoExistente.setNombre(request.getNombre());
                    productoExistente.setDescripcion(request.getDescripcion());
                    productoExistente.setCategoria(request.getCategoria());
                    productoExistente.setSku(request.getSku());
                    productoExistente.setPrecio(request.getPrecio());
                    productoExistente.setActivo(request.getActivo());
                    productoExistente.setFechaActualizacion(LocalDateTime.now());
                    Producto productoActualizado = productoRepository.save(productoExistente);
                    return mapToProductoResponse(productoActualizado);
                });
    }

    /**
     * Elimina un producto por su ID.
     * @param id ID del producto a eliminar.
     */
    @Transactional
    public boolean eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false; // El producto no existe
    }

    /**
     * Busca productos por nombre (ignorando mayúsculas/minúsculas).
     * @param nombre Nombre a buscar.
     * @return Lista de ProductoResponse.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToProductoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca productos por categoría (ignorando mayúsculas/minúsculas).
     * @param categoria Categoría a buscar.
     * @return Lista de ProductoResponse.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(this::mapToProductoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los productos que están activos.
     * @return Lista de ProductoResponse de productos activos.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerProductosActivos() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToProductoResponse)
                .collect(Collectors.toList());
    }

    // --- Método de mapeo de Entidad a DTO de Respuesta ---
    private ProductoResponse mapToProductoResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .categoria(producto.getCategoria())
                .sku(producto.getSku())
                .precio(producto.getPrecio())
                .activo(producto.getActivo())
                .fechaCreacion(producto.getFechaCreacion())
                .fechaActualizacion(producto.getFechaActualizacion())
                .build();
    }
}