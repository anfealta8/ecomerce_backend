package com.anfealta.ecommerce.ecomerce_backend.service; 

import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;
import com.anfealta.ecommerce.ecomerce_backend.entity.RolUsuario;
import com.anfealta.ecommerce.ecomerce_backend.repository.UsuarioRepository;
import com.anfealta.ecommerce.ecomerce_backend.service.impl.UsuarioServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.HashSet;
import java.util.Arrays; 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*; 

@ExtendWith(MockitoExtension.class) 
public class UsuarioServiceTest {

    @Mock 
    private UsuarioRepository usuarioRepository;

    @Mock 
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Debe crear un nuevo usuario exitosamente con rol USER por defecto")
    void shouldCreateNewUserSuccessfully() {
        UsuarioRequest request = UsuarioRequest.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .build();

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
        
        when(usuarioRepository.existsByNombreUsuario(request.getUsername())).thenReturn(false);
        
        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);
        
        Usuario nuevoUsuario = Usuario.builder()
                .id(1L)
                .nombreUsuario("testuser")
                .contrasena("encodedPassword123")
                .email("test@example.com")
                .roles(new HashSet<>(Collections.singletonList(RolUsuario.ADMIN))) 
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevoUsuario);

        UsuarioResponse response = usuarioService.crearUsuario(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("ADMIN")); 
        
        verify(usuarioRepository, times(1)).existsByNombreUsuario("testuser");
        verify(usuarioRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
@DisplayName("Debe lanzar ResponseStatusException (CONFLICT) cuando el nombre de usuario ya existe")
void shouldThrowConflictWhenUsernameAlreadyExists() {
    // GIVEN
    UsuarioRequest request = UsuarioRequest.builder()
            .username("existinguser")
            .password("password123")
            .email("new@example.com")
            .build();

    when(usuarioRepository.existsByNombreUsuario(request.getUsername())).thenReturn(true);
    

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        usuarioService.crearUsuario(request);
    });

    System.out.println("Razón de la excepción: " + exception.getReason());
    System.out.println("Status de la excepción: " + exception.getStatusCode());

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

    assertEquals("El nombre de usuario 'existinguser' ya está en uso.", exception.getReason());
    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Debe retornar un UsuarioResponse cuando se encuentra un usuario por ID")
    void shouldReturnUserResponseWhenUserExistsById() {
        Long userId = 1L;
        Usuario usuario = Usuario.builder()
                .id(userId)
                .nombreUsuario("testuser")
                .contrasena("encodedPassword")
                .email("test@example.com")
                .roles(new HashSet<>(Arrays.asList(RolUsuario.ADMIN)))
                .build();

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        Optional<UsuarioResponse> response = usuarioService.obtenerUsuarioPorId(userId);

        assertTrue(response.isPresent());
        assertEquals(userId, response.get().getId());
        assertEquals("testuser", response.get().getUsername());
        assertTrue(response.get().getRoles().contains("ADMIN"));

        verify(usuarioRepository, times(1)).findById(userId);
    }
}