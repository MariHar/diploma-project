package com.example.diplomathesisfinalcollection.user;

import com.example.diplomathesisfinalcollection.dto.UserRegistrationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerNewPatient(UserRegistrationDTO dto) {
        User newPatient = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role("ROLE_PATIENT")
                .enabled(false)
                .build();

        UserProfile profile = new UserProfile();
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setDateOfBirth(dto.getBirthDate());
        profile.setGender(dto.getGender());
        newPatient.setUserProfile(profile);

        return userRepository.save(newPatient);
    }

    public void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setEnabled(true);
            userRepository.save(user);
        });
    }

    public void updatePassword(String email, String newRawPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newRawPassword));
            userRepository.save(user);
        });
    }

}