package com.anfealta.ecommerce.ecomerce_backend.config;

import com.anfealta.ecommerce.ecomerce_backend.security.JwtAuthenticationFilter; // Importa tu filtro JWT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // Tu UsuarioService implementa UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder; // El PasswordEncoder que ya configuraste
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint; // Para manejar 401 Unauthorized
import org.springframework.http.HttpStatus; // Para HttpStatus.UNAUTHORIZED

@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // Tu UsuarioService
    private final PasswordEncoder passwordEncoder; // Tu PasswordEncoder

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // Define la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: Permite acceso sin autenticación a la API de autenticación y a Swagger
                .requestMatchers(
                    "/api/auth/**", // Para registro y login de usuarios
                    "/v2/api-docs",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/configuration/ui",
                    "/configuration/security",
                    "/swagger-ui/**",
                    "/webjars/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Todas las demás solicitudes requieren autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Hace que las sesiones sean sin estado (para JWT)
            )
            .authenticationProvider(authenticationProvider()) // Configura tu proveedor de autenticación
            // Agrega tu filtro JWT antes del filtro de autenticación de nombre de usuario/contraseña de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                // Maneja solicitudes no autenticadas (ej. 401 Unauthorized)
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }

    // Configura el AuthenticationProvider que usa tu UserDetailsService y PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Usa tu UsuarioService
        authProvider.setPasswordEncoder(passwordEncoder); // Usa tu PasswordEncoder
        return authProvider;
    }

    // Expone el AuthenticationManager como un Bean para que pueda ser inyectado (ej. en el controlador de Auth)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}