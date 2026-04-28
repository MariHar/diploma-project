package com.example.medicalweb.user;

import com.example.medicalweb.team.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private TeamRepository teamRepository;


    @Autowired
    private UserRepository userRepository;

    public String getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    public User register(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        User savedUser = repo.save(user);

        return savedUser;
    }

    public boolean login(String username, String password) {
        return repo.findByUsername(username)
                .map(u -> u.getPasswordHash().equals(password))
                .orElse(false);
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }


    public User getUserProfile(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    public User updateProfile(User user) {
        return repo.save(user);
    }

    public User authenticate(String username, String password) {
        Optional<User> userOptional = repo.findByUsername(username);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        } else {
            return null;
        }
    }

}

