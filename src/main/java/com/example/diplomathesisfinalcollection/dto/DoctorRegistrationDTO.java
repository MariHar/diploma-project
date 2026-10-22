package com.example.diplomathesisfinalcollection.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class DoctorRegistrationDTO {
    private String email;
     private String phone;

     private String firstName;
    private String lastName;
    private String patronymic;
    private String specialization;
    private Integer experienceYears;
    private String description;
    private MultipartFile photoFile;
    private String education;
    private List<String> languagesSpoken = new ArrayList<>();

    private String departmentId;
    private List<String> serviceIds;
}