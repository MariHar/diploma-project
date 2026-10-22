package com.example.diplomathesisfinalcollection.notification;


import org.springframework.stereotype.Service;

@Service
public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public void sendVerificationCode(String phone, String code) {
        System.out.println("📱 Sending SMS to " + phone + ". Verification Code: " + code);
        // TODO: Implement Twilio logic here later
    }

    @Override
    public boolean supports(String type) {
        return "SMS".equalsIgnoreCase(type);
    }
    @Override
    public void sendPasswordResetCode(String phone, String code) {
        System.out.println("📱 Sending PASSWORD RESET SMS to " + phone + ". Code: " + code);
        // TODO: Implement Twilio logic here later
    }
}