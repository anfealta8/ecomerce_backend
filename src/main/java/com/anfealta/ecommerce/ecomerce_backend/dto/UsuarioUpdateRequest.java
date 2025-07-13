package com.anfealta.ecommerce.ecomerce_backend.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioUpdateRequest {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;

    // La contraseña es OPCIONAL para la actualización
    // No usamos @NotBlank aquí
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres si se proporciona")
    private String password; // Puede ser null o vacío si no se cambia

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser una dirección de correo válida")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;

    // Puedes añadir roles aquí si tu endpoint de actualización los maneja
    private Set<String> roles; // Importa java.util.Set
}