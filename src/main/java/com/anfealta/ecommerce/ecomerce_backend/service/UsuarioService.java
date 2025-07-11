package com.anfealta.ecommerce.ecomerce_backend.service; // Crea este paquete si no existe

import com.anfealta.ecommerce.ecomerce_backend.entity.RolUsuario; // Importa tu enum de roles
import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario;    // Importa tu entidad Usuario
import com.anfealta.ecommerce.ecomerce_backend.repository.UsuarioRepository; // Importa tu repositorio de Usuario
import org.springframework.beans.factory.annotation.Autowired; // Para inyección de dependencias
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Para encriptar contraseñas
import org.springframework.stereotype.Service; // Marca esta clase como un servicio de Spring

import java.time.LocalDateTime;
import java.util.Collections; // Para Collections.singleton()
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service // Indica a Spring que esta clase es un servicio y debe ser manejada por el contenedor de Spring
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectamos el encriptador de contraseñas

    // Constructor para inyectar dependencias (Spring lo maneja automáticamente con @Autowired)
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * La contraseña se encripta antes de ser guardada.
     * Asigna el rol 'USER' por defecto.
     * @param usuario El objeto Usuario a registrar (con contraseña sin encriptar).
     * @return El Usuario guardado.
     * @throws RuntimeException Si el nombre de usuario o el email ya existen.
     */
    public Usuario registrarNuevoUsuario(Usuario usuario) {
        // Validar si el nombre de usuario ya existe
        if (usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario()).isPresent()) {
            throw new RuntimeException("El nombre de usuario '" + usuario.getNombreUsuario() + "' ya está en uso.");
        }
        // Validar si el email ya existe
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email '" + usuario.getEmail() + "' ya está registrado.");
        }

        // Encriptar la contraseña antes de guardarla
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        // Asignar rol por defecto (ej. USER)
        usuario.setRoles(Collections.singleton(RolUsuario.USER)); // Asigna el rol USER por defecto
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now()); // Inicializa fecha de actualización

        return usuarioRepository.save(usuario);
    }

    /**
     * Carga los detalles del usuario por su nombre de usuario para Spring Security.
     * @param nombreUsuario El nombre de usuario a buscar.
     * @return UserDetails con los detalles del usuario.
     * @throws UsernameNotFoundException Si el usuario no es encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre de usuario: " + nombreUsuario));
    }

    /**
     * Busca un usuario por su ID.
     * @param id El ID del usuario.
     * @return Un Optional que contiene el Usuario si es encontrado.
     */
    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * @param nombreUsuario El nombre de usuario.
     * @return Un Optional que contiene el Usuario si es encontrado.
     */
    public Optional<Usuario> buscarUsuarioPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }

    /**
     * Obtiene todos los usuarios.
     * @return Una lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Aquí podrías añadir métodos para actualizar usuarios, cambiar contraseñas, gestionar roles, etc.
    // public Usuario actualizarUsuario(Usuario usuario) { ... }
    // public void eliminarUsuario(Long id) { ... }
}
