package com.example.diplomathesisfinalcollection.instrumental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "instrumental_exams")
public class InstrumentalExam {

    @Id
    private String id;

    // Links
    private String patientId;
    private String orderingDoctorId;
    private String appointmentId;
    private String medicalRecordId;

    // Order Details
    private String examType; // e.g., "MRI", "X-Ray", "CT Scan"
    private String areaToExamine; // e.g., "Chest", "Head", "Left Knee"
    private String clinicalReason; // Doctor's notes for the radiologist

    private LocalDateTime orderDate;
    private String status; // PENDING, COMPLETED

    // Radiologist's Results (filled in later)
    private String radiologistId;
    private String findings; // The text conclusion
    private String reportFilePath; // Path to the uploaded image/PDF
    private LocalDateTime completionDate;
    private String serviceId; // 👈 Սա նույնպես
// Inside your LabTest.java and InstrumentalExam.java classes,
// you should add the following field:

    @Builder.Default
    private boolean isInvoiced = false;}