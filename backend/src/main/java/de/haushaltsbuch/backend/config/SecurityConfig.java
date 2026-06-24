package de.haushaltsbuch.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;

/**
 * Configures HTTP security for the application.
 *
 * <p>The frontend is a statically built SvelteKit SPA served from the same origin/context
 * path as the API, so a classic session cookie (instead of stateless JWTs) is sufficient
 * here and avoids any token-handling logic on the client.
 *
 * <p>There is exactly one user, configured via {@code app.auth.username}/{@code app.auth.password-hash}
 * (see {@link #userDetailsService}) instead of a database table, since this application
 * is designed for single-user use.
 */
@Configuration
public class SecurityConfig {

    @Value("${app.auth.username}")
    private String authUsername;

    @Value("${app.auth.password-hash}")
    private String authPasswordHash;

    /**
     * Builds the single configured user from {@code app.auth.*} properties.
     *
     * @return an in-memory user store containing exactly one user
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(authUsername)
                        .password(authPasswordHash)
                        .roles("USER")
                        .build()
        );
    }

    /**
     * @return the encoder used to hash and verify the configured user's password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} so {@code AuthController} can authenticate
     * login requests manually instead of relying on Spring Security's form-login flow.
     *
     * @param config Spring Boot's authentication configuration
     * @return the authentication manager backed by {@link #userDetailsService} and {@link #passwordEncoder}
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Stores the {@link org.springframework.security.core.context.SecurityContext} in the
     * {@code HttpSession} after a successful login, backing it with the {@code JSESSIONID} cookie.
     *
     * @return the session-based security context repository
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Defines which endpoints require an authenticated session and how unauthenticated
     * API requests are answered (JSON 401 instead of a redirect, since this is an SPA).
     *
     * @param http the security builder
     * @return the configured filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // No session exists yet before login, so there is nothing for a CSRF
                        // attack to ride on; logout only ends the session, which is harmless.
                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/logout"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(SecurityConfig::sendUnauthorized));

        return http.build();
    }

    /**
     * Writes a {@code {"error": "Not authenticated"}} JSON body with status {@code 401}.
     */
    private static void sendUnauthorized(HttpServletRequest request,
                                          HttpServletResponse response,
                                          AuthenticationException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Not authenticated\"}");
    }
}
