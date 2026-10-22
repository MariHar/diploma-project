package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.rating.Rating;
import com.example.diplomathesisfinalcollection.rating.RatingRepository;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/dashboard")
    public String showRatingsDashboard(Model model) {
        // 1. Fetch all ratings from newest to oldest
        List<Rating> allRatings = ratingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        model.addAttribute("ratings", allRatings);

        // 2. Pass repositories to the model so Thymeleaf can resolve names dynamically
        model.addAttribute("userRepo", userRepository);
        model.addAttribute("doctorRepo", doctorRepository);
        model.addAttribute("apptRepo", appointmentRepository);

        return "admin/ratings-dashboard";
    }
}