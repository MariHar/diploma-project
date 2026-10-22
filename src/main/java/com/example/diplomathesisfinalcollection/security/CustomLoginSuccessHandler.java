package com.example.diplomathesisfinalcollection.security;

import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        User user = userRepository.findByEmail(authentication.getName()).orElse(null);

        // 1. Առաջին մուտքի վերահսկում (Универсальный подход)
        if (user != null && user.isForcePasswordChange()) {
            String role = user.getRole();
            if (role.equals("ROLE_LAB_WORKER") || role.equals("ROLE_DOCTOR")) {
                response.sendRedirect("/doctor/first-login-setup");
                return; // ԿԱՐԵՎՈՐ Է. կանգնեցնում ենք կոդի ընթացքը այստեղ
            }
        }

        // 2. Ստանդարտ Ուղղորդում (Dashboard-ներ)
        String redirectUrl = "/";
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard"; break;
            } else if (role.equals("ROLE_DOCTOR")) {
                redirectUrl = "/doctor/dashboard"; break;
            } else if (role.equals("ROLE_LAB_WORKER")) {
                redirectUrl = "/lab/dashboard"; break;
            } else if (role.equals("ROLE_PATIENT")) {
                redirectUrl = "/profile"; break;
            }else if (role.equals("ROLE_PHARMACY_ADMIN")) {
                redirectUrl = "/pharmacy-admin/dashboard"; break;
            }else if (role.equals("ROLE_RADIOLOGIST")) {
                redirectUrl = "/radiologist/dashboard"; break;
            }
        }
        response.sendRedirect(redirectUrl);
    }
}