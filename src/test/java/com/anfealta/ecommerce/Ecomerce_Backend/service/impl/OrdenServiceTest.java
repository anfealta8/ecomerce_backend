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

    // Datos de prueba
    private Usuario usuario;
    private Producto producto1;
    private Inventario inventario1;
    private OrdenDetalleRequest detalleRequest1;
    private OrdenRequest ordenRequest;
    private Orden ordenGuardada;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        // Configurar los valores de las propiedades @Value usando ReflectionTestUtils
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 0.8); // Alta probabilidad para probar el descuento
        ReflectionTestUtils.setField(ordenService, "minOrdersForFrequentCustomer", 5);
        ReflectionTestUtils.setField(ordenService, "frequentCustomerPeriodDays", 30);

        // Datos comunes para los tests
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
                .aplicarDescuentoAleatorio(false) // Por defecto no aplicar descuento aleatorio
                .build();

        // Orden de ejemplo que sería "guardada" por el repositorio
        ordenGuardada = new Orden();
        ordenGuardada.setId(1L);
        ordenGuardada.setUsuario(usuario);
        ordenGuardada.setFechaCreacion(LocalDateTime.now());
        ordenGuardada.setFechaActualizacion(LocalDateTime.now());
        ordenGuardada.setEstado(Orden.EstadoOrden.PENDIENTE);
        ordenGuardada.setSubtotal(new BigDecimal("200.00")); // 2 * 100
        ordenGuardada.setDescuentoTotal(BigDecimal.ZERO);
        ordenGuardada.setTotal(new BigDecimal("200.00"));

        OrdenDetalle od1 = new OrdenDetalle();
        od1.setId(1L);
        od1.setOrden(ordenGuardada);
        od1.setProducto(producto1);
        od1.setCantidad(detalleRequest1.getCantidad());
        od1.setPrecioUnitario(producto1.getPrecio());
        // CORRECCIÓN YA APLICADA: Usar métodos de BigDecimal para la multiplicación
        od1.setSubtotalLinea(producto1.getPrecio().multiply(BigDecimal.valueOf(detalleRequest1.getCantidad())));
        ordenGuardada.addDetalle(od1);
    }

    // --- Tests para crearOrden ---
    @Test
    @DisplayName("Debe crear una orden exitosamente sin descuentos")
    void crearOrden_Success_NoDiscounts() {
        // GIVEN
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false); // No es cliente frecuente
        when(ordenRepository.save(any(Orden.class))).thenReturn(ordenGuardada); // Devuelve la orden "guardada"

        // Configurar rango de fechas para que no aplique descuento por tiempo
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(2).format(FORMATTER));

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        assertEquals(ordenGuardada.getId(), response.getId());
        assertEquals(usuario.getId(), response.getUsuarioId());
        assertEquals(Orden.EstadoOrden.PENDIENTE, response.getEstado());
        // Comparaciones BigDecimal usando compareTo()
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
        verify(inventarioRepository, times(1)).save(any(Inventario.class)); // Verifica que el inventario se actualizó
        verify(ordenRepository, times(1)).save(any(Orden.class));
        verify(usuarioService, times(1)).esClienteFrecuente(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si el usuario no es encontrado al crear la orden")
    void crearOrden_UsuarioNotFound() {
        // GIVEN
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN & THEN
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
        // GIVEN
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.empty()); // Producto no encontrado

        // WHEN & THEN
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
        // GIVEN
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.empty()); // Inventario no encontrado

        // WHEN & THEN
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
        // GIVEN
        inventario1.setCantidadDisponible(1); // Stock insuficiente
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));

        // WHEN & THEN
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
        // GIVEN
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L); // Simula el ID asignado por el repositorio
            return orden;
        });

        // Subtotal esperado antes del 10%: 2 * 100.00 = 200.00
        // Descuento del 10%: 200.00 * 0.10 = 20.00
        // Total esperado: 200.00 - 20.00 = 180.00

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotal()));

        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("No debe aplicar el descuento del 10% cuando la orden está fuera del rango de tiempo")
    void crearOrden_DoNotApply10PercentDiscount_OutsideTimeRange() {
        // GIVEN
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

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getTotal()));

        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar el descuento aleatorio del 50% si las condiciones se cumplen")
    void crearOrden_Apply50PercentRandomDiscount() {
        // GIVEN
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 1.0); // 100% de probabilidad
        ordenRequest.setAplicarDescuentoAleatorio(true);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        // Marcamos este stubbing como lenient para evitar UnnecessaryStubbingException
        lenient().when(random.nextDouble()).thenReturn(0.5); // Simula que el random cumple la condición (0.5 < 1.0)
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        // Subtotal: 200.00
        // Descuento 10%: 20.00 -> Total = 180.00
        // Descuento 50% sobre 180.00: 90.00
        // Total final: 180.00 - 90.00 = 90.00
        // Descuento total aplicado: 20.00 + 90.00 = 110.00

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("110.00").compareTo(response.getDescuentoTotal())); // 20 + 90
        assertEquals(0, new BigDecimal("90.00").compareTo(response.getTotal())); // 200 - 110

        verify(random, times(1)).nextDouble();
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("No debe aplicar el descuento aleatorio del 50% si el random no cumple la condición")
    void crearOrden_DoNotApply50PercentRandomDiscount_RandomFail() {
        // GIVEN
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 0.1); // Baja probabilidad
        ordenRequest.setAplicarDescuentoAleatorio(true);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        // Marcamos este stubbing como lenient para evitar UnnecessaryStubbingException
        lenient().when(random.nextDouble()).thenReturn(0.9); // Simula que el random NO cumple la condición (0.9 > 0.1)
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        // Subtotal: 200.00
        // Descuento 10%: 20.00 -> Total = 180.00
        // Descuento 50% NO aplicado
        // Descuento total aplicado: 20.00
        // Total final: 180.00

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotal()));

        // Verificamos que nextDouble() fue llamado (aunque el valor no se usó para el descuento)
        verify(random, times(1)).nextDouble();
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("No debe aplicar el descuento aleatorio del 50% si aplicarDescuentoAleatorio es falso")
    void crearOrden_DoNotApply50PercentRandomDiscount_FlagFalse() {
        // GIVEN
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ordenRequest.setAplicarDescuentoAleatorio(false); // Bandera explícitamente en falso

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(false);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        // Subtotal: 200.00
        // Descuento 10%: 20.00 -> Total = 180.00
        // Descuento 50% NO aplicado (por flag)
        // Descuento total aplicado: 20.00
        // Total final: 180.00

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.getTotal()));

        verify(random, never()).nextDouble(); // random.nextDouble() no debe ser llamado
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar el descuento del 5% para cliente frecuente")
    void crearOrden_Apply5PercentFrequentCustomerDiscount() {
        // GIVEN
        // Descuentos por tiempo fuera de rango para aislar el descuento de cliente frecuente
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(2).format(FORMATTER));

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(usuario.getId(), 5, 30)).thenReturn(true); // Es cliente frecuente
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        // Subtotal: 200.00
        // Descuento 10% y 50% NO aplicados
        // Descuento 5% sobre 200.00: 10.00
        // Total final: 200.00 - 10.00 = 190.00
        // Descuento total aplicado: 10.00

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("10.00").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("190.00").compareTo(response.getTotal()));

        verify(usuarioService, times(1)).esClienteFrecuente(usuario.getId(), 5, 30);
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debe aplicar todos los descuentos: 10%, 50% aleatorio y 5% frecuente")
    void crearOrden_ApplyAllDiscounts() {
        // GIVEN
        ReflectionTestUtils.setField(ordenService, "descuentoFechaInicioStr", LocalDateTime.now().minusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "descuentoFechaFinStr", LocalDateTime.now().plusHours(1).format(FORMATTER));
        ReflectionTestUtils.setField(ordenService, "probabilidadDescuentoAleatorio", 1.0); // 100% de probabilidad
        ordenRequest.setAplicarDescuentoAleatorio(true);

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(inventarioRepository.findByProductoId(producto1.getId())).thenReturn(Optional.of(inventario1));
        when(usuarioService.esClienteFrecuente(anyLong(), anyInt(), anyInt())).thenReturn(true); // Es cliente frecuente
        // Marcamos este stubbing como lenient para evitar UnnecessaryStubbingException
        lenient().when(random.nextDouble()).thenReturn(0.5); // Simula que el random cumple la condición

        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        // Calculos:
        // Subtotal: 200.00
        // 1. Descuento 10%: 200.00 * 0.10 = 20.00. Total = 180.00. Descuento total = 20.00
        // 2. Descuento 50% aleatorio (sobre 180.00): 180.00 * 0.50 = 90.00. Total = 90.00. Descuento total = 20.00 + 90.00 = 110.00
        // 3. Descuento 5% cliente frecuente (sobre 90.00): 90.00 * 0.05 = 4.50. Total = 85.50. Descuento total = 110.00 + 4.50 = 114.50

        // WHEN
        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        // THEN
        assertNotNull(response);
        // Comparaciones BigDecimal usando compareTo()
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("114.50").compareTo(response.getDescuentoTotal()));
        assertEquals(0, new BigDecimal("85.50").compareTo(response.getTotal()));

        verify(random, times(1)).nextDouble();
        verify(usuarioService, times(1)).esClienteFrecuente(anyLong(), anyInt(), anyInt());
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }


    // --- Tests para obtenerOrdenPorId ---
    @Test
    @DisplayName("Debe obtener una orden por ID existente")
    void obtenerOrdenPorId_Found() {
        // GIVEN
        when(ordenRepository.findById(ordenGuardada.getId())).thenReturn(Optional.of(ordenGuardada));

        // WHEN
        Optional<OrdenResponse> response = ordenService.obtenerOrdenPorId(ordenGuardada.getId());

        // THEN
        assertTrue(response.isPresent());
        assertEquals(ordenGuardada.getId(), response.get().getId());
        assertEquals(ordenGuardada.getUsuario().getNombreUsuario(), response.get().getNombreUsuario());
        assertFalse(response.get().getDetalles().isEmpty());
        verify(ordenRepository, times(1)).findById(ordenGuardada.getId());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando la orden no se encuentra por ID")
    void obtenerOrdenPorId_NotFound() {
        // GIVEN
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN
        Optional<OrdenResponse> response = ordenService.obtenerOrdenPorId(99L);

        // THEN
        assertFalse(response.isPresent());
        verify(ordenRepository, times(1)).findById(99L);
    }

    // --- Tests para obtenerTodasLasOrdenes ---
    @Test
    @DisplayName("Debe obtener una lista de todas las ordenes")
    void obtenerTodasLasOrdenes_Success() {
        // GIVEN
        Orden otraOrden = new Orden();
        otraOrden.setId(2L);
        otraOrden.setUsuario(usuario);
        otraOrden.setFechaCreacion(LocalDateTime.now());
        otraOrden.setFechaActualizacion(LocalDateTime.now());
        otraOrden.setEstado(Orden.EstadoOrden.COMPLETADA);
        otraOrden.setSubtotal(new BigDecimal("50.00"));
        otraOrden.setTotal(new BigDecimal("50.00"));
        // Crear un detalle de orden correctamente con BigDecimal
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

        // WHEN
        List<OrdenResponse> responseList = ordenService.obtenerTodasLasOrdenes();

        // THEN
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(ordenGuardada.getId(), responseList.get(0).getId());
        assertEquals(otraOrden.getId(), responseList.get(1).getId());
        verify(ordenRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si no hay ordenes")
    void obtenerTodasLasOrdenes_EmptyList() {
        // GIVEN
        when(ordenRepository.findAll()).thenReturn(Collections.emptyList());

        // WHEN
        List<OrdenResponse> responseList = ordenService.obtenerTodasLasOrdenes();

        // THEN
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(ordenRepository, times(1)).findAll();
    }

    // --- Tests para obtenerOrdenesPorUsuario ---
    @Test
    @DisplayName("Debe obtener ordenes por ID de usuario existente")
    void obtenerOrdenesPorUsuario_UserFound_OrdersFound() {
        // GIVEN
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        List<Orden> userOrders = Collections.singletonList(ordenGuardada);
        when(ordenRepository.findByUsuario(usuario)).thenReturn(userOrders);

        // WHEN
        List<OrdenResponse> responseList = ordenService.obtenerOrdenesPorUsuario(usuario.getId());

        // THEN
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
        // GIVEN
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN & THEN
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
        // GIVEN
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(ordenRepository.findByUsuario(usuario)).thenReturn(Collections.emptyList());

        // WHEN
        List<OrdenResponse> responseList = ordenService.obtenerOrdenesPorUsuario(usuario.getId());

        // THEN
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(ordenRepository, times(1)).findByUsuario(usuario);
    }

    // --- Tests para actualizarEstadoOrden ---
    @Test
    @DisplayName("Debe actualizar el estado de una orden existente exitosamente")
    void actualizarEstadoOrden_Success() {
        Long ordenId = ordenGuardada.getId();
        Orden.EstadoOrden nuevoEstado = Orden.EstadoOrden.ENVIADA;

        // GIVEN
        when(ordenRepository.findById(ordenId)).thenReturn(Optional.of(ordenGuardada));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setFechaActualizacion(LocalDateTime.now());
            return orden;
        });

        // WHEN
        Optional<OrdenResponse> response = ordenService.actualizarEstadoOrden(ordenId, nuevoEstado);

        // THEN
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

        // GIVEN
        when(ordenRepository.findById(ordenId)).thenReturn(Optional.empty());

        // WHEN
        Optional<OrdenResponse> response = ordenService.actualizarEstadoOrden(ordenId, nuevoEstado);

        // THEN
        assertFalse(response.isPresent());
        verify(ordenRepository, times(1)).findById(ordenId);
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    // --- Tests para eliminarOrden ---
    @Test
    @DisplayName("Debe eliminar una orden existente y retornar true")
    void eliminarOrden_Success() {
        // GIVEN
        when(ordenRepository.existsById(ordenGuardada.getId())).thenReturn(true);
        doNothing().when(ordenRepository).deleteById(ordenGuardada.getId());

        // WHEN
        boolean eliminado = ordenService.eliminarOrden(ordenGuardada.getId());

        // THEN
        assertTrue(eliminado);
        verify(ordenRepository, times(1)).existsById(ordenGuardada.getId());
        verify(ordenRepository, times(1)).deleteById(ordenGuardada.getId());
    }

    @Test
    @DisplayName("Debe retornar false cuando se intenta eliminar una orden no existente")
    void eliminarOrden_NotFound() {
        // GIVEN
        when(ordenRepository.existsById(99L)).thenReturn(false);

        // WHEN
        boolean eliminado = ordenService.eliminarOrden(99L);

        // THEN
        assertFalse(eliminado);
        verify(ordenRepository, times(1)).existsById(99L);
        verify(ordenRepository, never()).deleteById(anyLong());
    }
}