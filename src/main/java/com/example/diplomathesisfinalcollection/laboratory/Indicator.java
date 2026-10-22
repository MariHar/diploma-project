package com.example.diplomathesisfinalcollection.laboratory;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "indicators")
@Data
public class Indicator {
    @Id
    private String id;

    private String testName;
    private String indicatorKey;
    private String displayName;
    private String unit;

    private Double normMin;
    private Double normMax;

    private boolean active = true;
}