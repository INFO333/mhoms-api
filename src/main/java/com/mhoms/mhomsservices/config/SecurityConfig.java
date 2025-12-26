package com.mhoms.mhomsservices.config;

import com.mhoms.mhomsservices.security.CustomAccessDeniedHandler;
import com.mhoms.mhomsservices.security.CustomAuthenticationEntryPoint;
import com.mhoms.mhomsservices.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Security configuration with JWT authentication
 * Updated with pagination and advanced feature endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ═══════════════════════════════════════════════════════
                        // PUBLIC ENDPOINTS - No authentication required
                        // ═══════════════════════════════════════════════════════
                        .requestMatchers(
                                "/health",
                                "/auth/**",           // Authentication endpoints
                                "/v3/api-docs/**",    // Swagger API docs
                                "/swagger-ui/**",     // Swagger UI
                                "/swagger-ui.html",   // Swagger UI HTML
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ═══════════════════════════════════════════════════════
                        // PATIENT ENDPOINTS
                        // ═══════════════════════════════════════════════════════
                        // Create patient - ADMIN only
                        .requestMatchers(HttpMethod.POST, "/patients/**").hasRole("ADMIN")
                        // Update patient - ADMIN only
                        .requestMatchers(HttpMethod.PUT, "/patients/**").hasRole("ADMIN")
                        // Delete patient - ADMIN only
                        .requestMatchers(HttpMethod.DELETE, "/patients/**").hasRole("ADMIN")
                        // View patients - ADMIN and DOCTOR
                        .requestMatchers(HttpMethod.GET, "/patients/**").hasAnyRole("ADMIN", "DOCTOR")

                        // ═══════════════════════════════════════════════════════
                        // DOCTOR ENDPOINTS
                        // ═══════════════════════════════════════════════════════
                        // Create doctor - ADMIN only
                        .requestMatchers(HttpMethod.POST, "/doctors/**").hasRole("ADMIN")
                        // Update doctor - ADMIN only
                        .requestMatchers(HttpMethod.PUT, "/doctors/**").hasRole("ADMIN")
                        // Toggle status - ADMIN only
                        .requestMatchers(HttpMethod.PATCH, "/doctors/**").hasRole("ADMIN")
                        // Delete doctor - ADMIN only
                        .requestMatchers(HttpMethod.DELETE, "/doctors/**").hasRole("ADMIN")
                        // View doctors - ALL authenticated users
                        .requestMatchers(HttpMethod.GET, "/doctors/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        // ═══════════════════════════════════════════════════════
                        // APPOINTMENT ENDPOINTS
                        // ═══════════════════════════════════════════════════════
                        // Book appointment - ADMIN and PATIENT
                        .requestMatchers(HttpMethod.POST, "/appointments/**").hasAnyRole("ADMIN", "PATIENT")
                        // Update status - ADMIN and DOCTOR
                        .requestMatchers(HttpMethod.PUT, "/appointments/*/status").hasAnyRole("ADMIN", "DOCTOR")
                        // Reschedule - ADMIN and DOCTOR
                        .requestMatchers(HttpMethod.PUT, "/appointments/*/reschedule").hasAnyRole("ADMIN", "DOCTOR")
                        // Cancel - ADMIN, DOCTOR, and PATIENT
                        .requestMatchers(HttpMethod.PUT, "/appointments/*/cancel").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                        // Delete - ADMIN only
                        .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasRole("ADMIN")
                        // View appointments - ALL authenticated users
                        .requestMatchers(HttpMethod.GET, "/appointments/**").authenticated()

                        // ═══════════════════════════════════════════════════════
                        // DASHBOARD ENDPOINTS
                        // ═══════════════════════════════════════════════════════
                        // Dashboard - ADMIN only
                        .requestMatchers("/dashboard/**").hasRole("ADMIN")

                        // ═══════════════════════════════════════════════════════
                        // ALL OTHER ENDPOINTS
                        // ═══════════════════════════════════════════════════════
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}