package com.example.diplomathesisfinalcollection.clinicservice;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clinic_services")
public class ClinicService {
    @Id
    private String id;

    private String code;
    private String departmentId;

    private String name;
    private String description;

    private String preparationRules;

    private Double price;
    private Integer durationMinutes;
    @Builder.Default
    private boolean active = true;
}