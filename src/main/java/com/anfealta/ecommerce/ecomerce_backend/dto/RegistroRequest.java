package com.anfealta.ecommerce.ecomerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Importa las anotaciones de validación (javax.validation o jakarta.validation)
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data // Lombok: genera getters, setters, toString, equals, hashCode
@Builder // Lombok: permite construir objetos con el patrón Builder
@NoArgsConstructor // Lombok: constructor sin argumentos
@AllArgsConstructor // Lombok: constructor con todos los argumentos
public class RegistroRequest {

    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String contrasena;

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    // Puedes añadir otros campos si los necesitas en el registro, ej. nombre, apellido.
}