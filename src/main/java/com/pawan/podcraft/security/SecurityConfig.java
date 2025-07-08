package com.pawan.podcraft.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/chat-lobby/**").authenticated()
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                )
                .logout(config -> config
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}
