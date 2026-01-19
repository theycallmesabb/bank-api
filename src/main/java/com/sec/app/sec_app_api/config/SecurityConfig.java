package com.sec.app.sec_app_api.config;

import com.sec.app.sec_app_api.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/register").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<com.sec.app.sec_app_api.entity.User> userOpt = 
                userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            
            com.sec.app.sec_app_api.entity.User user = userOpt.get();
            
            return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("USER")
                .build();
        };
    }
}
