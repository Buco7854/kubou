package com.kubou.infrastructure.config;

import com.kubou.infrastructure.security.CustomUserDetailsService;
import com.kubou.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/ws/**").permitAll()
                .requestMatchers("/api/v1/games/by-pin/**").permitAll()
                
                // Restrict creation endpoints to USER or ADMIN roles (exclude GUEST)
                .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/quizzes/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/quizzes/**").hasAnyRole("USER", "ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/v1/questions/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/questions/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/questions/**").hasAnyRole("USER", "ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/v1/games/**").hasAnyRole("USER", "ADMIN") // Only registered users can host games
                
                // Guests can only participate (GET/POST to game session endpoints handled by controller logic or specific permissions)
                // But generally, any authenticated user (including GUEST) can access other endpoints unless restricted above.
                .anyRequest().authenticated()
            );
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
