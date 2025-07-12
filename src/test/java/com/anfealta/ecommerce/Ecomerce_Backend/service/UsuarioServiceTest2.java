package com.anfealta.ecommerce.ecomerce_backend.service;


import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.entity.RolUsuario;
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest2 {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioRequest usuarioRequest;
    private Usuario usuarioExistente;
    private Usuario usuarioNuevo;

    @BeforeEach
    void setUp() {
        usuarioRequest = UsuarioRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        usuarioExistente = Usuario.builder()
                .id(1L)
                .nombreUsuario("existinguser")
                .email("existing@example.com")
                .contrasena("encodedExistingPassword")
                .roles(Collections.singleton(RolUsuario.USER))
                .fechaCreacion(LocalDateTime.now().minusDays(5))
                .fechaActualizacion(LocalDateTime.now().minusDays(5))
                .build();

        usuarioNuevo = Usuario.builder()
                .id(2L)
                .nombreUsuario("anotheruser") // This is the conflicting username
                .email("another@example.com")
                .contrasena("encodedNewPassword")
                .roles(Collections.singleton(RolUsuario.USER))
                .fechaCreacion(LocalDateTime.now().minusDays(1))
                .fechaActualizacion(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("Debe crear un usuario exitosamente cuando los datos son válidos y no hay conflictos")
    void crearUsuario_Success() {
        // GIVEN
        when(usuarioRepository.existsByNombreUsuario(usuarioRequest.getUsername())).thenReturn(false);
        when(usuarioRepository.existsByEmail(usuarioRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(usuarioRequest.getPassword())).thenReturn("encodedPassword");

        // Cuando se llama a save, devuelve una nueva instancia de Usuario que simula haber sido guardada
        // y asegúrate de que tiene los roles correctamente inicializados.
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simula el ID que la base de datos asignaría
            savedUser.setContrasena("encodedPassword"); // Asegúrate que la contraseña está codificada

            // Asegurarse de que los roles se inicializan si no lo están,
            // o se establecen según la lógica de negocio (ej. rol por defecto USER)
            if (savedUser.getRoles() == null || savedUser.getRoles().isEmpty()) {
                savedUser.setRoles(new HashSet<>(Collections.singletonList(RolUsuario.USER))); // Inicializar con un rol por defecto
            }
            savedUser.setFechaCreacion(LocalDateTime.now());
            savedUser.setFechaActualizacion(LocalDateTime.now());
            return savedUser;
        });

        // WHEN
        UsuarioResponse response = usuarioService.crearUsuario(usuarioRequest);

        // THEN
        assertNotNull(response);
        assertEquals(usuarioRequest.getUsername(), response.getUsername());
        assertEquals(usuarioRequest.getEmail(), response.getEmail());
        assertTrue(response.getRoles().contains("USER"));
        verify(usuarioRepository, times(1)).existsByNombreUsuario(usuarioRequest.getUsername());
        verify(usuarioRepository, times(1)).existsByEmail(usuarioRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(usuarioRequest.getPassword());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (CONFLICT) cuando el nombre de usuario ya existe")
    void crearUsuario_UsernameConflict() {
        when(usuarioRepository.existsByNombreUsuario(usuarioRequest.getUsername())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.crearUsuario(usuarioRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El nombre de usuario 'testuser' ya está en uso."));

        verify(usuarioRepository, times(1)).existsByNombreUsuario(usuarioRequest.getUsername());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    
    @Test
    @DisplayName("Debe obtener un usuario por ID existente")
    void obtenerUsuarioPorId_Found() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        Optional<UsuarioResponse> response = usuarioService.obtenerUsuarioPorId(1L);

        assertTrue(response.isPresent());
        assertEquals(usuarioExistente.getId(), response.get().getId());
        assertEquals(usuarioExistente.getNombreUsuario(), response.get().getUsername());
        assertEquals(usuarioExistente.getEmail(), response.get().getEmail());
        Set<String> expectedRoles = usuarioExistente.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        assertEquals(expectedRoles, response.get().getRoles());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando el usuario no se encuentra por ID")
    void obtenerUsuarioPorId_NotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UsuarioResponse> response = usuarioService.obtenerUsuarioPorId(99L);

        assertFalse(response.isPresent());
        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe eliminar un usuario existente y retornar true")
    void eliminarUsuario_Success() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        boolean eliminado = usuarioService.eliminarUsuario(1L);

        assertTrue(eliminado);
        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe retornar false cuando se intenta eliminar un usuario no existente")
    void eliminarUsuario_NotFound() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        boolean eliminado = usuarioService.eliminarUsuario(99L);

        assertFalse(eliminado);
        verify(usuarioRepository, times(1)).existsById(99L);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe actualizar un usuario exitosamente")
    void actualizarUsuario_Success() {
        Long userId = 1L;
        UsuarioRequest updateRequest = UsuarioRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword123")
                .build();

        // Simular que el usuario existente es encontrado
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        // Simular que el nuevo username y email no existen para otros usuarios
        // Nota: Para este test, el username 'updateduser' no debe existir.
        when(usuarioRepository.existsByNombreUsuario(updateRequest.getUsername())).thenReturn(false);
        when(usuarioRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        // Simular la codificación de la nueva contraseña
        when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("encodedNewPassword");

        // Capturar el argumento pasado a save para verificar los detalles
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioActualizado = invocation.getArgument(0);
            assertEquals(userId, usuarioActualizado.getId());
            assertEquals(updateRequest.getUsername(), usuarioActualizado.getNombreUsuario());
            assertEquals(updateRequest.getEmail(), usuarioActualizado.getEmail());
            assertEquals("encodedNewPassword", usuarioActualizado.getContrasena());
            // Se puede verificar que la fecha de actualización cambió
            assertNotNull(usuarioActualizado.getFechaActualizacion());
            return usuarioActualizado;
        });

        Optional<UsuarioResponse> response = usuarioService.actualizarUsuario(userId, updateRequest);

        assertTrue(response.isPresent());
        assertEquals(userId, response.get().getId());
        assertEquals(updateRequest.getUsername(), response.get().getUsername());
        assertEquals(updateRequest.getEmail(), response.get().getEmail());
        verify(usuarioRepository, times(1)).findById(userId);
        verify(usuarioRepository, times(1)).existsByNombreUsuario(updateRequest.getUsername());
        verify(usuarioRepository, times(1)).existsByEmail(updateRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(updateRequest.getPassword());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si el usuario a actualizar no se encuentra")
    void actualizarUsuario_NotFound() {
        Long userId = 99L;
        UsuarioRequest updateRequest = UsuarioRequest.builder()
                .username("nonexistentuser")
                .email("nonexistent@example.com")
                .password("password")
                .build();

        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UsuarioResponse> response = usuarioService.actualizarUsuario(userId, updateRequest);

        assertFalse(response.isPresent());
        verify(usuarioRepository, times(1)).findById(userId);
        verify(usuarioRepository, never()).existsByNombreUsuario(anyString());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (CONFLICT) si el nuevo nombre de usuario ya está en uso por otro usuario")
    void actualizarUsuario_UsernameConflict() {
        Long userId = 1L;
        UsuarioRequest updateRequest = UsuarioRequest.builder()
                .username("anotheruser") // username que ya existe para usuarioNuevo (ID 2L)
                .email("updated@example.com")
                .password("newpassword")
                .build();

        // Simular que el usuario existente es encontrado
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        // Simular que el nuevo username ya existe Y pertenece a un ID diferente
        when(usuarioRepository.existsByNombreUsuario(updateRequest.getUsername())).thenReturn(true);
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.actualizarUsuario(userId, updateRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        // MODIFICACIÓN: Usar assertEquals para una verificación más estricta del mensaje.
        assertEquals("El nombre de usuario 'anotheruser' ya está en uso.", exception.getReason());

        verify(usuarioRepository, times(1)).findById(userId);
        verify(usuarioRepository, times(1)).existsByNombreUsuario(updateRequest.getUsername());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    
    @Test
    @DisplayName("Debe obtener una lista de todos los usuarios")
    void obtenerTodosLosUsuarios_Success() {
        List<Usuario> usuarios = Arrays.asList(usuarioExistente, usuarioNuevo);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioResponse> responses = usuarioService.obtenerTodosLosUsuarios();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        // Verificar que el mapeo a DTO es correcto
        assertEquals(usuarioExistente.getNombreUsuario(), responses.get(0).getUsername());
        assertEquals(usuarioNuevo.getNombreUsuario(), responses.get(1).getUsername());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si no hay usuarios")
    void obtenerTodosLosUsuarios_NoUsers() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<UsuarioResponse> responses = usuarioService.obtenerTodosLosUsuarios();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }
}