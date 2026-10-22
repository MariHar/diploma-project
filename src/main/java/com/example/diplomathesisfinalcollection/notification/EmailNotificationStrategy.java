package com.example.diplomathesisfinalcollection.notification;

import com.example.diplomathesisfinalcollection.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationStrategy implements NotificationStrategy {

    // 🔴 INJECT YOUR EXISTING EMAIL SERVICE HERE
    // (Replace 'YourExistingEmailService' with the actual class name you use for appointments)
    private final EmailService emailService;

    @Override
    public void sendVerificationCode(String email, String code) {

        String subject = "Your Medical Portal Verification Code";
        String body = "Welcome to the Medical Management System!\n\n" +
                "Your account verification code is: " + code + "\n\n" +
                "This code will expire in 10 minutes.";

        // 🔴 CALL YOUR EXISTING METHOD HERE
        emailService.sendSimpleMessage(email, subject, body);

        System.out.println("✅ Verification Email sent to: " + email);
    }

    @Override
    public boolean supports(String type) {
        return "EMAIL".equalsIgnoreCase(type);
    }

    @Override
    public void sendPasswordResetCode(String email, String code) {

        String subject = "Password Reset Request - Medical Portal";
        String body = "We received a request to reset your password.\n\n" +
                "Your password reset code is: " + code + "\n\n" +
                "This code will expire in 10 minutes. If you did not request this, please ignore this email.";

        // 🔴 USE YOUR CUSTOM EMAIL SERVICE HERE (Same as you did in sendVerificationCode)
        // For example:
        emailService.sendSimpleMessage(email, subject, body);

        System.out.println("✅ Password Reset Email sent to: " + email);
    }
}