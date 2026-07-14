package com.example.discordclone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Для разработки часто отключают
        .authorizeHttpRequests((requests) -> requests
            .requestMatchers("/", "/home", "/register", "/login", "/css/**", "/js/**").permitAll() // РАЗРЕШАЕМ ДОСТУП
            .anyRequest().authenticated() // Всё остальное — только после логина
        )
        .formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        );
    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}