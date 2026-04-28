package com.example.diplomathesisfinalcollection.blockchain;


import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "blockchain_records")
public class BlockchainMedicalRecord {

    @Id
    private String id;

    @Indexed
    private String patientId;

    private String clinicalData;

    private LocalDateTime timestamp;

    private String previousHash;

    private String currentHash;
}