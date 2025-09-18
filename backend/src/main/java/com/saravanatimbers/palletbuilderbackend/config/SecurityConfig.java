package com.saravanatimbers.palletbuilderbackend.config;

import com.saravanatimbers.palletbuilderbackend.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            System.out.println("[SECURITY] Access denied for: " + request.getRequestURI());
            accessDeniedException.printStackTrace();
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admin/settings").permitAll()
                        .requestMatchers("/api/download/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/email/send").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/quotes/all").hasRole("ADMIN")
                        .requestMatchers("/api/quotes/*/status").authenticated()
                        .requestMatchers("/api/quotes/*/upload").authenticated()
                        .requestMatchers("/api/quotes/files/**").authenticated()
                        .requestMatchers("/api/quotes/user/**").authenticated()
                        .requestMatchers("/api/quotes/*").authenticated()
                        .requestMatchers("/api/quotes").authenticated()
                        .requestMatchers("/api/orders/all").hasRole("ADMIN")
                        .requestMatchers("/api/orders/user/**").authenticated()
                        .requestMatchers("/api/orders/*").authenticated()
                        .requestMatchers("/api/orders").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/user-info/{userId:.+}/discount-category").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh.accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 