package com.anfealta.ecommerce.ecomerce_backend.service; 

import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.ProductoResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto;
import com.anfealta.ecommerce.ecomerce_backend.repository.ProductoRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.OrdenDetalleRepository; // Necesario para el mock
import com.anfealta.ecommerce.ecomerce_backend.service.impl.ProductoServiceImpl; // Importa la implementación concreta

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageRequest; // Necesario para PageRequest.of

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock 
    private ProductoRepository productoRepository;

    @Mock 
    private OrdenDetalleRepository ordenDetalleRepository;

    @InjectMocks 
    private ProductoServiceImpl productoService;

    private ProductoRequest productoRequest;
    private Producto productoExistente;
    private Producto productoActualizado;
    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        productoRequest = ProductoRequest.builder()
                .nombre("Laptop XYZ")
                .descripcion("Potente laptop para gaming")
                .categoria("Electronicos")
                .sku("LAPTOP-XYZ-001")
                .precio(new BigDecimal("1500.00"))
                .activo(true)
                .build();

        productoExistente = Producto.builder()
                .id(1L)
                .nombre("Laptop XYZ")
                .descripcion("Potente laptop para gaming")
                .categoria("Electronicos")
                .sku("LAPTOP-XYZ-001")
                .precio(new BigDecimal("1500.00"))
                .activo(true)
                .fechaCreacion(LocalDateTime.now().minusDays(10))
                .fechaActualizacion(LocalDateTime.now().minusDays(10))
                .build();

        productoActualizado = Producto.builder()
                .id(1L)
                .nombre("Laptop XYZ Pro")
                .descripcion("Potente laptop para gaming y diseño")
                .categoria("Electronicos")
                .sku("LAPTOP-XYZ-001-PRO") // SKU cambiado
                .precio(new BigDecimal("1800.00"))
                .activo(true)
                .fechaCreacion(productoExistente.getFechaCreacion())
                .fechaActualizacion(LocalDateTime.now()) 
                .build();

        productoResponse = ProductoResponse.builder()
                .id(1L)
                .nombre("Laptop XYZ")
                .descripcion("Potente laptop para gaming")
                .categoria("Electronicos")
                .sku("LAPTOP-XYZ-001")
                .precio(new BigDecimal("1500.00"))
                .activo(true)
                .fechaCreacion(LocalDateTime.now().minusDays(10))
                .fechaActualizacion(LocalDateTime.now().minusDays(10))
                .build();
    }
    
    @Test
    @DisplayName("Debe crear un producto exitosamente")
    void crearProducto_Success() {
        when(productoRepository.existsBySku(productoRequest.getSku())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoExistente);

        ProductoResponse response = productoService.crearProducto(productoRequest);

        assertNotNull(response);
        assertEquals(productoExistente.getId(), response.getId());
        assertEquals(productoRequest.getNombre(), response.getNombre());
        assertEquals(productoRequest.getSku(), response.getSku());
        assertEquals(productoRequest.getPrecio(), response.getPrecio());

        verify(productoRepository, times(1)).existsBySku(productoRequest.getSku());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (CONFLICT) cuando el SKU ya existe al crear")
    void crearProducto_SkuConflict() {
        when(productoRepository.existsBySku(productoRequest.getSku())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productoService.crearProducto(productoRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El SKU del producto ya existe: " + productoRequest.getSku()));

        verify(productoRepository, times(1)).existsBySku(productoRequest.getSku());
        verify(productoRepository, never()).save(any(Producto.class)); // Asegura que save no fue llamado
    }
    
    @Test
    @DisplayName("Debe obtener un producto por ID existente")
    void obtenerProductoPorId_Found() {
        when(productoRepository.findById(productoExistente.getId())).thenReturn(Optional.of(productoExistente));

        Optional<ProductoResponse> response = productoService.obtenerProductoPorId(productoExistente.getId());

        assertTrue(response.isPresent());
        assertEquals(productoExistente.getId(), response.get().getId());
        assertEquals(productoExistente.getNombre(), response.get().getNombre());
        verify(productoRepository, times(1)).findById(productoExistente.getId());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando el producto no se encuentra por ID")
    void obtenerProductoPorId_NotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProductoResponse> response = productoService.obtenerProductoPorId(99L);

        assertFalse(response.isPresent());
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe obtener una lista de todos los productos")
    void obtenerTodosLosProductos_Success() {
        
        List<Producto> productos = Arrays.asList(productoExistente, productoActualizado);
        when(productoRepository.findAll()).thenReturn(productos);

        
        List<ProductoResponse> responseList = productoService.obtenerTodosLosProductos();

        
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(productoExistente.getNombre(), responseList.get(0).getNombre());
        assertEquals(productoActualizado.getNombre(), responseList.get(1).getNombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si no hay productos")
    void obtenerTodosLosProductos_EmptyList() {
        
        when(productoRepository.findAll()).thenReturn(Collections.emptyList());

        
        List<ProductoResponse> responseList = productoService.obtenerTodosLosProductos();

        
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(productoRepository, times(1)).findAll();
    }
    
    @Test
    @DisplayName("Debe actualizar un producto exitosamente sin cambiar el SKU")
    void actualizarProducto_Success_SameSku() {
        Long productId = productoExistente.getId();
        ProductoRequest updateRequest = ProductoRequest.builder()
                .nombre("Laptop XYZ Actualizada")
                .descripcion("Descripción actualizada")
                .categoria("Electronicos")
                .sku(productoExistente.getSku()) 
                .precio(new BigDecimal("1600.00"))
                .activo(true)
                .build();

        when(productoRepository.findById(productId)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setFechaActualizacion(LocalDateTime.now()); 
            return p;
        });

        Optional<ProductoResponse> response = productoService.actualizarProducto(productId, updateRequest);

        assertTrue(response.isPresent());
        assertEquals(updateRequest.getNombre(), response.get().getNombre());
        assertEquals(updateRequest.getPrecio(), response.get().getPrecio());
        assertEquals(updateRequest.getSku(), response.get().getSku());

        verify(productoRepository, times(1)).findById(productId);
        verify(productoRepository, never()).existsBySku(anyString());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe actualizar un producto exitosamente cambiando el SKU a uno no existente")
    void actualizarProducto_Success_NewSku() {
        Long productId = productoExistente.getId();
        ProductoRequest updateRequest = ProductoRequest.builder()
                .nombre("Laptop XYZ Actualizada")
                .descripcion("Descripción actualizada")
                .categoria("Electronicos")
                .sku("NEW-SKU-001") // Nuevo SKU
                .precio(new BigDecimal("1600.00"))
                .activo(true)
                .build();

        when(productoRepository.findById(productId)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsBySku(updateRequest.getSku())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setFechaActualizacion(LocalDateTime.now());
            return p;
        });

        Optional<ProductoResponse> response = productoService.actualizarProducto(productId, updateRequest);

        assertTrue(response.isPresent());
        assertEquals(updateRequest.getNombre(), response.get().getNombre());
        assertEquals(updateRequest.getSku(), response.get().getSku()); // SKU debe ser el nuevo

        verify(productoRepository, times(1)).findById(productId);
        verify(productoRepository, times(1)).existsBySku(updateRequest.getSku());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (CONFLICT) si el nuevo SKU ya existe para otro producto al actualizar")
    void actualizarProducto_SkuConflict() {
        Long productId = productoExistente.getId();
        ProductoRequest updateRequest = ProductoRequest.builder()
                .nombre("Laptop XYZ Actualizada")
                .descripcion("Descripción actualizada")
                .categoria("Electronicos")
                .sku("SKU-OTRO-PRODUCTO") 
                .precio(new BigDecimal("1600.00"))
                .activo(true)
                .build();

        when(productoRepository.findById(productId)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsBySku(updateRequest.getSku())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productoService.actualizarProducto(productId, updateRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El nuevo SKU ya existe para otro producto: " + updateRequest.getSku()));

        verify(productoRepository, times(1)).findById(productId);
        verify(productoRepository, times(1)).existsBySku(updateRequest.getSku());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si el producto a actualizar no se encuentra")
    void actualizarProducto_NotFound() {
        Long productId = 99L;
        ProductoRequest updateRequest = ProductoRequest.builder()
                .nombre("Producto Inexistente")
                .sku("NON-EXISTENT-SKU")
                .precio(BigDecimal.ZERO)
                .build();

        
        when(productoRepository.findById(productId)).thenReturn(Optional.empty());

        
        Optional<ProductoResponse> response = productoService.actualizarProducto(productId, updateRequest);

        
        assertFalse(response.isPresent());
        verify(productoRepository, times(1)).findById(productId);
        verify(productoRepository, never()).existsBySku(anyString()); 
        verify(productoRepository, never()).save(any(Producto.class));
    }
    
    @Test
    @DisplayName("Debe eliminar un producto existente y retornar true")
    void eliminarProducto_Success() {
        
        when(productoRepository.existsById(productoExistente.getId())).thenReturn(true);
        doNothing().when(productoRepository).deleteById(productoExistente.getId()); 

        
        boolean eliminado = productoService.eliminarProducto(productoExistente.getId());

        
        assertTrue(eliminado);
        verify(productoRepository, times(1)).existsById(productoExistente.getId());
        verify(productoRepository, times(1)).deleteById(productoExistente.getId());
    }

    @Test
    @DisplayName("Debe retornar false cuando se intenta eliminar un producto no existente")
    void eliminarProducto_NotFound() {
        
        when(productoRepository.existsById(99L)).thenReturn(false);

        
        boolean eliminado = productoService.eliminarProducto(99L);

        
        assertFalse(eliminado);
        verify(productoRepository, times(1)).existsById(99L);
        verify(productoRepository, never()).deleteById(anyLong()); // Asegura que deleteById no fue llamado
    }

    @Test
    @DisplayName("Debe buscar productos por nombre (case-insensitive)")
    void buscarPorNombre_Success() {
        
        Producto p1 = Producto.builder().id(1L).nombre("Laptop Gaming").sku("LG-001").build();
        Producto p2 = Producto.builder().id(2L).nombre("Laptop Ultrabook").sku("LU-002").build();
        List<Producto> foundProducts = Arrays.asList(p1, p2);
        when(productoRepository.findByNombreContainingIgnoreCase("laptop")).thenReturn(foundProducts);

        
        List<ProductoResponse> responseList = productoService.buscarPorNombre("laptop");

        
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Laptop Gaming", responseList.get(0).getNombre());
        assertEquals("Laptop Ultrabook", responseList.get(1).getNombre());
        verify(productoRepository, times(1)).findByNombreContainingIgnoreCase("laptop");
    }

    @Test
    @DisplayName("Debe buscar productos por categoría (case-insensitive)")
    void buscarPorCategoria_Success() {
        
        Producto p1 = Producto.builder().id(1L).nombre("Teclado").categoria("Perifericos").sku("T-001").build();
        Producto p2 = Producto.builder().id(2L).nombre("Mouse").categoria("Perifericos").sku("M-001").build();
        List<Producto> foundProducts = Arrays.asList(p1, p2);
        when(productoRepository.findByCategoriaIgnoreCase("perifericos")).thenReturn(foundProducts);

        
        List<ProductoResponse> responseList = productoService.buscarPorCategoria("perifericos");

        
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Teclado", responseList.get(0).getNombre());
        assertEquals("Mouse", responseList.get(1).getNombre());
        verify(productoRepository, times(1)).findByCategoriaIgnoreCase("perifericos");
    }

    @Test
    @DisplayName("Debe obtener solo productos activos")
    void obtenerProductosActivos_Success() {
        
        Producto p1 = Producto.builder().id(1L).nombre("Activo 1").activo(true).sku("A-001").build();
        Producto p2 = Producto.builder().id(2L).nombre("Inactivo 1").activo(false).sku("I-001").build();
        Producto p3 = Producto.builder().id(3L).nombre("Activo 2").activo(true).sku("A-002").build();
        List<Producto> activeProducts = Arrays.asList(p1, p3);
        when(productoRepository.findByActivoTrue()).thenReturn(activeProducts);

        
        List<ProductoResponse> responseList = productoService.obtenerProductosActivos();

        
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertTrue(responseList.stream().allMatch(ProductoResponse::getActivo));
        assertEquals("Activo 1", responseList.get(0).getNombre());
        verify(productoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Debe obtener el top 5 de productos más vendidos")
    void obtenerTop5ProductosMasVendidos_Success() {
        
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{10L, "Producto A", 100L},
            new Object[]{20L, "Producto B", 90L},
            new Object[]{30L, "Producto C", 80L},
            new Object[]{40L, "Producto D", 70L},
            new Object[]{50L, "Producto E", 60L}
        );
        when(ordenDetalleRepository.findTop5MostSoldProducts(any(PageRequest.class))).thenReturn(mockResults);

        
        List<ProductoResponse> responseList = productoService.obtenerTop5ProductosMasVendidos();

        
        assertNotNull(responseList);
        assertEquals(5, responseList.size());
        assertEquals(10L, responseList.get(0).getId());
        assertEquals("Producto A", responseList.get(0).getNombre());
        assertEquals(50L, responseList.get(4).getId());
        assertEquals("Producto E", responseList.get(4).getNombre());

        verify(ordenDetalleRepository, times(1)).findTop5MostSoldProducts(any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe retornar una lista vacía para top 5 productos más vendidos si no hay datos")
    void obtenerTop5ProductosMasVendidos_Empty() {
        
        when(ordenDetalleRepository.findTop5MostSoldProducts(any(PageRequest.class))).thenReturn(Collections.emptyList());

        
        List<ProductoResponse> responseList = productoService.obtenerTop5ProductosMasVendidos();

        
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(ordenDetalleRepository, times(1)).findTop5MostSoldProducts(any(PageRequest.class));
    }
}