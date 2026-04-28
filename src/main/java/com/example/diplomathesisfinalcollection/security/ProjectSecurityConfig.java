package com.example.diplomathesisfinalcollection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class ProjectSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Շատ կարևոր է POST-ի համար
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ԲԱՑՈՒՄ ԵՆՔ ԱՄԵՆ ԻՆՉ (միայն այս 2 օրվա համար)
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Եթե H2 բազա ես օգտագործում

        return http.build();
    }
}