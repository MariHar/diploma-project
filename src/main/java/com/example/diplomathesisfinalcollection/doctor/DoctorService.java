//package com.example.diplomathesisfinalcollection.doctor;
//
//import com.example.diplomathesisfinalcollection.dto.DoctorRegistrationDTO;
//import com.example.diplomathesisfinalcollection.user.User;
//import com.example.diplomathesisfinalcollection.dto.UserRegistrationDTO;
//import com.example.diplomathesisfinalcollection.user.UserRepository;
//import com.example.diplomathesisfinalcollection.user.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class DoctorService {
//
//    private final UserRepository userRepository;
//    private final DoctorProfileRepository doctorProfileRepository;
//    private final UserService userService;
//    private final PasswordEncoder passwordEncoder;
//
//    public void registerDoctor(DoctorRegistrationDTO dto) {
//        // 1. Create User (disabled until approved)
//        User user = User.builder()
//                .firstName(dto.getFirstName())
//                .lastName(dto.getLastName())
//                .email(dto.getEmail())
//                .phone(dto.getPhone())
//                .passwordHash(passwordEncoder.encode(dto.getPassword()))
//                .role("ROLE_DOCTOR")
//                .enabled(false) // 🔴 Must be approved by Admin first
//                .build();
//
//        User savedUser = userRepository.save(user);
//
//        // 2. Create Doctor Profile
//        DoctorProfile profile = DoctorProfile.builder()
//                .userId(savedUser.getId())
//                .specialty(dto.getSpecialty())
//                .education(dto.getEducation())
//                .yearsOfExperience(dto.getYearsOfExperience())
//                .availableDays(dto.getAvailableDays())
//                .startTime(dto.getStartTime())
//                .endTime(dto.getEndTime())
//                .bio(dto.getBio())
//                .approved(false) // 🔴 Awaiting admin approval
//                .build();
//
//        doctorProfileRepository.save(profile);
//    }
//
//    public void approveDoctor(String userId) {
//        doctorProfileRepository.findByUserId(userId).ifPresent(profile -> {
//            profile.setApproved(true);
//            doctorProfileRepository.save(profile);
//        });
//
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setEnabled(true);
//            userRepository.save(user);
//        });
//    }
//
//    public DoctorProfile getProfileByUserId(String userId) {
//        return doctorProfileRepository.findByUserId(userId).orElse(null);
//    }
//}