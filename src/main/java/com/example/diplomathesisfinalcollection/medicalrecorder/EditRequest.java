package com.example.diplomathesisfinalcollection.medicalrecorder;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "edit_requests")
@Data
@Builder
public class EditRequest {
    @Id
    private String id;
    private String recordId;
    private String doctorId;
    private String doctorName;
    private String patientName;
    private String requestReason;
    private LocalDateTime requestDate; // Երբ է ուղարկվել հարցումը

    private String status; // PENDING, APPROVED, REJECTED
    private String adminComment; // Եթե ադմինը մերժում է

    private LocalDateTime approvalDate;
    private LocalDateTime expiryDate; // 🔴 Սա այն ժամանակն է, երբ խմբագրումը կփակվի (օր.՝ հաստատումից 2 ժամ անց)

    // Սնեփշոթ (Snapshot)՝ ինչ տեսք ուներ քարտը մինչև փոփոխությունը

    private String originalDiagnosis;
    private String originalTreatment;
    private LocalDate originalFollowUpDate; // 🔴 ՆՈՐ

    // 🔴 ՆՈՐ: Ի՞նչ դարձավ ՀԵՏՈ
    private String newDiagnosis;
    private String newTreatment;
    private LocalDate newFollowUpDate;      // 🔴 ՆՈՐ
}
