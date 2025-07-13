package com.anfealta.ecommerce.ecomerce_backend.controller;

import com.anfealta.ecommerce.ecomerce_backend.dto.TopFrequentCustomerResponse;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioRequest;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioResponse;
import com.anfealta.ecommerce.ecomerce_backend.dto.UsuarioUpdateRequest;
import com.anfealta.ecommerce.ecomerce_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController 
@RequestMapping("/api/usuarios") 
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint para crear un nuevo usuario. (CRUD: CREATE)
     * Requiere autenticación.
     * @param request El DTO con los datos del nuevo usuario.
     * @return ResponseEntity con el UsuarioResponse del usuario creado y status 201.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        try {
            UsuarioResponse nuevoUsuario = usuarioService.crearUsuario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear el usuario: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener un usuario por su ID. (CRUD: READ)
     * Requiere autenticación.
     * @param id El ID del usuario.
     * @return ResponseEntity con el UsuarioResponse si se encuentra, y status 200, o 404 si no.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
    }

    /**
     * Endpoint para obtener todos los usuarios. (CRUD: READ ALL)
     * Requiere autenticación.
     * @return ResponseEntity con una lista de UsuarioResponse y status 200.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> obtenerTodosLosUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Endpoint para actualizar un usuario existente. (CRUD: UPDATE)
     * Requiere autenticación.
     * @param id El ID del usuario a actualizar.
     * @param request El DTO con los datos actualizados del usuario.
     * @return ResponseEntity con el UsuarioResponse del usuario actualizado y status 200, o 404/409 si no.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) { 
        try {
            Optional<UsuarioResponse> usuarioActualizado = usuarioService.actualizarUsuario(id, request);
            
            return usuarioActualizado.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint para eliminar un usuario por su ID. (CRUD: DELETE)
     * Requiere autenticación.
     * @param id El ID del usuario a eliminar.
     * @return ResponseEntity con status 204 No Content si se elimina, o 404 si no se encuentra.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        boolean eliminado = usuarioService.eliminarUsuario(id);
        if (eliminado) {
            return ResponseEntity.noContent().build(); 
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id); // 404 Not Found
        }
    }

    /**
     * Endpoint para obtener el Top 5 de clientes frecuentes.
     * Requiere rol ADMIN.
     * @return Lista de TopFrequentCustomerResponse.
     */
    @GetMapping("/reportes/top5-frecuentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopFrequentCustomerResponse>> obtenerTop5ClientesFrecuentes() {
        List<TopFrequentCustomerResponse> clientes = usuarioService.obtenerTop5ClientesFrecuentes();
        return ResponseEntity.ok(clientes);
    }
}
