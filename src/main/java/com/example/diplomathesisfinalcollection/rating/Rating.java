package com.example.diplomathesisfinalcollection.rating;

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
@Document(collection = "ratings")
public class Rating {
    @Id
    private String id;

    private String appointmentId;
    private String doctorId;
    private String patientId;
    private int score;
    private String comment;
    private boolean isAnonymous;
    private LocalDateTime createdAt;
}