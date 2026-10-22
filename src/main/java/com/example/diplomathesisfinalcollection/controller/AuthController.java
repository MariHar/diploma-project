package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.dto.UserRegistrationDTO;
import com.example.diplomathesisfinalcollection.services.VerificationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


//import com.example.diplomathesisfinalcollection.user.UserRegistrationDTO;
import com.example.diplomathesisfinalcollection.user.UserService;
//import com.example.diplomathesisfinalcollection.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Valid
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationService verificationService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "verified", required = false) String verified,
                            @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
                            Model model) {
        if (error != null) model.addAttribute("errorMsg", "Invalid email or password.");
        if (verified != null) model.addAttribute("successMsg", "Account verified successfully! You can now log in.");
        if (resetSuccess != null) model.addAttribute("successMsg", "Password reset successfully! Please log in with your new password."); // 🔴 NEW
        return "login";
    }
    @PostMapping("/register/save")
    public String registerPatient(@ModelAttribute UserRegistrationDTO dto,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (userService.existsByEmail(dto.getEmail())) {
            model.addAttribute("errorMsg", "Email is already registered!");
            model.addAttribute("userDto", dto);
            return "register";
        }

        userService.registerNewPatient(dto);

        verificationService.generateAndSendCode(dto.getEmail(), dto.getPhone(), "EMAIL");

        redirectAttributes.addAttribute("email", dto.getEmail());
        return "redirect:/verify";
    }

    // --- VERIFICATION ---

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDto", new UserRegistrationDTO());
        return "register";
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam("email") String email, Model model) {
        // Serve the separate verify.html page
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify/submit")
    public String submitVerification(@RequestParam("email") String email,
                                     @RequestParam("code") String code,
                                     Model model) {

        boolean isValid = verificationService.verifyCode(email, code);

        if (isValid) {
            // Activate user and redirect to login with a success flag
            userService.activateUser(email);
            return "redirect:/login?verified=true";
        } else {
            // Stay on the verify page and show an error
            model.addAttribute("email", email);
            model.addAttribute("errorMsg", "Invalid or expired verification code. Please try again.");
            return "verify";
        }
    }
}
