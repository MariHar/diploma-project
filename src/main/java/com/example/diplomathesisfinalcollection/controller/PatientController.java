package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExam;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExamRepository;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.laboratory.LabTestRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrder;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrderRepository;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.services.FileUploadService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class PatientController {

    @Autowired private UserRepository userRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private EmailService emailService;
    @Autowired private LabTestRepository labTestRepository;// Ավելացրու սա վերևում
    @Autowired private PharmacyOrderRepository pharmacyOrderRepository;// Ավելացրու սա վերևում
    @Autowired private FileUploadService fileUploadService;// Ավելացրու սա վերևում
    @Autowired private InstrumentalExamRepository instrumentalExamRepository; // 🔴 Inject new repo
    @Autowired private InvoiceRepository invoiceRepository;


    @GetMapping("/profile")
    public String patientProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        if (patient == null) return "redirect:/login";

        if (patient.getUserProfile() != null && patient.getUserProfile().getDateOfBirth() != null) {
            int age = java.time.Period.between(patient.getUserProfile().getDateOfBirth(), java.time.LocalDate.now()).getYears();
            model.addAttribute("patientAge", age);
        } else {
            model.addAttribute("patientAge", "N/A");
        }

        List<Appointment> myAppointments = appointmentRepository.findByPatientIdOrderByDateAsc(patient.getId());
        List<InstrumentalExam> allExams = instrumentalExamRepository.findByPatientId(patient.getId());
        List<LabTest> allLabs = labTestRepository.findByPatientId(patient.getId());

        Map<String, String> invoiceStatuses = new HashMap<>();
        Map<String, String> invoiceIds = new HashMap<>();

        List<Invoice> allInvoices = invoiceRepository.findByPatientId(patient.getId());
        for (Invoice inv : allInvoices) {
            if (inv.getAppointmentId() != null) {
                invoiceStatuses.put(inv.getAppointmentId(), inv.getStatus().name());
                invoiceIds.put(inv.getAppointmentId(), inv.getId());
            }
        }

        Set<String> processedRecordIds = new HashSet<>();
        List<MedicalRecord> myRecords = new ArrayList<>();

        for (Appointment appt : myAppointments) {
            if (appt.getDoctorId() != null) {
                doctorRepository.findById(appt.getDoctorId()).ifPresent(doc ->
                        model.addAttribute("doctor_" + appt.getId(), doc));
            }

            invoiceRepository.findAll().stream()
                    .filter(inv -> appt.getId().equals(inv.getAppointmentId()))
                    .findFirst()
                    .ifPresent(inv -> {
                        invoiceStatuses.put(appt.getId(), inv.getStatus().toString());
                        invoiceIds.put(appt.getId(), inv.getId());
                    });

            medicalRecordRepository.findByAppointmentId(appt.getId()).ifPresent(rec -> {
                if (!processedRecordIds.contains(rec.getId())) {
                    myRecords.add(rec);
                    processedRecordIds.add(rec.getId());

                    if (rec.getDoctorId() != null) {
                        doctorRepository.findById(rec.getDoctorId()).ifPresent(doc ->
                                model.addAttribute("rec_doctor_" + rec.getId(), doc));
                    }
                    model.addAttribute("rec_date_" + rec.getId(), appt.getDate());
                }
            });
        }

        for (InstrumentalExam exam : allExams) {
            if (exam.getAppointmentId() != null) {
                appointmentRepository.findById(exam.getAppointmentId()).ifPresent(appt -> {
                    model.addAttribute("exam_date_" + exam.getId(), appt.getDate());
                    doctorRepository.findById(appt.getDoctorId()).ifPresent(doc ->
                            model.addAttribute("exam_doctor_" + exam.getId(), doc));
                });
            }
        }

        List<PharmacyOrder> orders = pharmacyOrderRepository.findByPatientIdOrderByOrderDateDesc(patient.getId());
        Map<String, String> orderStatuses = new HashMap<>();
        for (PharmacyOrder order : orders) {
            if (order.getMedicalRecordId() != null) orderStatuses.put(order.getMedicalRecordId(), order.getStatus());
        }
        List<Appointment> pendingReviews = appointmentRepository
                .findByPatientIdAndStatusAndIsRatedFalseAndIsSkippedFalse(patient.getId(), "COMPLETED");

        if (!pendingReviews.isEmpty()) {
            Appointment apptToRate = pendingReviews.get(0);
            model.addAttribute("pendingRating", apptToRate);

            long visitCount = appointmentRepository.countByPatientIdAndDoctorIdAndStatus(
                    patient.getId(), apptToRate.getDoctorId(), "COMPLETED");
            model.addAttribute("ratingVisitCount", visitCount);

            System.out.println("✅ Գտնվել է չգնահատված այց: ID = " + apptToRate.getId());
        } else {
            System.out.println("❌ Չգնահատված այց ՉԻ գտնվել:");
        }


        model.addAttribute("patient", patient);
        model.addAttribute("appointments", myAppointments);
        model.addAttribute("records", myRecords);
        model.addAttribute("labTests", allLabs);
        model.addAttribute("instrumentalExams", allExams);
        model.addAttribute("orderStatuses", orderStatuses);
        model.addAttribute("invoiceIds", invoiceIds);
        model.addAttribute("invoiceStatuses", invoiceStatuses);



        return "profile";
    }


    @GetMapping("/patient/ai-assistant")
    public String showAiChatPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Գտնում ենք պացիենտին և գցում մոդելի մեջ, որ HTML-ը ճանաչի user.id-ն
        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        if (patient != null) {
            model.addAttribute("user", patient);
        }

        return "chatbot";
    }
    @GetMapping("/patient/record/{recordId}/download")
    public ResponseEntity<ByteArrayResource> downloadRecordPdf(@PathVariable String recordId, Principal principal) {
        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId).orElse(null);
            User user = userRepository.findByEmail(principal.getName()).orElse(null);

            if (record == null || user == null || !record.getPatientId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Doctor doctor = doctorRepository.findById(record.getDoctorId()).orElse(null);
            List<LabTest> labTests = labTestRepository.findByAppointmentId(record.getAppointmentId());

            byte[] pdfBytes = emailService.generateMedicalRecordPdfBytes(record, user, doctor, labTests);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Medical_Report_" + recordId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/patient/record/{recordId}/view")
    public String viewMedicalRecord(@PathVariable String recordId, Model model, Principal principal) {
        MedicalRecord record = medicalRecordRepository.findById(recordId).orElse(null);
        User user = userRepository.findByEmail(principal.getName()).orElse(null);

        if (record == null || user == null || !record.getPatientId().equals(user.getId())) {
            return "redirect:/profile?error=access_denied";
        }

        Doctor doctor = doctorRepository.findById(record.getDoctorId()).orElse(null);
        List<LabTest> labTests = labTestRepository.findByAppointmentId(record.getAppointmentId());

        model.addAttribute("record", record);
        model.addAttribute("doctor", doctor);
        model.addAttribute("labTests", labTests);
        model.addAttribute("patient", user);

        return "patient/record-details";
    }

