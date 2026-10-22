package com.example.diplomathesisfinalcollection.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FinancialReportDTO {
    private String invoiceId;
    private String patientName;
    private String doctorName;
    private String departmentName;
    private String serviceName;
    private Double amount;
    private String status; // PAID, PENDING, UNPAID
    private LocalDateTime issueDate;
}