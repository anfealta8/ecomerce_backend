package com.anfealta.ecommerce.ecomerce_backend.entity; // Ajusta el paquete si es necesario

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority; // Para roles de Spring Security
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Para roles
import org.springframework.security.core.userdetails.UserDetails; // Interfaz clave para Spring Security

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set; // Para roles

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios") // Nombre de la tabla en la base de datos
@EntityListeners(AuditingEntityListener.class) // Habilita auditoría automática (si configuras @EnableJpaAuditing)
public class Usuario implements UserDetails { // Implementa UserDetails para integrar con Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombreUsuario; // Campo para el username

    @Column(nullable = false)
    private String contrasena; // Contraseña encriptada

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(nullable = false) // Fecha de la última modificación
    private LocalDateTime fechaActualizacion;

    // Puedes manejar roles como una lista de strings o una entidad separada si son complejos
    @ElementCollection(targetClass = RolUsuario.class, fetch = FetchType.EAGER) // Roles cargados de inmediato
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING) // Almacena los roles como String en la DB
    private Set<RolUsuario> roles; // Usamos Set para evitar roles duplicados

    // --- Implementación de UserDetails para Spring Security ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapea tus roles a GrantedAuthority
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Podrías implementar lógica de expiración de cuenta
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Podrías implementar lógica de bloqueo de cuenta
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Podrías implementar lógica de expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return true; // Podrías implementar lógica para habilitar/deshabilitar usuarios
    }
}