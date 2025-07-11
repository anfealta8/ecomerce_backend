package com.anfealta.ecommerce.ecomerce_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // Indica que esta clase provee beans de configuración
public class AppConfig {

    @Bean // Crea una instancia de BCryptPasswordEncoder y la pone en el contexto de Spring
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
