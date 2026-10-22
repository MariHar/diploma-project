package com.example.diplomathesisfinalcollection.verifiaction;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "verification_codes")
public class VerificationCode {

    @Id
    private String id;

    @Indexed // Speeds up queries
    private String email;

    private String code;

    // 🔴 TTL Index: MongoDB will auto-delete this document when the time is reached
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}