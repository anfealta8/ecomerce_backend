package com.anfealta.ecommerce.ecomerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipoToken = "Bearer"; // Tipo de token, por convención "Bearer"
    private Long idUsuario;
    private String nombreUsuario;
    private String email;
    // Puedes añadir los roles del usuario si quieres que el frontend los conozca
    // private Set<String> roles;
}