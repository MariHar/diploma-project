package com.example.diplomathesisfinalcollection.security;


import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        // 🔴 FIX: Use the 7-parameter constructor to include the 'enabled' status
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isEnabled(),      // 1. enabled (must be true for login)
                true,                  // 2. accountNonExpired
                true,                  // 3. credentialsNonExpired
                true,                  // 4. accountNonLocked
                Collections.singletonList(authority)
        );

        /* ALTERNATIVE (Cleaner):
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authority)
                .disabled(!user.isEnabled()) // Spring Security uses 'disabled' (inverse of enabled)
                .build();
        */
    }
}