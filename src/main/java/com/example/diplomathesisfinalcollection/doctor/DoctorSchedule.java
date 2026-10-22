package com.example.diplomathesisfinalcollection.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctor_schedules")
public class DoctorSchedule {
    @Id
    private String id;

    private String doctorId;

    // Շաբաթվա օրը (օրինակ՝ "MONDAY", "TUESDAY")
    private String dayOfWeek;

    // Սկիզբ և Ավարտ (օրինակ՝ "09:00" և "15:00")
    private String startTime;
    private String endTime;

    private boolean hasBreak;
    private String breakStartTime;
    private String breakEndTime;

    // Եթե այս օրը հանգստյան է, ապա false
    private boolean isWorkingDay;
}