package com.example.diplomathesisfinalcollection.medicalrecorder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    private String id;
    @Field("block_hash")
    private String blockHash;

    private String appointmentId;
    private String patientId;
    private String doctorId;

    private LocalDateTime recordDate;

    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String contraindications;
    private String notes;
    private LocalDate followUpDate;
    private boolean adminUnlocked = false;
    private String editRequestReason;
    private String status;
    private List<Medication> prescriptions;
    private String objectiveExamination;

    private String complaints;
    private String anamnesisMorbi;
    private String anamnesisVitae;

    private String inspection;      // Դիտում
    private String palpation;       // Շոշափում
    private String percussion;      // Թակոց
    private String auscultation;    // Լսում

    private List<InstrumentalExam> instrumentalExams;



    @Data
    public static class InstrumentalExam {
        private String type;
        private String area;
        private String findings;
        private String imageUrl;
    }
}

