package com.anfealta.ecommerce.ecomerce_backend.service;

import com.anfealta.ecommerce.ecomerce_backend.dto.LoginRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.LoginResponse; // Usaremos este como respuesta de Auth
import com.anfealta.ecommerce.ecomerce_backend.dto.RegistroRequest; // Usaremos este para el registro
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;
import com.anfealta.ecommerce.ecomerce_backend.repository.UsuarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.security.JwtUtil; // Usamos tu JwtUtil
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; 
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registra un nuevo usuario a partir de un RegistroRequest y genera un token de login.
     * @param request DTO de RegistroRequest.
     * @return LoginResponse con el token JWT y los datos del usuario.
     */
    public LoginResponse registerUser(RegistroRequest request) {
        if (usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario()) 
                .contrasena(passwordEncoder.encode(request.getContrasena())) 
                .email(request.getEmail())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        String jwtToken = jwtUtil.generateToken(nuevoUsuario); 

        return LoginResponse.builder()
                .token(jwtToken)
                .idUsuario(nuevoUsuario.getId())
                .nombreUsuario(nuevoUsuario.getNombreUsuario())
                .email(nuevoUsuario.getEmail())
                .build();
    }

    /**
     * Autentica un usuario y genera un token de login.
     * @param request DTO de LoginRequest.
     * @return LoginResponse con el token JWT y los datos del usuario.
     */
    public LoginResponse loginUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), 
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado después de autenticación."));

        String jwtToken = jwtUtil.generateToken(usuario); 

        return LoginResponse.builder()
                .token(jwtToken)
                .idUsuario(usuario.getId())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .build();
    }
}