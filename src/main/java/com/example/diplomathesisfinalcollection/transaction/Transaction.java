package com.example.diplomathesisfinalcollection.transaction;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String patientId;
    private String invoiceId; // Reference to the invoice being paid
    private ItemType itemType; // APPOINTMENT, PHARMACY_ORDER, LAB_TEST_PACKAGE
    private Double amount;
    private String currency; // "AMD"
    private TransactionStatus status; // PENDING, COMPLETED, FAILED
    private String paymentGateway; // "Stripe"
    private String paymentGatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum ItemType { APPOINTMENT, PHARMACY_ORDER, CLINIC_SERVICE }
    public enum TransactionStatus { PENDING, COMPLETED, FAILED, REFUNDED }
}