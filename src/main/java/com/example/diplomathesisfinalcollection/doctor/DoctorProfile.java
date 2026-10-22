//package com.example.diplomathesisfinalcollection.doctor;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Document(collection = "doctor_profiles")
//public class DoctorProfile {
//
//    @Id
//    private String id;
//
//    private String userId; // Links to User document
//
//    private String specialty; // Cardiologist, Dermatologist, etc.
//    private String education;
//    private int yearsOfExperience;
//
//    // Schedule
//    private String availableDays; // "Mon, Wed, Fri"
//    private String startTime;      // "09:00"
//    private String endTime;        // "17:00"
//
//    private String bio;
//
//    @Builder.Default
//    private boolean approved = false; // Admin must approve
//}