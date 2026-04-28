package com.example.diplomathesisfinalcollection.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    private String id;

    // Կապեր մյուս աղյուսակների հետ
    private String appointmentId;
    private String patientId;
    private String doctorId;

    private LocalDateTime recordDate; // Երբ է լրացվել

    // Բուն բժշկական տվյալները
    private String symptoms;      // Գանգատներ / Ախտանիշներ
    private String diagnosis;     // Ախտորոշում
    private String treatment;     // Նշանակված բուժում / Դեղորայք
    private String notes;         // Լրացուցիչ նշումներ (օր.՝ հաջորդ այցի խորհուրդներ)
}
