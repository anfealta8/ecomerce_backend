package com.anfealta.ecommerce.ecomerce_backend.service; 

import com.anfealta.ecommerce.ecomerce_backend.dto.InventarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.InventarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Inventario;
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto;
import com.anfealta.ecommerce.ecomerce_backend.repository.InventarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.ProductoRepository;
import com.anfealta.ecommerce.ecomerce_backend.service.InventarioService; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private InventarioService inventarioService; 

    private Producto productoExistente;
    private InventarioRequest inventarioRequest;
    private Inventario inventarioExistente;
    private Inventario inventarioNuevoProducto; 

    @BeforeEach
    void setUp() {
        productoExistente = Producto.builder()
                .id(1L)
                .nombre("Laptop Gaming Pro")
                .sku("LGP-001")
                .build();

        Producto otroProducto = Producto.builder()
                .id(2L)
                .nombre("Monitor UltraWide")
                .sku("MUW-001")
                .build();

        inventarioRequest = InventarioRequest.builder()
                .productoId(productoExistente.getId())
                .cantidadDisponible(100)
                .cantidadReservada(10)
                .cantidadMinima(5)
                .build();

        inventarioExistente = Inventario.builder()
                .id(10L)
                .producto(productoExistente)
                .cantidadDisponible(100)
                .cantidadReservada(10)
                .cantidadMinima(5)
                .fechaCreacion(LocalDateTime.now().minusDays(5))
                .fechaActualizacion(LocalDateTime.now().minusDays(5))
                .build();
        
        inventarioNuevoProducto = Inventario.builder()
                .id(11L)
                .producto(otroProducto)
                .cantidadDisponible(50)
                .cantidadReservada(5)
                .cantidadMinima(2)
                .fechaCreacion(LocalDateTime.now().minusDays(2))
                .fechaActualizacion(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    @DisplayName("Debe crear un nuevo registro de inventario exitosamente")
    void crearInventario_Success() {
        
        when(productoRepository.findById(inventarioRequest.getProductoId())).thenReturn(Optional.of(productoExistente));
        when(inventarioRepository.findByProductoId(productoExistente.getId())).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioExistente); 

        
        InventarioResponse response = inventarioService.crearInventario(inventarioRequest);

        
        assertNotNull(response);
        assertEquals(inventarioExistente.getId(), response.getId());
        assertEquals(inventarioRequest.getCantidadDisponible(), response.getCantidadDisponible());
        assertEquals(productoExistente.getId(), response.getProductoId());
        assertEquals(productoExistente.getNombre(), response.getNombreProducto());
        assertEquals(productoExistente.getSku(), response.getSkuProducto());

        verify(productoRepository, times(1)).findById(inventarioRequest.getProductoId());
        verify(inventarioRepository, times(1)).findByProductoId(productoExistente.getId());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si el producto no es encontrado al crear inventario")
    void crearInventario_ProductoNotFound() {
        
        when(productoRepository.findById(inventarioRequest.getProductoId())).thenReturn(Optional.empty());

         
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.crearInventario(inventarioRequest);
        });

        assertTrue(exception.getMessage().contains("Producto no encontrado con ID: " + inventarioRequest.getProductoId()));

        verify(productoRepository, times(1)).findById(inventarioRequest.getProductoId());
        verify(inventarioRepository, never()).findByProductoId(anyLong()); 
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si ya existe un registro de inventario para el producto")
    void crearInventario_InventarioAlreadyExists() {
        
        when(productoRepository.findById(inventarioRequest.getProductoId())).thenReturn(Optional.of(productoExistente));
        when(inventarioRepository.findByProductoId(productoExistente.getId())).thenReturn(Optional.of(inventarioExistente));

         
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.crearInventario(inventarioRequest);
        });

        assertTrue(exception.getMessage().contains("Ya existe un registro de inventario para el producto con ID: " + productoExistente.getId()));

        verify(productoRepository, times(1)).findById(inventarioRequest.getProductoId());
        verify(inventarioRepository, times(1)).findByProductoId(productoExistente.getId());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    
    @Test
    @DisplayName("Debe obtener un registro de inventario por su ID existente")
    void obtenerInventarioPorId_Found() {
        
        when(inventarioRepository.findById(inventarioExistente.getId())).thenReturn(Optional.of(inventarioExistente));

        
        Optional<InventarioResponse> response = inventarioService.obtenerInventarioPorId(inventarioExistente.getId());

        
        assertTrue(response.isPresent());
        assertEquals(inventarioExistente.getId(), response.get().getId());
        assertEquals(inventarioExistente.getCantidadDisponible(), response.get().getCantidadDisponible());
        assertEquals(productoExistente.getNombre(), response.get().getNombreProducto());

        verify(inventarioRepository, times(1)).findById(inventarioExistente.getId());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando el registro de inventario no se encuentra por ID")
    void obtenerInventarioPorId_NotFound() {
        
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        
        Optional<InventarioResponse> response = inventarioService.obtenerInventarioPorId(99L);

        
        assertFalse(response.isPresent());
        verify(inventarioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe obtener un registro de inventario por ID de producto existente")
    void obtenerInventarioPorProductoId_Found() {
        
        when(inventarioRepository.findByProductoId(productoExistente.getId())).thenReturn(Optional.of(inventarioExistente));

        
        Optional<InventarioResponse> response = inventarioService.obtenerInventarioPorProductoId(productoExistente.getId());

        
        assertTrue(response.isPresent());
        assertEquals(inventarioExistente.getId(), response.get().getId());
        assertEquals(productoExistente.getId(), response.get().getProductoId());
        assertEquals(productoExistente.getNombre(), response.get().getNombreProducto());

        verify(inventarioRepository, times(1)).findByProductoId(productoExistente.getId());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando el registro de inventario no se encuentra por ID de producto")
    void obtenerInventarioPorProductoId_NotFound() {
        
        when(inventarioRepository.findByProductoId(99L)).thenReturn(Optional.empty());

        
        Optional<InventarioResponse> response = inventarioService.obtenerInventarioPorProductoId(99L);

        
        assertFalse(response.isPresent());
        verify(inventarioRepository, times(1)).findByProductoId(99L);
    }

    @Test
    @DisplayName("Debe obtener una lista de todos los registros de inventario")
    void obtenerTodosLosInventarios_Success() {
        
        Inventario inv1 = Inventario.builder().id(1L).producto(productoExistente).cantidadDisponible(50).build();
        Inventario inv2 = Inventario.builder().id(2L).producto(Inventario.builder().producto(Producto.builder().id(3L).nombre("Teclado").sku("TEC-001").build()).build().getProducto()).cantidadDisponible(20).build();
        List<Inventario> inventarios = Arrays.asList(inv1, inv2);
        when(inventarioRepository.findAll()).thenReturn(inventarios);

        
        List<InventarioResponse> responseList = inventarioService.obtenerTodosLosInventarios();

        
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(50, responseList.get(0).getCantidadDisponible());
        assertEquals(20, responseList.get(1).getCantidadDisponible());
        assertEquals("Laptop Gaming Pro", responseList.get(0).getNombreProducto()); 
        assertEquals("Teclado", responseList.get(1).getNombreProducto()); 

        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si no hay registros de inventario")
    void obtenerTodosLosInventarios_EmptyList() {
        
        when(inventarioRepository.findAll()).thenReturn(Collections.emptyList());

        
        List<InventarioResponse> responseList = inventarioService.obtenerTodosLosInventarios();

        
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(inventarioRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Debe actualizar un registro de inventario exitosamente (misma ID de producto)")
    void actualizarInventario_Success_SameProduct() {
        Long inventarioId = inventarioExistente.getId();
        InventarioRequest updateRequest = InventarioRequest.builder()
                .productoId(productoExistente.getId()) 
                .cantidadDisponible(120)
                .cantidadReservada(15)
                .cantidadMinima(10)
                .build();

        
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioExistente));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario inv = invocation.getArgument(0);
            inv.setFechaActualizacion(LocalDateTime.now()); 
            return inv;
        });

        
        Optional<InventarioResponse> response = inventarioService.actualizarInventario(inventarioId, updateRequest);

        
        assertTrue(response.isPresent());
        assertEquals(inventarioId, response.get().getId());
        assertEquals(updateRequest.getCantidadDisponible(), response.get().getCantidadDisponible());
        assertEquals(updateRequest.getCantidadReservada(), response.get().getCantidadReservada());
        assertEquals(updateRequest.getCantidadMinima(), response.get().getCantidadMinima());
        assertEquals(productoExistente.getId(), response.get().getProductoId());

        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(productoRepository, never()).findById(anyLong()); 
        verify(inventarioRepository, never()).findByProductoId(anyLong()); 
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe actualizar un registro de inventario exitosamente (cambio de producto a uno sin inventario)")
    void actualizarInventario_Success_ChangeProduct_NoExistingInventory() {
        Long inventarioId = inventarioExistente.getId();
        Producto nuevoProducto = Producto.builder().id(3L).nombre("Mouse Inalambrico").sku("MI-001").build();
        InventarioRequest updateRequest = InventarioRequest.builder()
                .productoId(nuevoProducto.getId()) 
                .cantidadDisponible(50)
                .cantidadReservada(5)
                .cantidadMinima(2)
                .build();

        
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioExistente));
        when(productoRepository.findById(nuevoProducto.getId())).thenReturn(Optional.of(nuevoProducto));
        when(inventarioRepository.findByProductoId(nuevoProducto.getId())).thenReturn(Optional.empty()); 

        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario inv = invocation.getArgument(0);
            inv.setFechaActualizacion(LocalDateTime.now());
            return inv;
        });

        
        Optional<InventarioResponse> response = inventarioService.actualizarInventario(inventarioId, updateRequest);

        
        assertTrue(response.isPresent());
        assertEquals(inventarioId, response.get().getId());
        assertEquals(nuevoProducto.getId(), response.get().getProductoId());
        assertEquals(nuevoProducto.getNombre(), response.get().getNombreProducto());
        assertEquals(updateRequest.getCantidadDisponible(), response.get().getCantidadDisponible());

        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(productoRepository, times(1)).findById(nuevoProducto.getId());
        verify(inventarioRepository, times(1)).findByProductoId(nuevoProducto.getId());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si el registro de inventario a actualizar no se encuentra")
    void actualizarInventario_NotFound() {
        Long inventarioId = 99L; 
        InventarioRequest updateRequest = InventarioRequest.builder()
                .productoId(productoExistente.getId())
                .cantidadDisponible(10)
                .build();

        
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.empty());

        
        Optional<InventarioResponse> response = inventarioService.actualizarInventario(inventarioId, updateRequest);

        
        assertFalse(response.isPresent());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(productoRepository, never()).findById(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si el nuevo producto no es encontrado al actualizar inventario")
    void actualizarInventario_NuevoProductoNotFound() {
        Long inventarioId = inventarioExistente.getId();
        Long nonExistentProductId = 99L;
        InventarioRequest updateRequest = InventarioRequest.builder()
                .productoId(nonExistentProductId) 
                .cantidadDisponible(50)
                .build();

        
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioExistente));
        when(productoRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

         
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.actualizarInventario(inventarioId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("Nuevo producto no encontrado con ID: " + nonExistentProductId));

        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(productoRepository, times(1)).findById(nonExistentProductId);
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si el nuevo producto ya tiene un registro de inventario (conflicto)")
    void actualizarInventario_NuevoProductoAlreadyHasInventario() {
        Long inventarioId = inventarioExistente.getId(); 
        Long conflictingProductId = inventarioNuevoProducto.getProducto().getId(); 
        
        InventarioRequest updateRequest = InventarioRequest.builder()
                .productoId(conflictingProductId) 
                .cantidadDisponible(50)
                .build();

        
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioExistente));
        when(productoRepository.findById(conflictingProductId)).thenReturn(Optional.of(inventarioNuevoProducto.getProducto()));
        when(inventarioRepository.findByProductoId(conflictingProductId)).thenReturn(Optional.of(inventarioNuevoProducto));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.actualizarInventario(inventarioId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("El nuevo producto con ID: " + conflictingProductId + " ya tiene un registro de inventario."));

        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(productoRepository, times(1)).findById(conflictingProductId);
        verify(inventarioRepository, times(2)).findByProductoId(conflictingProductId); 
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }


    @Test
    @DisplayName("Debe eliminar un registro de inventario existente y retornar true")
    void eliminarInventario_Success() {
        
        when(inventarioRepository.existsById(inventarioExistente.getId())).thenReturn(true);
        doNothing().when(inventarioRepository).deleteById(inventarioExistente.getId());

        
        boolean eliminado = inventarioService.eliminarInventario(inventarioExistente.getId());

        
        assertTrue(eliminado);
        verify(inventarioRepository, times(1)).existsById(inventarioExistente.getId());
        verify(inventarioRepository, times(1)).deleteById(inventarioExistente.getId());
    }

    @Test
    @DisplayName("Debe retornar false cuando se intenta eliminar un registro de inventario no existente")
    void eliminarInventario_NotFound() {
        
        when(inventarioRepository.existsById(99L)).thenReturn(false);

        
        boolean eliminado = inventarioService.eliminarInventario(99L);

        
        assertFalse(eliminado);
        verify(inventarioRepository, times(1)).existsById(99L);
        verify(inventarioRepository, never()).deleteById(anyLong());
    }
}