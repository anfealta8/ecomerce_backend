package com.anfealta.ecommerce.ecomerce_backend.config;

import com.anfealta.ecommerce.ecomerce_backend.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder; // Keep this import
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder; 

    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder; 
    }

    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Use the injected field
        authProvider.setPasswordEncoder(passwordEncoder); // Use the injected field
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Configuración CORS directamente en Spring Security
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <-- AÑADE ESTA LÍNEA
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Sigue siendo importante para el pre-vuelo
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/productos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/buscar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/activos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/ordenes").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/ordenes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/ordenes/{id}").hasAnyRole("USER", "ADMIN") 
                .requestMatchers(HttpMethod.GET, "/api/ordenes/usuario/{usuarioId}").hasAnyRole("USER", "ADMIN") 
                .requestMatchers(HttpMethod.PUT, "/api/ordenes/{id}/estado").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/ordenes/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inventarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventarios/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventarios/producto/{productoId}").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventarios/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventarios/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/productos/reportes/top5-vendidos").hasAnyRole("USER", "ADMIN")
                
                

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // Orígenes permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("*")); // Cabeceras permitidas
        configuration.setAllowCredentials(true); // Permitir credenciales (JWT)
        configuration.setMaxAge(3600L); // Max age para cache de pre-vuelo

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Aplica a todas las rutas bajo /api
        return source;
    }
}