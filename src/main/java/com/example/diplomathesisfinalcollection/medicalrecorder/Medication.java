package com.example.diplomathesisfinalcollection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Medication {
    private String name;       // Դեղի անունը
    private String dosage;     // Չափաբաժինը (օր.՝ 500մգ)
    private String frequency;  // Քանի անգամ (օր.՝ 2 անգամ օրը)
    private String duration;   // Տևողությունը (օր.՝ 7 օր)
}
