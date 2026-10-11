package com.helpdesk.helpdesk_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.helpdesk.helpdesk_api.repository.UserRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepository userRepository) {

        return username -> {
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPasswordHash())
                    .authorities("ROLE_" + user.getRole())
                    .build();
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository contextRepository,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http
                .authenticationProvider(authenticationProvider)
                .securityContext(context ->
                        context.securityContextRepository(contextRepository))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED))
                .csrf(csrf -> csrf      // This stores the CSRF token in a cookie that the frontend can read. Keep the session cookie itself HTTP-only; the CSRF cookie has a different purpose.
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login","/api/auth/csrf", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/**")
                        .hasAnyRole("EMPLOYEE", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tickets")
                        .hasAnyRole("EMPLOYEE", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status")
                        .hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/assign")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/tickets/*/comments","/api/tickets/*/comments/**")
                        .hasAnyRole("EMPLOYEE", "AGENT", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .cors(Customizer.withDefaults());

        return http.build();
    }
}

//BCrypt hashes passwords with a salt. You store the resulting hash in users.password_hash, never the original password.
//The response intentionally excludes passwordHash

//CSRF protection helps prevent a malicious website from submitting authenticated state-changing requests using a user's browser session.

//This configuration uses role names with the ROLE_ prefix internally. For example, a database role of ADMIN becomes ROLE_ADMIN.
//Notice that CSRF protection remains enabled by default. We'll need to send a CSRF token for state-changing requests such as POST, PATCH and logout.