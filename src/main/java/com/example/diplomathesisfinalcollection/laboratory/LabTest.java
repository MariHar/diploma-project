package com.example.diplomathesisfinalcollection.laboratory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "lab_tests")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabTest {
    @Id
    private String id;
    private String appointmentId;
    private String patientId;
    private String doctorId; // Ուղեգրող բժիշկը
    private String labWorkerId; // Անալիզն անող աշխատակիցը

    private String testName; // Օր.՝ "Արյան ընդհանուր քննություն"
    private String category; // Օր.՝ "Լաբորատոր", "Ռենտգեն", "ՄՌՏ"

    private String status; // PENDING, COMPLETED

    private LocalDateTime orderDate; // Երբ է բժիշկը ուղեգրել
    private LocalDateTime completionDate; // Երբ է պատասխանը պատրաստ եղել

    // Դինամիկ տվյալների համար (Key-Value)
    private Map<String, String> results;

    private String resultNotes; // Լաբորանտի մեկնաբանությունը
    private String fileUrl; // Եթե PDF կամ նկար են կցում

    private List<Map<String, String>> detailedResults;
    private String patientFullName;
    private String doctorFullName;
    private String doctorSpecialization;
    private String serviceId; // 👈 Սա ավելացրու դաշտերի մեջ
    // Inside your LabTest.java and InstrumentalExam.java classes,
// you should add the following field:

    @Builder.Default
    private boolean isInvoiced = false;
}