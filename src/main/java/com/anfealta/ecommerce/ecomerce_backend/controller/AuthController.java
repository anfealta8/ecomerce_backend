package com.anfealta.ecommerce.ecomerce_backend.controller;

import com.anfealta.ecommerce.ecomerce_backend.dto.LoginRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.LoginResponse;
import com.anfealta.ecommerce.ecomerce_backend.dto.RegistroRequest;
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;
import com.anfealta.ecommerce.ecomerce_backend.security.JwtUtil;
import com.anfealta.ecommerce.ecomerce_backend.service.UsuarioService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; 

import jakarta.validation.Valid; // Para validar los DTOs de entrada

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Para autenticar al usuario
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // Objeto de autenticación de Spring Security
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Para manejar excepciones

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/auth") // Ruta base para los endpoints de autenticación
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager; // Lo inyectamos desde SecurityConfig
    private final JwtUtil jwtUtil;

    // Inyección de dependencias a través del constructor
    public AuthController(UsuarioService usuarioService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint para registrar un nuevo usuario.
     * @param request El DTO con los datos del usuario a registrar.
     * @return ResponseEntity con el mensaje de éxito o error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody RegistroRequest request) {
        try {
            Usuario nuevoUsuario = Usuario.builder()
                    .nombreUsuario(request.getNombreUsuario())
                    .contrasena(request.getContrasena()) // La contraseña se encripta en el servicio
                    .email(request.getEmail())
                    .build();

            usuarioService.registrarNuevoUsuario(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente!");
        } catch (RuntimeException e) {
            // Captura excepciones específicas del servicio (ej. usuario/email ya existe)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para el login de usuario.
     * @param request El DTO con las credenciales de login (nombre de usuario y contraseña).
     * @return ResponseEntity con el token JWT y datos del usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUsuario(@Valid @RequestBody LoginRequest request) {
        try {
            // Autentica al usuario usando AuthenticationManager
            // Esto dispara el UsuarioService.loadUserByUsername y la comparación de contraseñas
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getNombreUsuario(), request.getContrasena())
            );

            // Si la autenticación es exitosa, establece el contexto de seguridad (opcional para stateless)
            // SecurityContextHolder.getContext().setAuthentication(authentication);

            // Genera el token JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            // Cargar la entidad Usuario completa para obtener el ID y el email
            Usuario usuarioAutenticado = usuarioService.buscarUsuarioPorNombreUsuario(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado después de autenticación exitosa."));


            // Construye la respuesta del login
            LoginResponse response = LoginResponse.builder()
                    .token(jwt)
                    .idUsuario(usuarioAutenticado.getId())
                    .nombreUsuario(usuarioAutenticado.getNombreUsuario())
                    .email(usuarioAutenticado.getEmail())
                    // .roles(usuarioAutenticado.getRoles().stream().map(Enum::name).collect(Collectors.toSet())) // Si quieres los roles
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Captura cualquier excepción de autenticación (ej. credenciales inválidas)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    // --- Endpoint de prueba para usuarios autenticados ---
    // Puedes acceder a este endpoint solo si tienes un token JWT válido
    @GetMapping("/perfil")
    public ResponseEntity<String> obtenerPerfilUsuario(Authentication authentication) {
        // 'authentication.getName()' te da el nombre de usuario autenticado
        return ResponseEntity.ok("Bienvenido a tu perfil, " + authentication.getName() + "!");
    }
}
