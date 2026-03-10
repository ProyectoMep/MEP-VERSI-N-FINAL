package com.example.colegiosapp.config;

import java.io.IOException;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.colegiosapp.service.CustomUserDetailsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Usamos el CustomUserDetailsService basado en tu entidad Usuario
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.userDetailsService(customUserDetailsService)
               .passwordEncoder(passwordEncoder);

        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos comunes (css, js, img, etc.)
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/img/**", "/assets/**", "/fonts/**", "/favicon.ico").permitAll()

                // Rutas públicas
                .requestMatchers("/", "/login", "/register", "/error", "/public/**").permitAll()

                // Rutas protegidas por rol
                .requestMatchers("/tutor/**").hasAuthority("Tutor")
                .requestMatchers("/gestor/**").hasAuthority("Gestor")
                .requestMatchers("/admin/**").hasAuthority("Administrador")

                // Cualquier otra ruta requiere estar autenticado
                .anyRequest().authenticated()
            )

            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")       // name del input en tu formulario
                .passwordParameter("contrasena")   // name del input de contraseña
                .successHandler(authenticationSuccessHandler())   // redirección por rol
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Manejo de errores 401 / 403
            .exceptionHandling(ex -> ex
                // 403: autenticado pero sin permisos
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/login?denied");
                })
                // 401: no autenticado intentando acceder a algo protegido
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login?denied");
                })
            );

        // Registramos explícitamente el UserDetailsService
        http.userDetailsService(customUserDetailsService);

        return http.build();
    }

    /**
     * Redirección por rol después del login.
     * Tutor, Gestor y Administrador, cada uno con su dashboard.
     */
    @Bean
    @SuppressWarnings("Convert2Lambda")
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException, ServletException {

                String authority = authentication
                        .getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority();

                switch (authority) {
                    case "Tutor" -> response.sendRedirect("/tutor/dashboard");
                    case "Gestor" -> response.sendRedirect("/gestor/dashboard");
                    case "Administrador" -> response.sendRedirect("/admin/dashboard");
                    default -> // Por si llega algún rol no contemplado
                    response.sendRedirect("/");
                }
            }
        };
    }
}
