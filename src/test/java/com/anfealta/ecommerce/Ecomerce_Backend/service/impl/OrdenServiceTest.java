package com.anfealta.ecommerce.ecomerce_backend.service.impl;

import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenDetalleRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Inventario;
import com.anfealta.ecommerce.ecomerce_backend.entity.Orden;
import com.anfealta.ecommerce.ecomerce_backend.entity.OrdenDetalle;
import com.anfealta.ecommerce.ecomerce_backend.entity.Producto;
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;
import com.anfealta.ecommerce.ecomerce_backend.repository.InventarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.OrdenRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.ProductoRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.UsuarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.service.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private InventarioRepository inventarioRepository;
    @Mock
    private UsuarioService usuarioService;

    @Spy
    private Random random;

    @InjectMocks
    private OrdenServiceImpl ordenService;

    private Usuario usuario;
    private Producto producto1;
    private Inventario inventario1;
    private OrdenDetalleRequest detalleRequest1;
    private OrdenRequest ordenRequest;
    private Orden ordenGuardada;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 0.8); 
        ReflectionTestUtils.setField(ordenService, "minOrdersForFrequentCustomer", 5);
        ReflectionTestUtils.setField(ordenService, "frequentCustomerPeriodDays", 30);

        usuario = Usuario.builder()
                .id(1L)
                .nombreUsuario("testuser")
                .email("test@example.com")
                .build();

        producto1 = Producto.builder()
                .id(101L)
                .nombre("Producto A")
                .sku("PA-001")
                .precio(new BigDecimal("100.00"))
                .activo(true)
                .build();

        inventario1 = Inventario.builder()
                .id(201L)
                .producto(producto1)
                .cantidadDisponible(50)
                .cantidadReservada(0)
                .cantidadMinima(5)
                .build();

        detalleRequest1 = OrdenDetalleRequest.builder()
                .productoId(producto1.getId())
                .cantidad(2)
                .build();

        ordenRequest = OrdenRequest.builder()
                .usuarioId(usuario.getId())
                .detalles(Collections.singletonList(detalleRequest1))
                .aplicarDescuentoAleatorio(false) 
                .build();

        
        ordenGuardada = new Orden();
        ordenGuardada.setId(1L);
        ordenGuardada.setUsuario(usuario);
        ordenGuardada.setFechaCreacion(LocalDateTime.now());
        ordenGuardada.setFechaActualizacion(LocalDateTime.now());
        ordenGuardada.setEstado(Orden.EstadoOrden.PENDIENTE);
        ordenGuardada.setSubtotal(new BigDecimal("200.00")); 
        ordenGuardada.setDescuentoTotal(BigDecimal.ZERO);
        ordenGuardada.setTotal(new BigDecimal("200.00"));

        OrdenDetalle od1 = new OrdenDetalle();
        od1.setId(1L);
        od1.setOrden(ordenGuardada);
        od1.setProducto(producto1);
        od1.setCantidad(detalleRequest1.getCantidad());
        od1.setPrecioUnitario(producto1.getPrecio());
        od1.setSubtotalLinea(producto1.getPrecio().multiply(BigDecimal.valueOf(detalleRequest1.getCantidad())));
        ordenGuardada.addDetalle(od1);
    }

    @Test
    @DisplayName("Debe crear una orden exitosamente sin descuentos")
    void crearOrden_Success_NoDiscounts() {
        
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false); 
        when(ordenRepository.save(any(Orden.class))).thenReturn(ordenGuardada); 

        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(2).format(FORMATTER));

        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        assertEquals(ordenGuardada.getId(), response.getId());
        assertEquals(usuario.getId(), response.getUsuarioId());
        assertEquals(Orden.EstadoOrden.PENDIENTE, response.getEstado());
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getTotal()));
        assertFalse(response.getDetalles().isEmpty());
        assertEquals(1, response.getDetalles().size());
        assertEquals(producto1.getId(), response.getDetalles().get(0).getProductoId());
        assertEquals(detalleRequest1.getCantidad(), response.getDetalles().get(0).getCantidad());

        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(productoRepository, times(1)).findById(producto1.getId());
        verify(inventarioRepository, times(1)).findByProductoId(producto1.getId());
        verify(inventarioRepository, times(1)).save(any(Inventario.class)); 
        verify(ordenRepository, times(1)).save(any(Orden.class));
        verify(usuarioService, times(1)).esClienteFrecuente(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si el usuario no es encontrado al crear la orden")
    void crearOrden_UsuarioNotFound() {
        
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ordenService.crearOrden(ordenRequest);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Usuario no encontrado con ID: " + usuario.getId()));

        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(productoRepository, never()).findById(anyLong());
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si un producto en el detalle no es encontrado")
    void crearOrden_ProductoNotFound() {
        
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.empty()); 

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ordenService.crearOrden(ordenRequest);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Producto no encontrado con ID: " + producto1.getId()));

        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(productoRepository, times(1)).findById(producto1.getId());
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si el inventario para un producto no es encontrado")
    void crearOrden_InventarioNotFound() {
        
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.empty()); 

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ordenService.crearOrden(ordenRequest);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Inventario no encontrado para el producto con ID: " + producto1.getId()));

        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(productoRepository, times(1)).findById(producto1.getId());
        verify(inventarioRepository, times(1)).findByProductoId(producto1.getId());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si no hay suficiente stock para un producto")
    void crearOrden_InsufficientStock() {
        
        inventario1.setCantidadDisponible(1); 
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ordenService.crearOrden(ordenRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No hay suficiente stock para el producto " + producto1.getNombre()));

        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(productoRepository, times(1)).findById(producto1.getId());
        verify(inventarioRepository, times(1)).findByProductoId(producto1.getId());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar el descuento del 10% cuando la orden está dentro del rango de tiempo")
    void crearOrden_Apply10PercentDiscount() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L); 
            return orden;
        });

        
        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotal()));

        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("No debe aplicar el descuento del 10% cuando la orden está fuera del rango de tiempo")
    void crearOrden_DoNotApply10PercentDiscount_OutsideTimeRange() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(2).format(FORMATTER));

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getTotal()));

        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar el descuento aleatorio del 50% si las condiciones se cumplen")
    void crearOrden_Apply50PercentRandomDiscount() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 1.0); 
        ordenRequest.setAplicarDescuentoAleatorio(true);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        lenient().when(random.nextDouble()).thenReturn(0.5); 
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        
        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("110.00").compareTo(response.getDescuentoTotal())); 
        assertEquals(0, new BigDecimal("90.00").compareTo(response.getTotal())); 

        verify(random, times(1)).nextDouble();
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("No debe aplicar el descuento aleatorio del 50% si el random no cumple la condición")
    void crearOrden_DoNotApply50PercentRandomDiscount_RandomFail() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 0.1); 
        ordenRequest.setAplicarDescuentoAleatorio(true);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        
        lenient().when(random.nextDouble()).thenReturn(0.9); 
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        
        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotal()));

        
        verify(random, times(1)).nextDouble();
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("No debe aplicar el descuento aleatorio del 50% si aplicarDescuentoAleatorio es falso")
    void crearOrden_DoNotApply50PercentRandomDiscount_FlagFalse() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ordenRequest.setAplicarDescuentoAleatorio(false); 

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        

        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotal()));

        verify(random, never()).nextDouble(); 
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar el descuento del 5% para cliente frecuente")
    void crearOrden_Apply5PercentFrequentCustomerDiscount() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(2).format(FORMATTER));

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(usuario.getId(), 5, 30)).thenReturn(true); 
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        

        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("10.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("190.00").compareTo(response.getTotal()));

        verify(usuarioService, times(1)).esClienteFrecuente(usuario.getId(), 5, 30);
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar todos los descuentos: 10%, 50% aleatorio y 5% frecuente")
    void crearOrden_ApplyAllDiscounts() {
        
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 1.0); 
        ordenRequest.setAplicarDescuentoAleatorio(true);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(true); 
        
        lenient().when(random.nextDouble()).thenReturn(0.5); 

        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        
        
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        
        assertNotNull(response);
        
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("114.50").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("85.50").compareTo(response.getTotal()));

        verify(random, times(1)).nextDouble();
        verify(usuarioService, times(1)).esClienteFrecuente(anyLong(), anyInt(), anyInt());
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }


    
    @Test
    @DisplayName("Debe obtener una orden por ID existente")
    void obtenerOrdenPorId_Found() {
        
        when(ordenRepository.findById(ordenGuardada.getId())).thenReturn(Optional.of(ordenGuardada));

        
        Optional<OrdenResponse> response = ordenService.obtenerOrdenPorId(ordenGuardada.getId());

        
        assertTrue(response.isPresent());
        assertEquals(ordenGuardada.getId(), response.get().getId());
        assertEquals(ordenGuardada.getUsuario().getNombreUsuario(), response.get().getNombreUsuario());
        assertFalse(response.get().getDetalles().isEmpty());
        verify(ordenRepository, times(1)).findById(ordenGuardada.getId());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando la orden no se encuentra por ID")
    void obtenerOrdenPorId_NotFound() {
        
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        
        Optional<OrdenResponse> response = ordenService.obtenerOrdenPorId(99L);

        
        assertFalse(response.isPresent());
        verify(ordenRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe obtener una lista de todas las ordenes")
    void obtenerTodasLasOrdenes_Success() {
        
        Orden otraOrden = new Orden();
        otraOrden.setId(2L);
        otraOrden.setUsuario(usuario);
        otraOrden.setFechaCreacion(LocalDateTime.now());
        otraOrden.setFechaActualizacion(LocalDateTime.now());
        otraOrden.setEstado(Orden.EstadoOrden.COMPLETADA);
        otraOrden.setSubtotal(new BigDecimal("50.00"));
        otraOrden.setTotal(new BigDecimal("50.00"));
        
        OrdenDetalle otraOrdenDetalle = new OrdenDetalle();
        otraOrdenDetalle.setId(2L);
        otraOrdenDetalle.setOrden(otraOrden);
        otraOrdenDetalle.setProducto(producto1);
        otraOrdenDetalle.setCantidad(1);
        otraOrdenDetalle.setPrecioUnitario(new BigDecimal("50.00"));
        otraOrdenDetalle.setSubtotalLinea(new BigDecimal("50.00"));
        otraOrden.addDetalle(otraOrdenDetalle);

        List<Orden> ordenes = Arrays.asList(ordenGuardada, otraOrden);
        when(ordenRepository.findAll()).thenReturn(ordenes);

        
        List<OrdenResponse> responseList = ordenService.obtenerTodasLasOrdenes();

        
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(ordenGuardada.getId(), responseList.get(0).getId());
        assertEquals(otraOrden.getId(), responseList.get(1).getId());
        verify(ordenRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si no hay ordenes")
    void obtenerTodasLasOrdenes_EmptyList() {
        
        when(ordenRepository.findAll()).thenReturn(Collections.emptyList());

        
        List<OrdenResponse> responseList = ordenService.obtenerTodasLasOrdenes();

        
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(ordenRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Debe obtener ordenes por ID de usuario existente")
    void obtenerOrdenesPorUsuario_UserFound_OrdersFound() {
        
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        List<Orden> userOrders = Collections.singletonList(ordenGuardada);
        when(ordenRepository.findByUsuario(usuario)).thenReturn(userOrders);

        
        List<OrdenResponse> responseList = ordenService.obtenerOrdenesPorUsuario(usuario.getId());

        
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        assertEquals(usuario.getId(), responseList.get(0).getUsuarioId());
        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(ordenRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si el usuario no es encontrado al buscar sus ordenes")
    void obtenerOrdenesPorUsuario_UserNotFound() {
        
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ordenService.obtenerOrdenesPorUsuario(99L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Usuario no encontrado con ID: 99"));

        verify(usuarioRepository, times(1)).findById(99L);
        verify(ordenRepository, never()).findByUsuario(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si el usuario existe pero no tiene ordenes")
    void obtenerOrdenesPorUsuario_UserFound_NoOrders() {
        
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(ordenRepository.findByUsuario(usuario)).thenReturn(Collections.emptyList());

        
        List<OrdenResponse> responseList = ordenService.obtenerOrdenesPorUsuario(usuario.getId());

        
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(ordenRepository, times(1)).findByUsuario(usuario);
    }

    
    @Test
    @DisplayName("Debe actualizar el estado de una orden existente exitosamente")
    void actualizarEstadoOrden_Success() {
        Long ordenId = ordenGuardada.getId();
        Orden.EstadoOrden nuevoEstado = Orden.EstadoOrden.ENVIADA;

        
        when(ordenRepository.findById(ordenId)).thenReturn(Optional.of(ordenGuardada));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setFechaActualizacion(LocalDateTime.now());
            return orden;
        });

        
        Optional<OrdenResponse> response = ordenService.actualizarEstadoOrden(ordenId, nuevoEstado);

        
        assertTrue(response.isPresent());
        assertEquals(ordenId, response.get().getId());
        assertEquals(nuevoEstado, response.get().getEstado());
        verify(ordenRepository, times(1)).findById(ordenId);
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si la orden a actualizar no se encuentra")
    void actualizarEstadoOrden_NotFound() {
        Long ordenId = 99L;
        Orden.EstadoOrden nuevoEstado = Orden.EstadoOrden.ENVIADA;

        
        when(ordenRepository.findById(ordenId)).thenReturn(Optional.empty());

        
        Optional<OrdenResponse> response = ordenService.actualizarEstadoOrden(ordenId, nuevoEstado);

        
        assertFalse(response.isPresent());
        verify(ordenRepository, times(1)).findById(ordenId);
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe eliminar una orden existente y retornar true")
    void eliminarOrden_Success() {
        
        when(ordenRepository.existsById(ordenGuardada.getId())).thenReturn(true);
        doNothing().when(ordenRepository).deleteById(ordenGuardada.getId());

        
        boolean eliminado = ordenService.eliminarOrden(ordenGuardada.getId());

        
        assertTrue(eliminado);
        verify(ordenRepository, times(1)).existsById(ordenGuardada.getId());
        verify(ordenRepository, times(1)).deleteById(ordenGuardada.getId());
    }

    @Test
    @DisplayName("Debe retornar false cuando se intenta eliminar una orden no existente")
    void eliminarOrden_NotFound() {
        
        when(ordenRepository.existsById(99L)).thenReturn(false);

        
        boolean eliminado = ordenService.eliminarOrden(99L);

        
        assertFalse(eliminado);
        verify(ordenRepository, times(1)).existsById(99L);
        verify(ordenRepository, never()).deleteById(anyLong());
    }
}