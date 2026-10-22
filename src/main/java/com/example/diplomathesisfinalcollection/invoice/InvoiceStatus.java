package com.example.diplomathesisfinalcollection.invoice;

public enum InvoiceStatus {
    DRAFT,           // The invoice is actively being built (e.g., tests are being added). Not visible to the patient yet.
    PENDING_PAYMENT, // The invoice is finalized and awaiting payment from the patient.
    PAID,            // Payment confirmed.
    VOID             // Invoice cancelled by an admin.
}