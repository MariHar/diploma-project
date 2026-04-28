package com.example.diplomathesisfinalcollection.medicalrecorder; // Համոզվիր, որ փաթեթի անունը ճիշտ է

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "pharmacy_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyOrder {

    @Id
    private String id;

    // Ո՞վ է պատվիրել և որ դեղատոմսի (քարտի) հիման վրա
    private String patientId;
    private String medicalRecordId;

    // Վճարման մանրամասներ
    private Double totalAmount; // Ընդհանուր գումարը
    private String paymentMethod; // "CARD_ONLINE" կամ "CASH"

    // Պատվերի վիճակը: PENDING (Սպասվում է վճարման), PAID (Վճարված է), DELIVERED (Առաքված է)
    private String status;

    private LocalDateTime orderDate;
}