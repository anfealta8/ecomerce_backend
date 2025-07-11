package com.anfealta.ecommerce.ecomerce_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Marca esta clase como un componente de Spring
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // Tu UsuarioService implementa UserDetailsService

    @Autowired // Inyección de dependencias a través del constructor
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Verificar si el encabezado de autorización existe y tiene el formato "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Si no hay token o no es Bearer, sigue la cadena de filtros
            return;
        }

        // 2. Extraer el token JWT (después de "Bearer ")
        jwt = authHeader.substring(7); // "Bearer ".length() es 7

        // 3. Extraer el nombre de usuario del token
        username = jwtUtil.extractUsername(jwt);

        // 4. Si se extrajo un nombre de usuario y no hay una autenticación ya en el contexto de seguridad
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 5. Cargar los detalles del usuario usando tu UserDetailsService (UsuarioService)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Validar el token y los detalles del usuario
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 7. Si el token es válido, crear un objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credenciales (contraseña) no necesarias una vez el token es validado
                        userDetails.getAuthorities() // Roles/autoridades del usuario
                );
                // 8. Establecer detalles de la solicitud (IP, sesión, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. Establecer el objeto de autenticación en el contexto de seguridad de Spring
                // Esto indica que el usuario está autenticado para esta solicitud.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Continuar con la cadena de filtros de Spring Security
        filterChain.doFilter(request, response);
    }
}
