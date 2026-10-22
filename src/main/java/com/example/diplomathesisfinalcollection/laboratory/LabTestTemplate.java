package com.example.diplomathesisfinalcollection.laboratory;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lab_test_templates")
@Data
@Builder
public class LabTestTemplate {
    @Id
    private String id;
    private String testName; // e.g., "Glucose"
    private String unit;     // e.g., "mg/dL"
    private Double minNorm;  // e.g., 70.0
    private Double maxNorm;  // e.g., 100.0
    private String description; // What this test is for
}