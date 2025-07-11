package com.anfealta.ecommerce.ecomerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    private String contrasena;
}