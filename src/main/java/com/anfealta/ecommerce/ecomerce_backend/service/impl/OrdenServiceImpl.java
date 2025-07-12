package com.anfealta.ecommerce.ecomerce_backend.service.impl;

import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenDetalleRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.OrdenDetalleResponse;
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
import com.anfealta.ecommerce.ecomerce_backend.service.OrdenService;
import com.anfealta.ecommerce.ecomerce_backend.service.UsuarioService; 
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random; 
import java.util.stream.Collectors;

@Service
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioService usuarioService; 

    @Value("${app.descuentos.fecha-inicio}")
    private String descuentoFechaInicioStr;
    @Value("${app.descuentos.fecha-fin}")
    private String descuentoFechaFinStr;
    @Value("${app.descuentos.probabilidad-aleatorio}")
    private double probabilidadDescuentoAleatorio;
    @Value("${app.descuentos.cliente-frecuente.min-ordenes}")
    private int minOrdersForFrequentCustomer;
    @Value("${app.descuentos.cliente-frecuente.periodo-dias}")
    private int frequentCustomerPeriodDays;

    private final Random random = new Random();

    public OrdenServiceImpl(OrdenRepository ordenRepository, UsuarioRepository usuarioRepository,
                            ProductoRepository productoRepository, InventarioRepository inventarioRepository,
                            UsuarioService usuarioService) {
        this.ordenRepository = ordenRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.usuarioService = usuarioService;
    }

    private OrdenResponse mapToDto(Orden orden) {
        List<OrdenDetalleResponse> detallesDto = orden.getDetalles().stream()
                .map(detalle -> OrdenDetalleResponse.builder()
                        .id(detalle.getId())
                        .productoId(detalle.getProducto().getId())
                        .nombreProducto(detalle.getProducto().getNombre())
                        .skuProducto(detalle.getProducto().getSku())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotalLinea(detalle.getSubtotalLinea())
                        .build())
                .collect(Collectors.toList());

        return OrdenResponse.builder()
                .id(orden.getId())
                .usuarioId(orden.getUsuario().getId())
                .nombreUsuario(orden.getUsuario().getNombreUsuario())
                .fechaCreacion(orden.getFechaCreacion())
                .fechaActualizacion(orden.getFechaActualizacion())
                .estado(orden.getEstado())
                .subtotal(orden.getSubtotal())
                .descuentoTotal(orden.getDescuentoTotal())
                .total(orden.getTotal())
                .detalles(detallesDto)
                .build();
    }

    @Override
    @Transactional
    public OrdenResponse crearOrden(OrdenRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + request.getUsuarioId()));

        Orden nuevaOrden = new Orden();
        nuevaOrden.setUsuario(usuario);
        nuevaOrden.setEstado(Orden.EstadoOrden.PENDIENTE); 

        BigDecimal subtotalCalculado = BigDecimal.ZERO;
        BigDecimal descuentoTotalAplicado = BigDecimal.ZERO;
        LocalDateTime ahora = LocalDateTime.now();

        LocalDateTime fechaInicioDescuento = LocalDateTime.parse(descuentoFechaInicioStr);
        LocalDateTime fechaFinDescuento = LocalDateTime.parse(descuentoFechaFinStr);
        boolean isWithinTimeRange = ahora.isAfter(fechaInicioDescuento) && ahora.isBefore(fechaFinDescuento);

        boolean isFrequentCustomer = usuarioService.esClienteFrecuente(usuario.getId(), minOrdersForFrequentCustomer, frequentCustomerPeriodDays);

        for (OrdenDetalleRequest detalleRequest : request.getDetalles()) {
            Producto producto = productoRepository.findById(detalleRequest.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + detalleRequest.getProductoId()));

            Inventario inventario = inventarioRepository.findByProductoId(producto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventario no encontrado para el producto con ID: " + producto.getId()));

            if (inventario.getCantidadDisponible() < detalleRequest.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente stock para el producto " + producto.getNombre());
            }

            OrdenDetalle detalle = new OrdenDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleRequest.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); 
            detalle.setSubtotalLinea(producto.getPrecio().multiply(BigDecimal.valueOf(detalleRequest.getCantidad())));

            nuevaOrden.addDetalle(detalle); 
            subtotalCalculado = subtotalCalculado.add(detalle.getSubtotalLinea());

            inventario.setCantidadDisponible(inventario.getCantidadDisponible() - detalleRequest.getCantidad());
            inventarioRepository.save(inventario);
        }

        nuevaOrden.setSubtotal(subtotalCalculado);

        BigDecimal totalOrden = subtotalCalculado;

        if (isWithinTimeRange) {
            BigDecimal descuento10Porc = totalOrden.multiply(BigDecimal.valueOf(0.10));
            totalOrden = totalOrden.subtract(descuento10Porc);
            descuentoTotalAplicado = descuentoTotalAplicado.add(descuento10Porc);
        }

        if (isWithinTimeRange && request.getAplicarDescuentoAleatorio() && random.nextDouble() < probabilidadDescuentoAleatorio) {
            BigDecimal descuento50Porc = totalOrden.multiply(BigDecimal.valueOf(0.50)); // Aplica sobre el subtotal ya con 10%
            totalOrden = totalOrden.subtract(descuento50Porc);
            descuentoTotalAplicado = descuentoTotalAplicado.add(descuento50Porc);
        }

        if (isFrequentCustomer) {
            BigDecimal descuento5Porc = totalOrden.multiply(BigDecimal.valueOf(0.05)); // Aplica sobre el total actual
            totalOrden = totalOrden.subtract(descuento5Porc);
            descuentoTotalAplicado = descuentoTotalAplicado.add(descuento5Porc);
        }

        nuevaOrden.setDescuentoTotal(descuentoTotalAplicado);
        nuevaOrden.setTotal(totalOrden);

        nuevaOrden = ordenRepository.save(nuevaOrden);
        return mapToDto(nuevaOrden);
    }

    @Override
    public Optional<OrdenResponse> obtenerOrdenPorId(Long id) {
        return ordenRepository.findById(id).map(this::mapToDto);
    }

    @Override
    public List<OrdenResponse> obtenerTodasLasOrdenes() {
        return ordenRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenResponse> obtenerOrdenesPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + usuarioId));
        return ordenRepository.findByUsuario(usuario).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<OrdenResponse> actualizarEstadoOrden(Long id, Orden.EstadoOrden nuevoEstado) {
        return ordenRepository.findById(id).map(orden -> {
            orden.setEstado(nuevoEstado);
            return mapToDto(ordenRepository.save(orden));
        });
    }

    @Override
    @Transactional
    public boolean eliminarOrden(Long id) {
        if (ordenRepository.existsById(id)) {
            ordenRepository.deleteById(id);
            return true;
        }
        return false;
    }
}