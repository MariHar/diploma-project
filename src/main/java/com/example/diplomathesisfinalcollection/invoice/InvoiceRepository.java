package com.example.diplomathesisfinalcollection.invoice;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    // This custom query method is crucial for our BillingService.
    // It finds an invoice for a specific patient that is still in the DRAFT state.
    List<Invoice> findByPatientIdAndStatus(String patientId, InvoiceStatus status);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByPatientId(String patientId);
    Optional<Invoice> findByAppointmentId(String appointmentId);

}