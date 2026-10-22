package com.example.diplomathesisfinalcollection.controller; // կամ քո համապատասխան փաթեթը

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.rating.Rating;
import com.example.diplomathesisfinalcollection.rating.RatingRepository;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/rating")
public class RatingController {

    @Autowired private RatingRepository ratingRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;

    // 1. Պահպանել գնահատականը
    @PostMapping("/save")
    public String saveRating(@RequestParam String appointmentId,
                             @RequestParam String doctorId,
                             @RequestParam int score,
                             @RequestParam(required = false) String comment,
                             @RequestParam(defaultValue = "false") boolean isAnonymous,
                             Principal principal) {

        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        if (patient == null) return "redirect:/login";

        Rating rating = Rating.builder()
                .appointmentId(appointmentId)
                .doctorId(doctorId)
                .patientId(patient.getId())
                .score(score)
                .comment(comment)
                .isAnonymous(isAnonymous)
                .createdAt(LocalDateTime.now())
                .build();

        ratingRepository.save(rating);

        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setRated(true);
            appointmentRepository.save(appt);
        }

        // Վերադարձնում ենք պրոֆիլի էջ
        return "redirect:/profile?ratingSuccess=true";
    }

    @PostMapping("/skip")
    public String skipRating(@RequestParam String appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setSkipped(true);
            appointmentRepository.save(appt);
        }

        return "redirect:/profile";
    }
}