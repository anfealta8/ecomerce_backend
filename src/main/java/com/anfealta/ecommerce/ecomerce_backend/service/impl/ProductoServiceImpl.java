package com.anfealta.ecommerce.ecomerce_backend.service.impl;

import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto;
import com.anfealta.ecommerce.ecomerce_backend.repository.ProductoRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.OrdenDetalleRepository; 
import com.anfealta.ecommerce.ecomerce_backend.service.ProductoService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest; 
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final OrdenDetalleRepository ordenDetalleRepository; 

    public ProductoServiceImpl(ProductoRepository productoRepository, OrdenDetalleRepository ordenDetalleRepository) {
        this.productoRepository = productoRepository;
        this.ordenDetalleRepository = ordenDetalleRepository;
    }

    private ProductoResponse mapToDto(Producto producto) {
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

    private Producto mapToEntity(ProductoRequest request) {
        return Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(request.getCategoria())
                .sku(request.getSku())
                .precio(request.getPrecio())
                .activo(request.getActivo() != null ? request.getActivo() : true) 
                .build();
    }

    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        if (productoRepository.existsBySku(request.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El SKU del producto ya existe: " + request.getSku());
        }
        Producto producto = mapToEntity(request);
        producto = productoRepository.save(producto);
        return mapToDto(producto);
    }

    @Override
    public Optional<ProductoResponse> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id).map(this::mapToDto);
    }

    @Override
    public List<ProductoResponse> obtenerTodosLosProductos() {
        return productoRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ProductoResponse> actualizarProducto(Long id, ProductoRequest request) {
        return productoRepository.findById(id).map(producto -> {
            if (!producto.getSku().equals(request.getSku()) && productoRepository.existsBySku(request.getSku())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nuevo SKU ya existe para otro producto: " + request.getSku());
            }
            producto.setNombre(request.getNombre());
            producto.setDescripcion(request.getDescripcion());
            producto.setCategoria(request.getCategoria());
            producto.setSku(request.getSku());
            producto.setPrecio(request.getPrecio());
            producto.setActivo(request.getActivo() != null ? request.getActivo() : producto.getActivo());
            return mapToDto(productoRepository.save(producto));
        });
    }

    @Override
    @Transactional
    public boolean eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<ProductoResponse> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> obtenerProductosActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> obtenerTop5ProductosMasVendidos() {
        List<Object[]> results = ordenDetalleRepository.findTop5MostSoldProducts(PageRequest.of(0, 5));
        return results.stream()
                .map(result -> {
                    Long productId = (Long) result[0];
                    String productName = (String) result[1];
                    Long totalSold = (Long) result[2]; 
                    return ProductoResponse.builder()
                            .id(productId)
                            .nombre(productName)
                            .build();
                })
                .collect(Collectors.toList());
    }
}