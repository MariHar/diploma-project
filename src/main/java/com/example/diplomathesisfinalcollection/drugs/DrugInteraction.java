package com.example.diplomathesisfinalcollection.drugs;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "drug_interactions")
public class DrugInteraction {
    @Id
    private String id;
    private String drugA;
    private String drugB;
    private String severity; // Օրինակ՝ "HIGH", "MODERATE"
    private String warningMessage;
}