//    @GetMapping("/profile/edit")
//    public String editProfile(Model model, Principal principal, @RequestParam(required = false) String error) {
//        User user = userRepository.findByEmail(principal.getName()).orElse(null);
//        model.addAttribute("user", user);
//        if ("missing_delivery_info".equals(error)) {
//            model.addAttribute("errorMessage", "Դեղերի առաքման համար խնդրում ենք պարտադիր լրացնել ձեր հասցեն և հեռախոսահամարը:");
//        }
//        return "profile-edit";
//    }


    @PostMapping("/profile/edit/save")
    public String saveProfile(@ModelAttribute User updatedUser,
                              @RequestParam(value = "avatarFile", required = false) MultipartFile file,
                              Principal principal) {
        User existingUser = userRepository.findByEmail(principal.getName()).orElseThrow();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());

        if (updatedUser.getUserProfile() != null) {
            existingUser.getUserProfile().setGender(updatedUser.getUserProfile().getGender());
            existingUser.getUserProfile().setDateOfBirth(updatedUser.getUserProfile().getDateOfBirth());
        }

        if (file != null && !file.isEmpty()) {
            String imageUrl = fileUploadService.uploadImage(file);
            existingUser.setProfileImageUrl(imageUrl);
        }

        userRepository.save(existingUser);
        return "redirect:/profile?success=true";
    }



    @GetMapping("/patient/lab/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadLabPdf(@PathVariable String id, Principal principal) {
        try {
            LabTest lab = labTestRepository.findById(id).orElse(null);
            User user = userRepository.findByEmail(principal.getName()).orElse(null);

            if (lab == null || user == null || !lab.getPatientId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] pdfBytes = emailService.generateLabResultPdfBytes(lab, user);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Lab_Result_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/api/pending-review")
    @ResponseBody
    public ResponseEntity<?> getPendingReview(Principal principal) {
        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        if (patient == null) return ResponseEntity.status(401).build();

        List<Appointment> pendingReviews = appointmentRepository
                .findByPatientIdAndStatusAndIsRatedFalseAndIsSkippedFalse(patient.getId(), "COMPLETED");

        if (pendingReviews.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        Appointment apptToRate = pendingReviews.get(0);

        long visitCount = appointmentRepository.countByPatientIdAndDoctorIdAndStatus(
                patient.getId(), apptToRate.getDoctorId(), "COMPLETED");

        Map<String, Object> response = new HashMap<>();
        response.put("appointmentId", apptToRate.getId());
        response.put("doctorId", apptToRate.getDoctorId());
        response.put("visitCount", visitCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rate/skip/{appointmentId}")
    @ResponseBody
    public ResponseEntity<?> skipRating(@PathVariable String appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setSkipped(true);
            appointmentRepository.save(appt);
        }
        return ResponseEntity.ok().build();
    }



}