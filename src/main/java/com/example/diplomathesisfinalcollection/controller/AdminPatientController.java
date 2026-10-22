package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPatientController {
    @Autowired
    private UserRepository userRepository;

    // 1. ՑՈՒՑԱԴՐԵԼ ԲՈԼՈՐ ՊԱՑԻԵՆՏՆԵՐԻՆ
    @GetMapping("/patients")
    public String viewPatients(Model model) {
        // Ենթադրվում է, որ դու ունես findByRole մեթոդը քո UserRepository-ում
        List<User> patients = userRepository.findByRole("ROLE_PATIENT");
        model.addAttribute("patients", patients);
        return "admin/patients"; // Thymeleaf էջի անունը
    }

    // 2. ԱՊԱԲԼՈԿԱՎՈՐԵԼ ԿԱՄ ԲԼՈԿԱՎՈՐԵԼ ՊԱՑԻԵՆՏԻՆ
    @PostMapping("/patients/toggle-status/{id}")
    public String togglePatientStatus(@PathVariable String id) {
        User patient = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Պացիենտը չի գտնվել"));

        // Փոխում ենք կարգավիճակը հակառակի
        boolean newStatus = !patient.isEnabled();
        patient.setEnabled(newStatus);

        // Եթե ադմինը ԱՊԱԲԼՈԿԱՎՈՐՈՒՄ է (դարձնում է true), պետք է զրոյացնել չեղարկումները
        if (newStatus) {
            patient.setCancellationCount(0);
            System.out.println("✅ Պացիենտը ապաբլոկավորվեց և քանակը զրոյացավ: " + patient.getEmail());
        } else {
            System.out.println("❌ Պացիենտը բլոկավորվեց ադմինի կողմից: " + patient.getEmail());
        }

        userRepository.save(patient);
        return "redirect:/admin/patients";
    }
}
