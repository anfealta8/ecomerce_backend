package com.anfealta.ecommerce.ecomerce_backend.service.impl;

import com.anfealta.ecommerce.ecomerce_backend.dto.TopFrequentCustomerResponse; // Importa el nuevo DTO
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.RolUsuario;
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;
import com.anfealta.ecommerce.ecomerce_backend.repository.UsuarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.repository.OrdenRepository; // Importa OrdenRepository
import com.anfealta.ecommerce.ecomerce_backend.service.UsuarioService;
import jakarta.transaction.Transactional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest; // Importa PageRequest
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime; // Importa LocalDateTime
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Primary
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; 
    private final OrdenRepository ordenRepository; 

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, OrdenRepository ordenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.ordenRepository = ordenRepository;
    }

    private UsuarioResponse mapToDto(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .roles(usuario.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .build();
    }

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) { 
        if (usuarioRepository.existsByNombreUsuario(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario '" + request.getUsername() + "' ya está en uso.");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email '" + request.getEmail() + "' ya está en uso.");
        }

        Usuario nuevoUsuario = Usuario.builder()
                .nombreUsuario(request.getUsername())
                .contrasena(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(Collections.singleton(RolUsuario.USER)) 
                .build();
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);
        return mapToDto(nuevoUsuario);
    }

    @Override
    @Transactional
    public Optional<UsuarioResponse> actualizarUsuario(Long id, UsuarioRequest request) {
        return usuarioRepository.findById(id).map(usuario -> {
            if (!usuario.getNombreUsuario().equals(request.getUsername()) && usuarioRepository.existsByNombreUsuario(request.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario '" + request.getUsername() + "' ya está en uso.");
            }
            if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email '"+request.getEmail()+"' ya está en uso.");
                
            }
            usuario.setNombreUsuario(request.getUsername());
            usuario.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(request.getPassword()));
            }
            return mapToDto(usuarioRepository.save(usuario));
        });
    }

    @Override
    public Optional<UsuarioResponse> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(this::mapToDto);
    }

    @Override
    public List<UsuarioResponse> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean eliminarUsuario(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Override
    public List<TopFrequentCustomerResponse> obtenerTop5ClientesFrecuentes() {
        List<Object[]> results = ordenRepository.findTop5FrequentCustomers(PageRequest.of(0, 5));
        return results.stream()
                .map(result -> {
                    Long customerId = (Long) result[0];
                    String customerUsername = (String) result[1];
                    Long totalOrders = (Long) result[2];
                    return TopFrequentCustomerResponse.builder()
                            .customerId(customerId)
                            .customerUsername(customerUsername)
                            .totalOrders(totalOrders)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean esClienteFrecuente(Long usuarioId, int minOrders, int daysPeriod) {
        LocalDateTime fechaDesde = LocalDateTime.now().minusDays(daysPeriod);
        long ordersCount = ordenRepository.countByUsuarioIdAndFechaCreacionAfter(usuarioId, fechaDesde);
        return ordersCount >= minOrders;
    }
}