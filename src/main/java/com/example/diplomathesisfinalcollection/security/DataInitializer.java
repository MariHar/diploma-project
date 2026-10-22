package com.example.diplomathesisfinalcollection.security;

import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Ստեղծում ենք Գլխավոր Ադմինին
        if (userRepository.findByEmail("admin@clinic.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@clinic.com")
                    .passwordHash(passwordEncoder.encode("admin123")) // Securely encrypted
                    .firstName("Super")
                    .lastName("Admin")
                    .role("ROLE_ADMIN")
                    // Եթե ունես isEnabled կամ isVerified դաշտեր, ավելացրու այստեղ՝ .isEnabled(true)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ DEFAULT ADMIN CREATED: admin@clinic.com");
        }

        // 2. Ստեղծում ենք Դեղատան Ադմինին
        if (userRepository.findByEmail("pharmacy@clinic.com").isEmpty()) {
            User pharmacyAdmin = User.builder()
                    .email("pharmacy@clinic.com")
                    .passwordHash(passwordEncoder.encode("Pharmacy2026!"))
                    .firstName("Դեղատուն")
                    .lastName("Ադմինիստրատոր")
                    .role("ROLE_PHARMACY_ADMIN")
                    .build();

            userRepository.save(pharmacyAdmin);
            System.out.println("✅ PHARMACY ADMIN CREATED: pharmacy@clinic.com");
        }

        if (userRepository.findByEmail("radio@clinic.com").isEmpty()) {
            User radiologist = User.builder()
                    .email("radio@clinic.com")
                    .passwordHash(passwordEncoder.encode("radiologist123"))
                    .firstName("Main")
                    .lastName("Radiologist")
                    .role("ROLE_RADIOLOGIST")
                    .build();

            userRepository.save(radiologist);
            System.out.println("✅ Radiologist CREATED: radio@clinic.com");
        }
        if (userRepository.findByEmail("laboratory@clinic.com").isEmpty()) {
            User labWorker = User.builder()
                    .email("laboratory@clinic.com")
                    .passwordHash(passwordEncoder.encode("laboratory123"))
                    .firstName("Main")
                    .lastName("Laboratory")
                    .role("ROLE_LAB_WORKER")
                    .build();

            userRepository.save(labWorker);
            System.out.println("✅ LAB WORKER CREATED: laboratory@clinic.com");
        }
    }

//    @PostConstruct
//    public void createEssentialUsers() {
//        // --- Create Radiologist User ---
//        // We check if the user already exists before creating them.
//        if (userRepository.findByEmail("radio@hospital.com").isEmpty()) {
//            User radiologist = User.builder()
//                    .email("radio@hospital.com")
//                    .firstName("Main")
//                    .lastName("Radiologist")
//                    .passwordHash(passwordEncoder.encode("radiologist123")) // Use a secure password
//                    .role("ROLE_RADIOLOGIST")
//                    .build();
//            userRepository.save(radiologist);
//
//            // This will print the credentials to the console on startup
//            System.out.println("--- ESSENTIAL USER CREATED ---");
//            System.out.println("Radiologist Email: radio@hospital.com");
//            System.out.println("Radiologist Password: password123");
//            System.out.println("------------------------------");
//        }
//    }
}