package com.anfealta.ecommerce.ecomerce_backend.service;

import com.anfealta.ecommerce.ecomerce_backend.dto.InventarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.InventarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Inventario;
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto;
import com.anfealta.ecommerce.ecomerce_backend.repository.InventarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository; 

    @Autowired
    public InventarioService(InventarioRepository inventarioRepository, ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un nuevo registro de inventario para un producto.
     * @param request DTO con los datos del inventario.
     * @return DTO del inventario creado.
     * @throws RuntimeException si el producto no existe o ya tiene un registro de inventario.
     */
    @Transactional
    public InventarioResponse crearInventario(InventarioRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + request.getProductoId()));

        if (inventarioRepository.findByProductoId(producto.getId()).isPresent()) {
            throw new RuntimeException("Ya existe un registro de inventario para el producto con ID: " + producto.getId());
        }

        Inventario inventario = Inventario.builder()
                .producto(producto) 
                .cantidadDisponible(request.getCantidadDisponible())
                .cantidadReservada(request.getCantidadReservada())
                .cantidadMinima(request.getCantidadMinima())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Inventario inventarioGuardado = inventarioRepository.save(inventario);
        return mapToInventarioResponse(inventarioGuardado);
    }

    /**
     * Obtiene un registro de inventario por su ID.
     * @param id ID del registro de inventario.
     * @return Optional de InventarioResponse.
     */
    @Transactional(readOnly = true)
    public Optional<InventarioResponse> obtenerInventarioPorId(Long id) {
        return inventarioRepository.findById(id)
                .map(this::mapToInventarioResponse);
    }

    /**
     * Obtiene un registro de inventario por el ID del producto asociado.
     * @param productoId ID del producto.
     * @return Optional de InventarioResponse.
     */
    @Transactional(readOnly = true)
    public Optional<InventarioResponse> obtenerInventarioPorProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .map(this::mapToInventarioResponse);
    }

    /**
     * Obtiene todos los registros de inventario.
     * @return Lista de InventarioResponse.
     */
    @Transactional(readOnly = true)
    public List<InventarioResponse> obtenerTodosLosInventarios() {
        return inventarioRepository.findAll()
                .stream()
                .map(this::mapToInventarioResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un registro de inventario existente.
     * @param id ID del registro de inventario a actualizar.
     * @param request DTO con los nuevos datos del inventario.
     * @return Optional de InventarioResponse del inventario actualizado.
     * @throws RuntimeException si el producto no existe o si el productoId intenta cambiar a uno ya asociado.
     */
    @Transactional
    public Optional<InventarioResponse> actualizarInventario(Long id, InventarioRequest request) {
        return inventarioRepository.findById(id)
                .map(inventarioExistente -> {
                    if (!inventarioExistente.getProducto().getId().equals(request.getProductoId())) {
                        Producto nuevoProducto = productoRepository.findById(request.getProductoId())
                                .orElseThrow(() -> new RuntimeException("Nuevo producto no encontrado con ID: " + request.getProductoId()));

                        if (inventarioRepository.findByProductoId(nuevoProducto.getId()).isPresent() &&
                            !inventarioRepository.findByProductoId(nuevoProducto.getId()).get().getId().equals(id)) {
                            throw new RuntimeException("El nuevo producto con ID: " + nuevoProducto.getId() + " ya tiene un registro de inventario.");
                        }
                        inventarioExistente.setProducto(nuevoProducto);
                    }

                    inventarioExistente.setCantidadDisponible(request.getCantidadDisponible());
                    inventarioExistente.setCantidadReservada(request.getCantidadReservada());
                    inventarioExistente.setCantidadMinima(request.getCantidadMinima());
                    inventarioExistente.setFechaActualizacion(LocalDateTime.now());

                    Inventario inventarioActualizado = inventarioRepository.save(inventarioExistente);
                    return mapToInventarioResponse(inventarioActualizado);
                });
    }

    /**
     * Elimina un registro de inventario por su ID.
     * @param id ID del registro de inventario a eliminar.
     * @return true si se eliminó exitosamente, false si el registro no se encontró.
     */
    @Transactional
    public boolean eliminarInventario(Long id) {
        if (inventarioRepository.existsById(id)) {
            inventarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private InventarioResponse mapToInventarioResponse(Inventario inventario) {
        String nombreProducto = (inventario.getProducto() != null) ? inventario.getProducto().getNombre() : null;
        String skuProducto = (inventario.getProducto() != null) ? inventario.getProducto().getSku() : null;

        return InventarioResponse.builder()
                .id(inventario.getId())
                .productoId(inventario.getProducto() != null ? inventario.getProducto().getId() : null)
                .nombreProducto(nombreProducto)
                .skuProducto(skuProducto)
                .cantidadDisponible(inventario.getCantidadDisponible())
                .cantidadReservada(inventario.getCantidadReservada())
                .cantidadMinima(inventario.getCantidadMinima())
                .fechaCreacion(inventario.getFechaCreacion())
                .fechaActualizacion(inventario.getFechaActualizacion())
                .build();
    }
}