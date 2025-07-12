package com.anfealta.ecommerce.ecomerce_backend.service;

import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoResponse;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    ProductoResponse crearProducto(ProductoRequest request);
    Optional<ProductoResponse> obtenerProductoPorId(Long id);
    List<ProductoResponse> obtenerTodosLosProductos();
    Optional<ProductoResponse> actualizarProducto(Long id, ProductoRequest request);
    boolean eliminarProducto(Long id);
    List<ProductoResponse> buscarPorNombre(String nombre);
    List<ProductoResponse> buscarPorCategoria(String categoria);
    List<ProductoResponse> obtenerProductosActivos();
    List<ProductoResponse> obtenerTop5ProductosMasVendidos();
}