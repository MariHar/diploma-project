package com.example.diplomathesisfinalcollection.services;

import com.example.diplomathesisfinalcollection.notification.NotificationStrategy;
import com.example.diplomathesisfinalcollection.verifiaction.VerificationCode;
import com.example.diplomathesisfinalcollection.verifiaction.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeRepository repository;
    private final List<NotificationStrategy> notificationStrategies;
    private static final int EXPIRATION_MINUTES = 10;

    public void generateAndSendCode(String email, String phone, String preferredMethod) {
        // 1. Clean up any existing codes for this user
        repository.deleteByEmail(email);

        // 2. Generate 6-digit secure code
        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        // 3. Save to MongoDB
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                .build();
        repository.save(verificationCode);

        // 4. Send using the requested strategy (Email or SMS)
        String recipient = "SMS".equalsIgnoreCase(preferredMethod) ? phone : email;

        notificationStrategies.stream()
                .filter(strategy -> strategy.supports(preferredMethod))
                .findFirst()
                .ifPresent(strategy -> strategy.sendVerificationCode(recipient, code));
    }

    public boolean verifyCode(String email, String code) {
        return repository.findByEmailAndCode(email, code)
                .filter(vc -> vc.getExpiresAt().isAfter(Instant.now())) // Extra check in case Mongo TTL hasn't run yet
                .isPresent();
    }

    public void generateAndSendResetCode(String email, String preferredMethod) {
        // 1. Clean up old codes
        repository.deleteByEmail(email);

        // 2. Generate new 6-digit code
        String code = String.format("%06d", new java.security.SecureRandom().nextInt(999999));

        // 3. Save to MongoDB (Reusing our TTL collection!)
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                .build();
        repository.save(verificationCode);

        // 4. Send the Reset Code
        notificationStrategies.stream()
                .filter(strategy -> strategy.supports(preferredMethod))
                .findFirst()
                .ifPresent(strategy -> strategy.sendPasswordResetCode(email, code)); // 🔴 Calls the new method
    }
}