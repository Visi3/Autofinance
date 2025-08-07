package com.fag.Autofinance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {

        private final SecurityFilter securityFilter;

        public ConfigSecurity(SecurityFilter securityFilter) {
                this.securityFilter = securityFilter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf().disable()
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/login").permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return NoOpPasswordEncoder.getInstance();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

}
