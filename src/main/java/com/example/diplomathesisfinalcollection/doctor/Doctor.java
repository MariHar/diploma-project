package com.example.diplomathesisfinalcollection.doctor;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;



import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctors")
public class Doctor {
    @Id
    private String id;

    private String userId;
    private String departmentId;

    // Հիմնական տվյալներ (AM)
    private String firstName;
    private String lastName;
    private String patronymic;
    private String specialization;
    private String description;
    private String education; // Կրթություն

    // 🔴 Թարգմանությունների Map-եր բոլոր դաշտերի համար
//

    private String photoUrl;
    private String phone;
    private String email;
    private Double rating;
    private Integer experienceYears;
    private String roomNumber;

    @Builder.Default
    private List<String> serviceIds = new ArrayList<>();

    @Builder.Default
    private List<String> languagesSpoken = new ArrayList<>(); // Օր.՝ ["am", "en"]

    @Builder.Default
    private boolean active = true;


//    @Builder.Default
//    private Map<String, String> firstNameTrans = new HashMap<>();
//    @Builder.Default
//    private Map<String, String> lastNameTrans = new HashMap<>();
//    @Builder.Default
//    private Map<String, String> patronymicTrans = new HashMap<>();
//    @Builder.Default
//    private Map<String, String> specTrans = new HashMap<>();
//    @Builder.Default
//    private Map<String, String> descTrans = new HashMap<>();
//    @Builder.Default
//    private Map<String, String> eduTrans = new HashMap<>();
}