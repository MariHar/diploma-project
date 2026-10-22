package com.example.diplomathesisfinalcollection.notification;


public interface NotificationStrategy {
    void sendVerificationCode(String recipient, String code);

    boolean supports(String type);


    void sendPasswordResetCode(String recipient, String code); // 🔴 NEW METHOD

}