package com.example.diplomathesisfinalcollection.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomLoginSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encrypts passwords automatically
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 🛑 ՄԻՋՈՒԿԱՅԻՆ ԼՈՒԾՈՒՄ. Այս հասցեի վրա Spring Security ԸՆԴՀԱՆՐԱՊԵՍ չի աշխատի
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/pharmacy-admin/medicines/upload-excel");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. ԱՆՋԱՏՈՒՄ ԵՆՔ CSRF-Ը ԵՎ ԷԼ ՆԵՐՔԵՎՈՒՄ ՉԵՆՔ ԿՐԿՆՈՒՄ ԱՅՆ
                .csrf(csrf -> csrf.disable())
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/cancel/**"))
                .authorizeHttpRequests(auth -> auth
                        // 2. ԱՎԵԼԱՑՆՈՒՄ ԵՆՔ EXCEL-Ի ՀԱՍՑԵՆ PERMIT_ALL ՑՈՒՑԱԿՈՒՄ (Որ 403 հաստատ չտա)
                        .requestMatchers("/", "/about", "/service", "/contact", "/gallery", "/doctors/**", "/departments/**", "/appointments/**", "/privacy", "/terms",
                                "/login", "/register/**", "/search/**",
                                "/pharmacy/**", "/profile", "/verify/**", "/api/voice/**", "/homevoice", "/forgot-password/**", "/error", "/reset-password/**",
                                "/css/**", "/images/**", "/static/**", "/uploads/**", "/js/**",
                                "/pharmacy-admin/medicines/upload-excel", "/lab/fix-old-data", "/pay/invoice/**"
                        ).permitAll()

                        // Պաշտպանված էջեր (Require specific roles)
                        .requestMatchers("/doctor/first-login-setup", "/doctor/first-login-setup/save").hasAnyAuthority("ROLE_DOCTOR", "ROLE_LAB_WORKER")
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/doctor/**").hasAuthority("ROLE_DOCTOR")
                        .requestMatchers("/profile/**", "/patient/**","/api/chat/**").hasAuthority("ROLE_PATIENT")
                        .requestMatchers("/lab/**","/lab/history").hasAuthority("ROLE_LAB_WORKER")
                        .requestMatchers("/api/admin/pharmacy/**", "/pharmacy-admin/**").hasAuthority("ROLE_PHARMACY_ADMIN")
                        .requestMatchers("/api/blockchain/**").hasAnyAuthority("ROLE_DOCTOR")
                        .requestMatchers("/radiologist/**").hasAuthority("ROLE_RADIOLOGIST")
                        .requestMatchers("/secure-image").authenticated()
                        .requestMatchers("/api/appointments/unrated-completed").hasAuthority("ROLE_PATIENT")
                        .requestMatchers("/rating/**","/api/pending-review").hasAuthority("ROLE_PATIENT")

                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("LifeSpringSecretKey2026") // Սա ապահովում է, որ տոկենը չկեղծվի
                        .tokenValiditySeconds(86400 * 30) // Համակարգը կհիշի օգտատիրոջը ուղիղ 30 օր
                        .rememberMeParameter("remember-me") // Սա պետք է խստորեն համընկնի HTML-ի name-ի հետ
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        // 3. ԱՅՍՏԵՂԻՑ ՋՆՋՎԵԼ Է ԵՐԿՐՈՐԴ .csrf()-Ը, ՈՐԸ ԽԱՌՆՈՒՄ ԷՐ ԱՄԵՆ ԻՆՉ

        // Tell Spring to use our provider
        http.authenticationProvider(authenticationProvider());


        return http.build();
    }
}