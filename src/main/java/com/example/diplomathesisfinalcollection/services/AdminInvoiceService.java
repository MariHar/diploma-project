package com.example.diplomathesisfinalcollection.services;

import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminInvoiceService {

    private final InvoiceRepository invoiceRepository;

    // Վերադարձնում է բոլոր չհաստատված (DRAFT) հաշիվները
    public List<Invoice> getAllDraftInvoices() {
        return invoiceRepository.findByStatus(InvoiceStatus.DRAFT);
    }

    // Ադմինը հաստատում է հաշիվը, և այն դառնում է ենթակա վճարման
    public void approveInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.PENDING_PAYMENT);
            invoiceRepository.save(invoice);
        } else {
            throw new IllegalStateException("Only DRAFT invoices can be approved.");
        }
    }
}