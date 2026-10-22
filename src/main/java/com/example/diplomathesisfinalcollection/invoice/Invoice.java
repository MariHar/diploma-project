package com.example.diplomathesisfinalcollection.invoice;

import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "invoices")
public class Invoice {

    @Id
    private String id;
    private String patientId;
    private String appointmentId; // Ensure THIS field exists


    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Builder.Default
    private List<LineItem> lineItems = new ArrayList<>();

    private Double totalAmount;
    private LocalDateTime issueDate;
    private LocalDateTime paidDate;

    // This will store the transaction ID from Stripe (or our mock system)
    private String paymentGatewayTransactionId;

    // Inner static class for line items
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        private String sourceId; // The ID of the Appointment, LabTest, InstrumentalExam, etc.
        private String description; // e.g., "Consultation with Cardiologist", "Complete Blood Count"
        private int quantity;
        private Double unitPrice;
    }
}