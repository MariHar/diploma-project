//package com.example.diplomathesisfinalcollection.rating;
//
//import com.example.diplomathesisfinalcollection.dto.RatingRequestDTO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//public class RatingService {
//
//    @Autowired
//    private RatingRepository ratingRepository;
//
//    public Rating submitRating(RatingRequestDTO ratingRequest, String patientId) {
//        // 1. Prevent duplicate ratings for the same appointment
//        if (ratingRepository.existsByAppointmentId(ratingRequest.getAppointmentId())) {
//            throw new IllegalStateException("A rating for this appointment has already been submitted.");
//        }
//
//        // 2. Build the Rating object from the DTO
//        Rating newRating = Rating.builder()
//                .appointmentId(ratingRequest.getAppointmentId())
//                .doctorId(ratingRequest.getDoctorId())
//                .patientId(patientId) // Set the logged-in patient's ID
//                .score(ratingRequest.getScore())
//                .comment(ratingRequest.getComment())
//                .isAnonymous(ratingRequest.getIsAnonymous())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        // 3. Save to the database
//        return ratingRepository.save(newRating);
//    }
//}
