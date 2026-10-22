package com.example.diplomathesisfinalcollection.appointment;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class AppointmentStatusUpdater {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Ամեն օր կեսգիշերին (00:00) համակարգը թարմացնում է բոլոր այցերը
    @Scheduled(cron = "0 0 0 * * *")
    public void updateConfirmedToPending() {
        LocalDate today = LocalDate.now();
        List<Appointment> appointments = appointmentRepository.findAll();

        for (Appointment app : appointments) {
            // Եթե այցը CONFIRMED է և այն այսօր է կամ անցյալում
            if ("CONFIRMED".equals(app.getStatus()) &&
                    (app.getDate().toLocalDate().isEqual(today) || app.getDate().toLocalDate().isBefore(today))) {

                app.setStatus("PENDING");
                appointmentRepository.save(app);
            }
        }
        System.out.println("Ավտոմատ թարմացում. Այսօրվա այցերը դարձան PENDING");
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void sendUnpaidInvoiceReminders() {
        System.out.println("🔔 Ժամը 12:00 է. Սկսվում է չվճարված հաշիվների ստուգումը...");

        // Գտնում ենք բոլոր չվճարված հաշիվները
        List<Invoice> unpaidInvoices = invoiceRepository.findByPatientIdAndStatus(null, InvoiceStatus.PENDING_PAYMENT);
        // Նշում. Եթե վերևի մեթոդը չկա, կարող ես օգտագործել findAll() և ֆիլտրել ըստ STATUS-ի
        if (unpaidInvoices == null || unpaidInvoices.isEmpty()) {
            unpaidInvoices = invoiceRepository.findAll().stream()
                    .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING_PAYMENT)
                    .toList();
        }

        int count = 0;
        for (Invoice inv : unpaidInvoices) {
            User patient = userRepository.findById(inv.getPatientId()).orElse(null);

            if (patient != null && patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                // Կանչում ենք մեյլ ուղարկող փոքրիկ ֆունկցիան
                emailService.sendPaymentReminderSimpleEmail(
                        patient.getEmail(),
                        patient.getFirstName(),
                        inv.getTotalAmount()
                );
                count++;
            }
        }
        System.out.println("✅ Հիշեցումները ուղարկվեցին " + count + " պացիենտի:");
    }
}
