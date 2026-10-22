package com.example.diplomathesisfinalcollection.medicine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "pharmacy_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyOrder {
    @Id
    private String id;

    private String patientId;
    private String medicalRecordId;

    // 🔴 ՆՈՐ ԴԱՇՏԵՐ. Առաքման և Բժշկի տվյալների պատմություն
    private String deliveryAddress;      // Պացիենտի հասցեն գնման պահին
    private String deliveryPhone;        // Պացիենտի հեռախոսը գնման պահին
    private String prescribingDoctor;    // Բժշկի Անուն Ազգանուն
    private String doctorSpecialization; // Բժշկի մասնագիտացումը

    private List<String> purchasedDrugs;
    private Double totalAmount;
    private String status;
    private LocalDateTime orderDate;
}