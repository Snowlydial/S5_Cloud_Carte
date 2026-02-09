package com.example.carte.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // simplification pour dev
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // autoriser preflight
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // allow all localhost origins and react-web-app
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
    // Exception {
    // http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
    // .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/**",
    // "/cloud/api/auth/**"))
    // .authorizeHttpRequests(auth -> auth
    // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    // .requestMatchers("/api/auth/**").permitAll() // accès libre pour login
    // .requestMatchers("/login.html", "/login-form").permitAll()
    // .requestMatchers("/login").permitAll()
    // // .requestMatchers("/etudiant/**").permitAll()
    // // .requestMatchers("/etudiants/**").permitAll()
    // .anyRequest().authenticated())
    // .sessionManagement(session ->
    // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    // return http.build();
    // }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    // CorsConfiguration config = new CorsConfiguration();
    // config.setAllowedOriginPatterns(List.of("http://localhost:*")); // allow all
    // localhost origins
    // config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS",
    // "PATCH"));
    // config.setAllowedHeaders(List.of("Content-Type", "Authorization",
    // "X-Requested-With", "Accept"));
    // config.setAllowCredentials(false); // false when allowing wildcard origins
    // config.setMaxAge(3600L); // cache preflight requests for 1 hour

    // UrlBasedCorsConfigurationSource source = new
    // UrlBasedCorsConfigurationSource();
    // source.registerCorsConfiguration("/**", config);
    // return source;
    // }
    // Auth manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Mot de passe en clair
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

}
