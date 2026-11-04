package com.example.diplomathesisfinalcollection.blockchainaudit;


import com.example.diplomathesisfinalcollection.dto.CardHashDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "audit_logs")
public class BlockChainAudit {

    @Id
    private String id;
    private String recordId;
    private String hash;
    private String previousHash;
    private LocalDateTime timestamp;
    private CardHashDTO dataSnapshot;
    private List<String> changesSummary;

    public BlockChainAudit(String recordId, String hash, String previousHash, CardHashDTO dataSnapshot, List<String> changesSummary) {
        this.recordId = recordId;
        this.hash = hash;
        this.previousHash = previousHash;
        this.dataSnapshot = dataSnapshot;
        this.changesSummary = changesSummary;
        this.timestamp = LocalDateTime.now();
    }

}