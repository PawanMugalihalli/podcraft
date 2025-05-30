package com.pawan.riverside.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.GET, "/", "/login", "/logout", "/register").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
//                        .anyRequest().authenticated()
//                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/chat-lobby/**").authenticated()
                        .anyRequest().permitAll()
                )
//                .csrf(csrf->csrf.ignoringRequestMatchers("/chat-lobby/**","/save"))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                )
                .logout(config -> config
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}
