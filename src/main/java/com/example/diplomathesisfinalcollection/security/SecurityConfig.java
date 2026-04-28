package com.example.medicalweb.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Admin page protect
                        .requestMatchers("/admin", "/admin/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/doctor/login", "doctor/profile").permitAll()
                        // User page protect
                        .requestMatchers("/user/**").authenticated()
                        // All other pages public
                        .anyRequest().permitAll()

                )
                .formLogin(form -> form
                        .loginPage("/doctor/login")
                        .loginProcessingUrl("/doctor/login")
                        .defaultSuccessUrl("/doctor/profile", true)
                        .permitAll()
                )

                // Admin login
                .formLogin(form -> form
                        .loginPage("/admin")
                        .loginProcessingUrl("/admin")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // User login
                .formLogin(form -> form
                        .loginPage("/user/signin")
                        .loginProcessingUrl("/user/signin")
                        .defaultSuccessUrl("/user/profile")
                        .permitAll()
                )

                // Admin logout
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                // User logout
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );


        return http.build();
    }


}
