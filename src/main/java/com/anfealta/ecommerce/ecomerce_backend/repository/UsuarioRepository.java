package com.anfealta.ecommerce.ecomerce_backend.repository;

import com.anfealta.ecommerce.ecomerce_backend.entity.Usuario; // Importa tu entidad Usuario
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Importa Optional para métodos que pueden no encontrar un resultado

@Repository // Marca esta interfaz como un componente de repositorio de Spring
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository ya te da métodos básicos como save(), findById(), findAll(), delete()

    // Método personalizado para encontrar un Usuario por su nombre de usuario.
    // Es crucial para el proceso de login. Retorna Optional para manejar el caso de no encontrarlo.
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Método personalizado para encontrar un Usuario por su email (si lo necesitas para recuperación, etc.)
    Optional<Usuario> findByEmail(String email);

    // Puedes añadir otros métodos personalizados si prevés búsquedas específicas,
    // por ejemplo: List<Usuario> findByRolesContaining(RolUsuario rol);
}