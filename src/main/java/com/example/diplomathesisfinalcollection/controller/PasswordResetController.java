package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.user.UserService;
import com.example.diplomathesisfinalcollection.services.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final VerificationService verificationService;

    // --- 1. REQUEST PASSWORD RESET ---

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password/submit")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (!userService.existsByEmail(email)) {
            model.addAttribute("errorMsg", "We could not find an account with that email address.");
            return "forgot-password";
        }

        // Generate the code and send the email
        verificationService.generateAndSendResetCode(email, "EMAIL");

        // Redirect to the reset page (PRG Pattern)
        redirectAttributes.addAttribute("email", email);
        return "redirect:/reset-password";
    }

    // --- 2. ENTER CODE AND NEW PASSWORD ---

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password/submit")
    public String processResetPassword(@RequestParam("email") String email,
                                       @RequestParam("code") String code,
                                       @RequestParam("newPassword") String newPassword,
                                       Model model) {

        boolean isValid = verificationService.verifyCode(email, code);

        if (isValid) {
            // Update the password in MongoDB
            userService.updatePassword(email, newPassword);

            // Clean up the code so it can't be used twice
            verificationService.generateAndSendResetCode(email, "CLEANUP_ONLY"); // Or just add a delete method to your service

            return "redirect:/login?resetSuccess=true";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("errorMsg", "Invalid or expired reset code. Please try again.");
            return "reset-password";
        }
    }
}