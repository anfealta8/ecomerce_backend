package com.anfealta.ecommerce.ecomerce_backend.service;

import com.anfealta.ecommerce.ecomerce_backend.dto.TopFrequentCustomerResponse;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioUpdateRequest;

import org.springframework.security.core.userdetails.UserDetailsService; 

import java.util.List;
import java.util.Optional;

public interface UsuarioService extends UserDetailsService { 
    UsuarioResponse crearUsuario(UsuarioRequest request);
    Optional<UsuarioResponse> obtenerUsuarioPorId(Long id);
    List<UsuarioResponse> obtenerTodosLosUsuarios();
    Optional<UsuarioResponse> actualizarUsuario(Long id, UsuarioUpdateRequest request);
    boolean eliminarUsuario(Long id);

    List<TopFrequentCustomerResponse> obtenerTop5ClientesFrecuentes();
    boolean esClienteFrecuente(Long usuarioId, int minOrders, int daysPeriod);
}