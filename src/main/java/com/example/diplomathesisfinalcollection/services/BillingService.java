package com.example.diplomathesisfinalcollection.services;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExam;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExamRepository;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.laboratory.LabTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final LabTestRepository labTestRepository;
    private final InstrumentalExamRepository instrumentalExamRepository;
    private final ClinicServiceRepository clinicServiceRepository;

    @Transactional
    public Invoice generateInstantInvoiceForPatient(String patientId, String appointmentId) {
        Invoice invoice = Invoice.builder()
                .patientId(patientId)
                .appointmentId(appointmentId)
                .status(InvoiceStatus.PENDING_PAYMENT)
                .issueDate(LocalDateTime.now())
                .lineItems(new ArrayList<>())
                .totalAmount(0.0)
                .build();

        // 1. ԲՈՒՆ ԱՅՑԸ (ԿՈՆՍՈՒԼՏԱՑԻԱ)
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment != null && appointment.getServiceId() != null) {
            ClinicService clinicService = clinicServiceRepository.findById(appointment.getServiceId()).orElse(null);
            if (clinicService != null) {
                invoice.getLineItems().add(createLineItem(
                        appointmentId,
                        "Կոնսուլտացիա: " + clinicService.getName(),
                        clinicService.getPrice()
                ));
            }
        }

        // 2. ԼԱԲՈՐԱՏՈՐ ԱՆԱԼԻԶՆԵՐԸ (Վերցնում ենք գինը ClinicService-ից)
        List<LabTest> labs = labTestRepository.findByAppointmentId(appointmentId);
        if (labs != null && !labs.isEmpty()) {
            for (LabTest lab : labs) {
                // Ենթադրվում է, որ LabTest-ը ունի getServiceId() կամ նման դաշտ, որը կապված է ClinicService-ի հետ
                if (lab.getServiceId() != null) {
                    ClinicService labService = clinicServiceRepository.findById(lab.getServiceId()).orElse(null);
                    if (labService != null) {
                        invoice.getLineItems().add(createLineItem(
                                lab.getId(),
                                "Լաբորատորիա: " + labService.getName(),
                                labService.getPrice()
                        ));
                    }
                }
            }
        }

        // 3. ԳՈՐԾԻՔԱՅԻՆ ՀԵՏԱԶՈՏՈՒԹՅՈՒՆՆԵՐԸ (Վերցնում ենք գինը ClinicService-ից)
        List<InstrumentalExam> exams = instrumentalExamRepository.findByAppointmentId(appointmentId);
        if (exams != null && !exams.isEmpty()) {
            for (InstrumentalExam exam : exams) {
                // Նույն տրամաբանությամբ ստուգում ենք նաև այստեղ
                if (exam.getServiceId() != null) {
                    ClinicService radService = clinicServiceRepository.findById(exam.getServiceId()).orElse(null);
                    if (radService != null) {
                        invoice.getLineItems().add(createLineItem(
                                exam.getId(),
                                "Ռադիոլոգիա: " + radService.getName(),
                                radService.getPrice()
                        ));
                    }
                }
            }
        }

        // 4. ՀԱՇՎԱՐԿ ԵՎ ՊԱՀՊԱՆՈՒՄ
        if (!invoice.getLineItems().isEmpty()) {
            double total = invoice.getLineItems().stream().mapToDouble(Invoice.LineItem::getUnitPrice).sum();
            invoice.setTotalAmount(total);
            return invoiceRepository.save(invoice);
        }

        return null;
    }

    private Invoice.LineItem createLineItem(String sourceId, String desc, Double price) {
        return Invoice.LineItem.builder().sourceId(sourceId).description(desc).quantity(1).unitPrice(price).build();
    }
}