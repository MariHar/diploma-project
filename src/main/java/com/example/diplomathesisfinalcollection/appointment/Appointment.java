package com.example.diplomathesisfinalcollection.appointment;


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
@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;

    private String patientId;
    private String doctorId;
    private String serviceId;

    private Double price;
    private Double totalAmount;
    private boolean isPaid = false;
    private boolean isInvoiced = false;

    private String referredFromId;
    private String referredToDeptId;
    private String referralReason;
    private LocalDateTime date;
    private String status;

    private String cancelReason;
    private boolean wasNotified;
    private boolean isRated = false;
    private boolean isSkipped = false;

}
