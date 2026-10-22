package com.example.diplomathesisfinalcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private String id;
    private String patientName;
    private String doctorName;
    private String serviceName;
    private String departmentId; // Քանի որ բազայում id է պահվում
    private LocalDateTime date;
    private String status;
}