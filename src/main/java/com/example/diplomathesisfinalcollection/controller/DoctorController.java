package com.example.diplomathesisfinalcollection.controller;


import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
//import com.example.diplomathesisfinalcollection.drugs.DrugInteractionService;
import com.example.diplomathesisfinalcollection.drugs.DrugInteractionService;
import com.example.diplomathesisfinalcollection.dto.CardHashDTO;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExam;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExamRepository;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.laboratory.LabTestRepository;
//import com.example.diplomathesisfinalcollection.blockchain.AiDiagnosticService;
//import com.example.diplomathesisfinalcollection.blockchain.AiVisualResponse;
//import com.example.diplomathesisfinalcollection.blockchain.BlockchainIntegrityService;
//import com.example.diplomathesisfinalcollection.blockchain.DiagnosisRequest;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequest;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequestRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
//import com.example.diplomathesisfinalcollection.patientai.AISafetyService;
//import com.example.diplomathesisfinalcollection.patientai.MedicalAIService;
import com.example.diplomathesisfinalcollection.services.BillingService;
import com.example.diplomathesisfinalcollection.services.BlockchainService;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder; // Add import

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private EditRequestRepository editRequestRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;
    @Autowired
    private DrugInteractionService drugInteractionService;
    @Autowired
    private InstrumentalExamRepository instrumentalExamRepository;
    @Autowired
    private BillingService billingService;


    @GetMapping
    public String listAllDoctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctors"; // We will create doctors.html later
    }

//    @GetMapping("/{id}")
//    public String viewDoctorProfile(@PathVariable String id, Model model) {
//        Doctor doctor = doctorRepository.findById(id).orElse(null);
//
//        if (doctor == null) {
//            return "redirect:/doctors";
//        }
//
//        Department department = null;
//        if (doctor.getDepartmentId() != null) {
//            department = departmentRepository.findById(doctor.getDepartmentId()).orElse(null);
//        }
//
//        model.addAttribute("doctor", doctor);
//        model.addAttribute("department", department);
//
//        return "doctor-details"; // We will create doctor-details.html later
//    }



    @GetMapping("/dashboard")
    public String doctorDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);

        List<AppointmentDto> appointmentDtos = new ArrayList<>();
        if (doctor != null) {
            List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId());

            for (Appointment appt : appointments) {
                User patient = userRepository.findById(appt.getPatientId()).orElse(null);
                AppointmentDto dto = new AppointmentDto();
                dto.setId(appt.getId());
                dto.setDate(appt.getDate());
                dto.setStatus(appt.getStatus());

                if (patient != null) {
                    dto.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                    dto.setPatientPhone(patient.getPhone());
                    dto.setPatientEmail(patient.getEmail());
                }

                appointmentDtos.add(dto);
            }
        }

        model.addAttribute("appointments", appointmentDtos);
        model.addAttribute("doctor", doctor);
        model.addAttribute("user", user);

        return "doctor-dashboard";
    }

    // --- PRIVATE: Doctor's Profile Page ---
    @GetMapping("/profile")
    public String doctorProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        Department department = null;

        if (doctor != null && doctor.getDepartmentId() != null) {
            department = departmentRepository.findById(doctor.getDepartmentId()).orElse(null);
        }

        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("department", department);

        return "doctor-profile";
    }

    // --- PRIVATE: Doctor's Appointments Page ---
    @GetMapping("/appointments")
    public String doctorAppointments(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        List<AppointmentDto> appointmentDtos = new ArrayList<>();

        if (doctor != null) {
            List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByDateAsc(doctor.getId());

            for (Appointment appt : appointments) {
                User patient = userRepository.findById(appt.getPatientId()).orElse(null);

                AppointmentDto dto = new AppointmentDto();
                dto.setId(appt.getId());
                dto.setDate(appt.getDate());
                dto.setStatus(appt.getStatus());

                if (patient != null) {
                    dto.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                    dto.setPatientPhone(patient.getPhone());
                    dto.setPatientEmail(patient.getEmail());
                }

                appointmentDtos.add(dto);
            }
        }

        model.addAttribute("appointments", appointmentDtos);

        return "doctor-appointments";
    }

    // Inner DTO class for passing appointment data to HTML
    @lombok.Data
    public static class AppointmentDto {
        private String id;
        private String patientName;
        private String patientPhone;
        private String patientEmail;
        private java.time.LocalDateTime date;
        private String status;
    }

    @GetMapping("/first-login-setup")
    public String firstLoginSetup(Principal principal) {
        if (principal == null) return "redirect:/login";
        return "doctor-first-login"; // Show the form
    }

    @PostMapping("/first-login-setup/save")
    public String saveNewPassword(@RequestParam String newPassword, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user != null) {
            // Update password and clear the flag
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setForcePasswordChange(false);
            userRepository.save(user);

//            if ("ROLE_LAB_WORKER".equals(user.getRole())) {
//                return "redirect:/lab/dashboard"; // Լաբորանտին՝ լաբորատորիա
//            }
        }
        // Redirect to their dashboard now that they are verified!
        return "redirect:/doctor/dashboard";
    }

//    @PostMapping("/appointment/complete/{id}")
//    public String completeAppointment(@PathVariable String id, Principal principal) {
//        // 1. Գտնում ենք, թե ով է լոգին եղած բժիշկը
//        User user = userRepository.findByEmail(principal.getName()).orElse(null);
//        Doctor loggedInDoctor = doctorRepository.findByUserId(user.getId()).orElse(null);
//
//        // 2. Գտնում ենք գրանցումը
//        Appointment appointment = appointmentRepository.findById(id).orElse(null);
//
//        // 3. ԱՄԵՆԱԿԱՐԵՎՈՐ ՍՏՈՒԳՈՒՄԸ. Արդյո՞ք այս գրանցումը պատկանում է այս բժշկին
//        if (appointment != null && loggedInDoctor != null && appointment.getDoctorId().equals(loggedInDoctor.getId())) {
//            appointment.setStatus("COMPLETED");
//            appointmentRepository.save(appointment);
//        }
//        return "redirect:/doctor/appointments";
//    }

    @PostMapping("/appointment/cancel/{id}")
    public String cancelAppointmentByDoctor(@PathVariable String id, Principal principal) {
        // 1. Գտնում ենք, թե ով է լոգին եղած օգտատերը
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        // 2. Գտնում ենք տվյալ օգտատիրոջ բժշկի պրոֆիլը
        Doctor loggedInDoctor = doctorRepository.findByUserId(user.getId()).orElse(null);

        // 3. Գտնում ենք բուն գրանցումը (Appointment) բազայից
        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        // 4. ԱՆՎՏԱՆԳՈՒԹՅԱՆ ՍՏՈՒԳՈՒՄ.
        // Արդյո՞ք գրանցումը գոյություն ունի, բժիշկը գոյություն ունի, և արդյո՞ք այս գրանցումը այս բժշկինն է:
        if (appointment != null && loggedInDoctor != null && appointment.getDoctorId().equals(loggedInDoctor.getId())) {

            // Եթե ստուգումն անցավ, նոր միայն փոխում ենք կարգավիճակը
            appointment.setStatus("CANCELLED");
            appointmentRepository.save(appointment);
        }

        // Վերադարձնում ենք բժշկի ամրագրումների էջ
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/appointment/{id}/record")
    public String showMedicalRecordForm(@PathVariable String id, Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        List<LabTest> labTests = labTestRepository.findByAppointmentId(appointment.getId());

        if (appointment == null || doctor == null || !appointment.getDoctorId().equals(doctor.getId())) {
            return "redirect:/doctor/appointments";
        }
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);
        if(record != null) {
            List<InstrumentalExam> instrumentalExams = instrumentalExamRepository.findByMedicalRecordId(record.getId());
            model.addAttribute("instrumentalExams", instrumentalExams);
        }

        User patient = userRepository.findById(appointment.getPatientId()).orElse(null);
        if (patient != null && patient.getUserProfile() != null && patient.getUserProfile().getDateOfBirth() != null) {
            int age = java.time.Period.between(patient.getUserProfile().getDateOfBirth(), java.time.LocalDate.now()).getYears();
            model.addAttribute("patientAge", age);

            String gender = patient.getUserProfile().getGender();
            if (gender != null && !gender.isEmpty()) {
                gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
            } else {
                gender = "N/A";
            }
            model.addAttribute("patientGender", gender);
        } else {
            model.addAttribute("patientAge", "N/A");
            model.addAttribute("patientGender", "N/A");
        }

        Department labDept = departmentRepository.findByNameContainingIgnoreCase("Laboratory").stream().findFirst()
                .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Լաբորատորիա").stream().findFirst()
                        .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Lab").stream().findFirst().orElse(null)));

        Department radDept = departmentRepository.findByNameContainingIgnoreCase("Radiology").stream().findFirst()
                .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Ռադիոլոգիա").stream().findFirst()
                        .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Գործիքային").stream().findFirst()
                                .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Rad").stream().findFirst().orElse(null))));

        List<ClinicService> labServices = new ArrayList<>();
        List<ClinicService> radServices = new ArrayList<>();

        System.out.println("\n=== ՍԿԶԲՆԱԿԱՆ ՔԱՐՏԻ ԷՋԻ ԲԵՌՆՈՒՄ ===");

        // 🔴 ՈՒՂՂՈՒՄ. Նախ քաշում ենք բազայից, նոր տպում ենք քանակը
        if (labDept != null) {
            labServices = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(labDept.getId());
            System.out.println("✅ Լաբորատորիա գտնվեց (ID: " + labDept.getId() + ") | Ծառայություններ՝ " + labServices.size());
        } else {
            System.out.println("❌ ԼԱԲՈՐԱՏՈՐԻԱ բաժանմունքը ՉԳՏՆՎԵՑ բազայում:");
        }

        if (radDept != null) {
            radServices = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(radDept.getId());
            System.out.println("✅ Ռադիոլոգիա գտնվեց (ID: " + radDept.getId() + ") | Ծառայություններ՝ " + radServices.size());
        } else {
            System.out.println("❌ ՌԱԴԻՈԼՈԳԻԱ բաժանմունքը ՉԳՏՆՎԵՑ բազայում:");
        }
        System.out.println("====================================\n");

        model.addAttribute("labServices", labServices);
        model.addAttribute("radServices", radServices);
        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", patient);
        model.addAttribute("record", record);
        model.addAttribute("labTests", labTests);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patientId", userRepository.findById(patient.getId()));
        model.addAttribute("labTests", labTests);

        return "doctor-medical-record";
    }

//    @PostMapping("/appointment/{id}/record/save")
//    public String saveMedicalRecord(
//            @PathVariable String id,
//            @RequestParam String symptoms,
//            @RequestParam String diagnosis,
//            @RequestParam String treatment,
//            @RequestParam(required = false) String contraindications,
//            @RequestParam(required = false) String notes,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate followUpDate,
//            Principal principal,
//            @RequestParam(value = "action", defaultValue = "draft") String action // 🔴 Կոճակի գործողությունը
//    ) {
//
//        User user = userRepository.findByEmail(principal.getName()).orElse(null);
//        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
//        Appointment appointment = appointmentRepository.findById(id).orElse(null);
//
//        if (appointment != null && doctor != null && appointment.getDoctorId().equals(doctor.getId())) {
//
//            User patient = userRepository.findById(appointment.getPatientId()).orElse(null);
//            MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);
//
//            // Ստուգում ենք խմբագրման իրավունքը
//            if (record != null && !canDoctorEdit(record)) {
//                return "redirect:/doctor/appointment/" + id + "/record/view?error=no_permission";
//            }
//
//            // Եթե քարտ չկա, ստեղծում ենք նորը
//            if (record == null) {
//                record = MedicalRecord.builder()
//                        .appointmentId(appointment.getId())
//                        .patientId(appointment.getPatientId())
//                        .doctorId(doctor.getId())
//                        .recordDate(LocalDateTime.now())
//                        .build();
//            }
//
//            // Լրացնում ենք դաշտերը (թե՛ draft, թե՛ complete դեպքում)
//            record.setSymptoms(symptoms);
//            record.setDiagnosis(diagnosis);
//            record.setTreatment(treatment);
//            record.setContraindications(contraindications);
//            record.setNotes(notes);
//            record.setFollowUpDate(followUpDate);
//            record.setAdminUnlocked(false);
//            record.setEditRequestReason(null);
//
//            // 🔴 ՏՐԱՄԱԲԱՆՈՒԹՅՈՒՆ ԸՍՏ ԿՈՃԱԿԻ
//            if ("draft".equals(action)) {
//                // 1. DRAFT ՌԵԺԻՄ
//                record.setStatus("DRAFT");
//                medicalRecordRepository.save(record);
//
//                // Draft-ի դեպքում մեյլ չենք ուղարկում և appointment-ը չենք փակում
//                return "redirect:/doctor/appointments?msg=draft_saved";
//
//            } else {
//                // 2. COMPLETE (ԱՎԱՐՏԵԼ ԵՎ ՈՒՂԱՐԿԵԼ) ՌԵԺԻՄ
//                record.setStatus("COMPLETED");
//                medicalRecordRepository.save(record);
//
//                // Աուդիտի մասը
//                EditRequest lastReq = editRequestRepository.findTopByRecordIdOrderByRequestDateDesc(record.getId());
//                if (lastReq != null && "APPROVED".equals(lastReq.getStatus())) {
//                    lastReq.setNewDiagnosis(diagnosis);
//                    lastReq.setNewTreatment(treatment);
//                    lastReq.setNewFollowUpDate(followUpDate);
//                    editRequestRepository.save(lastReq);
//                }
//
//                // Փակում ենք Appointment-ը
//                appointment.setStatus("COMPLETED");
//                appointmentRepository.save(appointment);
//
//                // ՄԵՅԼԻ ԵՎ PDF-Ի ՈՒՂԱՐԿՈՒՄԸ ՊԱՑԻԵՆՏԻՆ
//                if (patient != null) {
//                    String recipientEmail = patient.getEmail();
//                    if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
//
//                        // 🔴 Ավելացնում ենք Լաբորատոր արդյունքները
//                        // (Նախապես վերևում @Autowired արա LabTestRepository-ն, եթե դեռ չես արել)
//                        List<LabTest> labTests = labTestRepository.findByAppointmentId(id);
//
//                        // Ուղարկում ենք մեյլը՝ ներառելով անալիզները (կարդա ներքևի նշումը)
//                        emailService.sendMedicalRecordEmail(recipientEmail, record, patient, doctor, labTests);
//                    }
//                }
//
//                return "redirect:/doctor/appointments?completed=true";
//            }
//        }
//        return "redirect:/doctor/appointments?error=invalid_request";
//    }

//    @PostMapping("/appointment/{id}/record/save")
//    public String saveMedicalRecord(
//            @PathVariable String id,
//            @ModelAttribute MedicalRecord recordData,
//            @RequestParam(required = false) String symptoms,
//            @RequestParam(required = false) String complaints,
//            @RequestParam(required = false) String anamnesisMorbi,
//            @RequestParam(required = false) String anamnesisVitae,
//            @RequestParam(required = false) String inspection,
//            @RequestParam(required = false) String palpation,
//            @RequestParam(required = false) String percussion,
//            @RequestParam(required = false) String auscultation,
//            @RequestParam(required = false) String diagnosis,
//            @RequestParam(required = false) String treatment,
//            @RequestParam(required = false) String contraindications,
//            @RequestParam(required = false) String notes,
//            @RequestParam(required = false) String followUpDate,
//            @RequestParam(value = "action", defaultValue = "draft") String action,
//            @RequestParam(required = false) String requestedSpecialist,
//            @RequestParam(required = false) String referralReason,
//            Principal principal
//    ) {
//        try {
//            User user = userRepository.findByEmail(principal.getName()).orElse(null);
//            Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
//            Appointment appointment = appointmentRepository.findById(id).orElse(null);
//
//            MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);
//
//            // 🛡️ ԱԼԳՈՐԻԹՄ 2. ՍՏՈՒԳՈՒՄ ԵՆՔ ԽՄԲԱԳՐՄԱՆ ԻՐԱՎՈՒՆՔԸ
//            // Եթե քարտը արդեն կա, բայց խմբագրման ժամանակն անցել է կամ թույլտվություն չկա
//            if (record != null && !canDoctorEdit(record)) {
//                return "redirect:/doctor/appointments?error=access_denied_time_expired";
//            }
//
//            if (record == null) {
//                record = MedicalRecord.builder()
//                        .appointmentId(id).patientId(appointment.getPatientId())
//                        .doctorId(doctor.getId()).recordDate(LocalDateTime.now()).build();
//            }
//
//            // Տվյալների լրացում
//            record.setSymptoms(symptoms);
//            record.setComplaints(complaints);
//            record.setAnamnesisMorbi(anamnesisMorbi);
//            record.setAnamnesisVitae(anamnesisVitae);
//            record.setInspection(inspection);
//            record.setPalpation(palpation);
//            record.setPercussion(percussion);
//            record.setAuscultation(auscultation);
//            record.setDiagnosis(diagnosis);
//            record.setTreatment(treatment);
//            record.setContraindications(contraindications);
//            record.setNotes(notes);
//            record.setPrescriptions(recordData.getPrescriptions());
//
//            if (followUpDate != null && !followUpDate.trim().isEmpty()) {
//                record.setFollowUpDate(LocalDate.parse(followUpDate));
//            }
//
//            // REFERRAL SYSTEM
//            if ("refer_to_admin".equals(action)) {
//                record.setStatus("REFERRED");
//                record.setNotes(record.getNotes() + " | Referral for: " + requestedSpecialist + " | Reason: " + referralReason);
//                medicalRecordRepository.save(record);
//
//                appointment.setStatus("REFERRED");
//                appointmentRepository.save(appointment);
//
//                Appointment referralAppt = Appointment.builder()
//                        .patientId(appointment.getPatientId())
//                        .referredFromId(doctor.getId())
//                        .referredToDeptId(requestedSpecialist)
//                        .referralReason(referralReason)
//                        .date(LocalDateTime.now().plusDays(1))
//                        .status("PENDING")
//                        .isPaid(false)
//                        .build();
//                appointmentRepository.save(referralAppt);
//
//                return "redirect:/doctor/appointments?msg=referred";
//            }
//
//            // DRAFT OR COMPLETE
//            record.setStatus(action.toUpperCase());
//            medicalRecordRepository.save(record);
//
//            System.out.println("DEBUG: Action is -> " + action); // Ստուգիր՝ արդյո՞ք սա "complete" է
//            if ("complete".equals(action)) {
//                System.out.println("DEBUG: Entered COMPLETE block");
//                // BLOCKCHAIN AUDIT APPEND
//                blockchainIntegrityService.addSecureRecord(appointment.getPatientId(), symptoms + " | " + diagnosis + " | " + treatment);
//
//                appointment.setStatus("COMPLETED");
//                appointmentRepository.save(appointment);
//
//                // Փակում ենք Edit Request-ը
//                EditRequest activeReq = editRequestRepository.findTopByRecordIdOrderByRequestDateDesc(record.getId());
//                if (activeReq != null && "APPROVED".equals(activeReq.getStatus())) {
//                    activeReq.setStatus("COMPLETED_EDIT");
//                    activeReq.setNewDiagnosis(diagnosis);
//                    activeReq.setNewTreatment(treatment);
//                    editRequestRepository.save(activeReq);
//
//                    record.setAdminUnlocked(false);
//                    medicalRecordRepository.save(record);
//                }
//
//                // 📧 ԱԼԳՈՐԻԹՄ 1. ՈՒՂԱՐԿՈՒՄ ԵՆՔ ՄԵՅԼԸ ԱՆԱԼԻԶՆԵՐՈՎ
//                User patient = userRepository.findById(appointment.getPatientId()).orElse(null);
//                if (patient != null && patient.getEmail() != null) {
//                    // Քաշում ենք անալիզները, որպեսզի ուղարկենք մեյլի մեջ
//                    List<LabTest> labTests = labTestRepository.findByAppointmentId(id);
//                    emailService.sendMedicalRecordEmail(patient.getEmail(), record, patient, doctor, labTests);
//                }
//
//            }
//
//            return "redirect:/doctor/appointments?msg=" + action;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "redirect:/doctor/appointments?error=true";
//        }
//    }
@PostMapping("/appointment/{id}/record/save")
public String saveMedicalRecord(
        @PathVariable String id,
        @ModelAttribute MedicalRecord recordData,
        @RequestParam(required = false) String symptoms,
        @RequestParam(required = false) String diagnosis,
        @RequestParam(required = false) String treatment,
        @RequestParam(required = false) String contraindications,
        @RequestParam(required = false) String notes,
        @RequestParam(required = false) String followUpDateStr,
        @RequestParam(required = false) String followUpTime,
        @RequestParam(required = false) String complaints,
        @RequestParam(required = false) String anamnesisMorbi,
        @RequestParam(required = false) String anamnesisVitae,
        @RequestParam(required = false) String inspection,
        @RequestParam(required = false) String palpation,
        @RequestParam(required = false) String percussion,
        @RequestParam(required = false) String auscultation,
        @RequestParam(required = false) String requestedSpecialist,
        @RequestParam(required = false) String referralReason,
        Principal principal,
        @RequestParam(value = "action", defaultValue = "draft") String action
) {
    System.out.println("====== ՀԱՐՑՈՒՄԸ ՀԱՍԱՎ: Action = " + action + " ======");

    try {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        if (appointment == null || doctor == null) {
            return "redirect:/doctor/appointments?error=not_found";
        }

        MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);

        // 🛡️ 1. Ստուգում ենք խմբագրման իրավունքը
//        if (record != null && !canDoctorEdit(record)) {
//            System.out.println("⚠️ Խմբագրումն արգելված է (ժամանակն անցել է)");
//            return "redirect:/doctor/appointment/" + id + "/record/view?error=no_permission";
//        }

        if (record == null) {
            record = MedicalRecord.builder()
                    .appointmentId(id)
                    .patientId(appointment.getPatientId())
                    .doctorId(doctor.getId())
                    .recordDate(LocalDateTime.now())
                    .build();
        }

        // 2. Լրացնում ենք տվյալները
        record.setSymptoms(symptoms);
        record.setComplaints(complaints);
        record.setAnamnesisMorbi(anamnesisMorbi);
        record.setAnamnesisVitae(anamnesisVitae);
        record.setInspection(inspection);
        record.setPalpation(palpation);
        record.setPercussion(percussion);
        record.setAuscultation(auscultation);
        record.setDiagnosis(diagnosis);
        record.setTreatment(treatment);
        record.setContraindications(contraindications);
        record.setNotes(notes);
        record.setPrescriptions(recordData.getPrescriptions());

        // Որոշում ենք վերջնական ստատուսը
        boolean isComplete = action.equalsIgnoreCase("complete");
        if (record.getStatus() != null && record.getStatus().equals("COMPLETED")) {
            // Եթե քարտն արդեն ավարտված է եղել, այն չի կարող դառնալ DRAFT, անկախ կոճակից
            isComplete = true;
            record.setStatus("COMPLETED");
        } else {
            record.setStatus(isComplete ? "COMPLETED" : "DRAFT");
        }

        if ("refer_to_admin".equalsIgnoreCase(action)) {
            System.out.println("🔄 Պացիենտը ուղեգրվում է ադմինիստրատուրա...");

            // 1. Ափդեյթ ենք անում Appointment-ը հատուկ ստատուսով
            appointment.setStatus("REFERRED_TO_ADMIN");
            appointment.setReferredFromId(doctor.getId());
            appointment.setReferralReason(referralReason);

            // Պահում ենք թե որ մասնագետն է պետք (տեքստով)
            appointment.setReferredToDeptId(requestedSpecialist);

            appointmentRepository.save(appointment);

            // 2. Քարտը նույնպես պահպանում ենք, որ բժշկի մոտ կիսատ չմնա
            record.setStatus("REFERRED");
            medicalRecordRepository.save(record);

            System.out.println("✅ Ուղեգրումը հաջողությամբ ուղարկվեց ադմինին:");
            return "redirect:/doctor/appointments?msg=referred_successfully";
        }

        if (followUpDateStr != null) {
            if (followUpDateStr.trim().isEmpty()) {
                record.setFollowUpDate(null); // Ջնջում ենք հին ամսաթիվը բազայից
            } else {
                record.setFollowUpDate(LocalDate.parse(followUpDateStr));
            }
        }

        // 💾 3. ՊԱՀՊԱՆՈՒՄ ԵՆՔ ՔԱՐՏԸ (Առաջնային գործողություն)
        medicalRecordRepository.save(record);
        System.out.println("✅ Բժշկական քարտը պահպանվեց բազայում: Status: " + record.getStatus());

        // 4. Եթե ԱՎԱՐՏՎԱԾ է
        if (isComplete) {
            boolean isFirstTimeComplete = !"COMPLETED".equals(appointment.getStatus());
            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);
            System.out.println("✅ Appointment-ի ստատուսը փոխվեց COMPLETED:");
            Invoice generatedInvoice = null;
            if (isFirstTimeComplete) {
                // Առաջնային ստեղծում՝ գեներացնում ենք ինվոյս
                generatedInvoice = billingService.generateInstantInvoiceForPatient(appointment.getPatientId(), id);
                System.out.println("✅ Գեներացվեց նոր ինվոյս:");
            } else {
                System.out.println("⚠️ Բժշկական քարտը թարմացվել է. նոր ինվոյս ՉԻ գեներացվում:");
            }            // 🚀 SMART FOLLOW-UP BOOKING
            // If the doctor picked a date AND a time, create a confirmed appointment
            if (followUpDateStr != null && !followUpDateStr.trim().isEmpty() &&
                    followUpTime != null && !followUpTime.trim().isEmpty()) {

                LocalDateTime nextVisitDateTime = LocalDateTime.of(
                        LocalDate.parse(followUpDateStr),
                        LocalTime.parse(followUpTime)
                );

                Appointment nextAppointment = Appointment.builder()
                        .patientId(appointment.getPatientId())
                        .doctorId(doctor.getId())
                        .serviceId(appointment.getServiceId()) // Inherit the same service type
                        .date(nextVisitDateTime)
                        .status("CONFIRMED") // Because the doctor booked it directly
                        .isPaid(false)
                        .build();

                appointmentRepository.save(nextAppointment);
                System.out.println("✅ Հաջորդ այցը ավտոմատ ամրագրվեց: " + nextVisitDateTime);
            }
            // Փակում ենք Ադմինի տված թույլտվությունը (եթե կար)
            EditRequest activeReq = editRequestRepository.findTopByRecordIdOrderByRequestDateDesc(record.getId());
            if (activeReq != null && "APPROVED".equals(activeReq.getStatus())) {
                activeReq.setStatus("COMPLETED_EDIT");
                editRequestRepository.save(activeReq);
                record.setAdminUnlocked(false);
                medicalRecordRepository.save(record);
            }
            try {
                CardHashDTO hashDto = new CardHashDTO(
                        record.getComplaints(),
                        record.getAnamnesisMorbi(),
                        record.getAnamnesisVitae(),
                        record.getSymptoms(),
                        record.getDiagnosis(),
                        record.getTreatment(),
                        record.getContraindications(),
                        record.getNotes(),
                        record.getPrescriptions(),
//                        record.getVitals(),
                        record.getObjectiveExamination(),
                        record.getInspection(),
                        record.getPalpation(),
                        record.getPercussion(),
                        record.getAuscultation()
//                        record.getInstrumentalExams()
                );
                // Ուղարկում ենք Բլոկչեյն սերվիսին նոր բազայում գրանցելու համար
                blockchainService.addBlock(record.getId(), hashDto);
                System.out.println("🔗 Բլոկչեյնի նոր բլոկը հաջողությամբ ստեղծվեց ամբողջական կլինիկական տվյալներով:");
            } catch (Exception bcError) {
                System.out.println("❌ Սխալ բլոկչեյնի բլոկ ստեղծելիս: " + bcError.getMessage());
            }

            // 📧 5. ԷԼԵԿՏՐՈՆԱՅԻՆ ՆԱՄԱԿ (Առանձին try-catch-ով)
            try {
                User patient = userRepository.findById(appointment.getPatientId()).orElse(null);
                if (patient != null && patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                    System.out.println("📧 Փորձում ենք ուղարկել մեյլ: " + patient.getEmail());
                    List<LabTest> labTests = labTestRepository.findByAppointmentId(id);

                    // Ճյուղավորում ըստ գործողության բնույթի
                    if (isFirstTimeComplete) {
                        emailService.sendMedicalRecordEmail(patient.getEmail(), record, patient, doctor, labTests, generatedInvoice, appointment);
                    } else {
                        emailService.sendMedicalRecordUpdateEmail(patient.getEmail(), record, patient, doctor, labTests);
                    }

                    System.out.println("✅ Մեյլը հաջողությամբ ուղարկվեց:");
                } else {
                    System.out.println("⚠️ Մեյլ չուղարկվեց (Պատճառ: Պացիենտի էլ. հասցեն դատարկ է)");
                }
            } catch (Exception mailError) {
                System.out.println("❌ Սխալ նամակի ուղարկման ժամանակ: " + mailError.getMessage());
            }
        }

        return "redirect:/doctor/appointments?msg=" + action;

    } catch (Exception e) {
        System.out.println("❌ ԿՐԻՏԻԿԱԿԱՆ ՍԽԱԼ ՊԱՀՊԱՆԵԼԻՍ:");
        e.printStackTrace();
        return "redirect:/doctor/appointments?error=server_error";
    }
}

    private boolean canDoctorEdit(MedicalRecord record) {
        if (record == null) return true;

        if (record.getStatus() == null || "DRAFT".equals(record.getStatus())) {
            return true;
        }

        if ("COMPLETED".equals(record.getStatus()) && record.getRecordDate() != null) {
            LocalDateTime initialDeadline = record.getRecordDate().plusHours(48);
            if (LocalDateTime.now().isBefore(initialDeadline)) {
                return true;
            }
        }

        EditRequest activeReq = editRequestRepository.findTopByRecordIdOrderByRequestDateDesc(record.getId());
        if (activeReq != null && "APPROVED".equals(activeReq.getStatus()) && activeReq.getExpiryDate() != null) {
            if (LocalDateTime.now().isBefore(activeReq.getExpiryDate())) {
                return true;
            }
        }
        return false;
    }

//    private boolean isRecordEditable(MedicalRecord record) {
//        if (record == null) return false;
//
//        // Եթե ադմինը ձեռքով բացել է դոստուպը
//        if (record.isAdminUnlocked()) return true;
//
//        // Եթե դեռ 24 ժամ չի անցել ստեղծման պահից
//        return record.getRecordDate().isAfter(LocalDateTime.now().minusHours(24));
//    }
//    private boolean canDoctorEdit(MedicalRecord record) {
//        if (record == null) return false;
//
//        // 1. Եթե Ադմինը ձեռքով բացել է թույլտվությունը (Unlock)
//        if (record.isAdminUnlocked()) {
//            return true;
//        }
//
//        // 2. Եթե քարտը նոր է ստեղծվել և դեռ 24 ժամը չի անցել
//        LocalDateTime now = LocalDateTime.now();
//        if (record.getRecordDate() != null && record.getRecordDate().isAfter(now.minusHours(24))) {
//            return true;
//        }
//
//        // Մնացած բոլոր դեպքերում խմբագրումն արգելված է
//        return false;
//    }

    @GetMapping("/appointment/{id}/record/view")
    public String viewMedicalRecord(@PathVariable String id, Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor loggedInDoctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        if (appointment == null || loggedInDoctor == null) {
            return "redirect:/doctor/appointments";
        }

        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointment.getId()).orElse(null);
        User patient = userRepository.findById(appointment.getPatientId()).orElse(null);

        Doctor recordDoctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);

        boolean isRecordOwner = appointment.getDoctorId().equals(loggedInDoctor.getId());        // 🔴 ՆՈՐ ՄԱՍ. Գտնում ենք վերջին հարցումը, որպեսզի բժիշկը տեսնի ադմինի մերժումը
        if (record != null) {
            EditRequest lastRequest = editRequestRepository.findTopByRecordIdOrderByRequestDateDesc(record.getId());
            model.addAttribute("editRequest", lastRequest);
        }

        if (patient != null && patient.getUserProfile() != null && patient.getUserProfile().getDateOfBirth() != null) {
            int age = java.time.Period.between(patient.getUserProfile().getDateOfBirth(), java.time.LocalDate.now()).getYears();
            model.addAttribute("patientAge", age);

            // Capitalize the first letter of the gender for better UI
            String gender = patient.getUserProfile().getGender();
            if (gender != null && !gender.isEmpty()) {
                gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
            } else {
                gender = "N/A";
            }
            model.addAttribute("patientGender", gender);
        } else {
            model.addAttribute("patientAge", "N/A");
            model.addAttribute("patientGender", "N/A");
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", patient);
        model.addAttribute("record", record);
        model.addAttribute("recordDoctor", recordDoctor); // Հեղինակ բժիշկը
        model.addAttribute("isRecordOwner", isRecordOwner); // Արդյո՞ք սա իր քարտն է

        // 🔴 Օգտագործում ենք ՆՈՐ ԽԵԼԱՑԻ մեթոդը
        model.addAttribute("isEditable", isRecordOwner && canDoctorEdit(record));
        // 🛡️ Security & AI Insights (append-only)
//        try {
//            String patientId = appointment.getPatientId();
//            boolean chainVerified = blockchainIntegrityService.isChainValid(patientId);
//            model.addAttribute("chainVerified", chainVerified);
//
////            if (record != null) {
////                String clinicalData = String.valueOf(record.getSymptoms())
////                        + String.valueOf(record.getDiagnosis())
////                        + String.valueOf(record.getTreatment());
////
////                AiVisualResponse ai = aiDiagnosticService.analyzeDiagnosis(
////                        DiagnosisRequest.builder()
////                                .patientId(patientId)
////                                .clinicalData(clinicalData)
////                                .build()
////                );
////
////                // Attach latest blockchain hash for audit footer (non-blocking)
////                blockchainIntegrityService.getLastRecord(patientId)
////                        .map(r -> r.getCurrentHash())
////                        .ifPresent(ai::setBlockHash);
////
////                model.addAttribute("aiVisual", ai);
////            }
//        } catch (Exception ex) {
//            model.addAttribute("chainVerified", false);
//        }
        List<LabTest> labTests = labTestRepository.findByAppointmentId(id);
        model.addAttribute("labTests", labTests);

        // ☢️ ԳՈՐԾԻՔԱՅԻՆ ՀԵՏԱԶՈՏՈՒԹՅՈՒՆՆԵՐԻ ՍՏԱՑՈՒՄ
        List<InstrumentalExam> instrumentalExams = instrumentalExamRepository.findByAppointmentId(id);
        model.addAttribute("instrumentalExams", instrumentalExams);

        List<Department> allDepartments = departmentRepository.findAll();
        model.addAttribute("departments", allDepartments);
        return "doctor-medical-record-view";
    }

    // 🔴 ՆՈՐ. ՊԱՑԻԵՆՏԻ ՊԱՏՄՈՒԹՅԱՆ ԷՋԸ
    @GetMapping("/patient/{patientId}/history")
    public String viewPatientHistory(@PathVariable String patientId, Model model, Principal principal) {
        // 1. Ստուգում ենք, որ մտնողը հաստատ բժիշկ է
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);

        if (doctor == null) {
            return "redirect:/login";
        }

        // 2. Գտնում ենք պացիենտին և նրա բոլոր նախկին քարտերը (նորից հին դասավորվածությամբ)
        User patient = userRepository.findById(patientId).orElse(null);
        List<MedicalRecord> history = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);

        model.addAttribute("patient", patient);
        model.addAttribute("history", history);

        return "doctor-patient-history"; // Այս էջը կստեղծենք հաջորդիվ
    }

//    private boolean canDoctorEdit(MedicalRecord record) {
//        if (record == null) return true;
//
//        // 🔴 ՈՒՂՂՈՒՄ. Եթե ստատուսը դեռ չկա (null) կամ սևագիր է, միշտ կարող է խմբագրել
//        if (record.getStatus() == null || "DRAFT".equals(record.getStatus())) {
//            return true;
//        }
//
//        // ԿԱՆՈՆ Ա. Ավարտված քարտերի համար ստուգում ենք առաջնային 24 ժամը
//        if ("COMPLETED".equals(record.getStatus()) && record.getRecordDate() != null) {
//            LocalDateTime initialDeadline = record.getRecordDate().plusHours(48);
//            if (LocalDateTime.now().isBefore(initialDeadline)) {
//                return true;
//            }
//        }
//
//        // ԿԱՆՈՆ Բ. Ստուգում ենք Ադմինի տված ժամանակը
//        EditRequest activeReq = editRequestRepository.findTopByRecordIdOrderByRequestDateDesc(record.getId());
//        if (activeReq != null && "APPROVED".equals(activeReq.getStatus()) && activeReq.getExpiryDate() != null) {
//            if (LocalDateTime.now().isBefore(activeReq.getExpiryDate())) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    @PostMapping("/appointment/{id}/record/request-edit")
    public String requestEdit(@PathVariable String id, @RequestParam String reason, Principal principal) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);

        if (record != null && doctor != null) {
            EditRequest editReq = EditRequest.builder()
                    .recordId(record.getId())
                    .doctorId(doctor.getId())
                    .doctorName("Dr. " + doctor.getFirstName() + " " + doctor.getLastName())
                    .requestReason(reason)
                    .requestDate(LocalDateTime.now())
                    .status("PENDING")
                    .originalDiagnosis(record.getDiagnosis())
                    .originalTreatment(record.getTreatment())
                    .originalFollowUpDate(record.getFollowUpDate())
                    .build();

            editRequestRepository.save(editReq);

            record.setEditRequestReason(reason);
            medicalRecordRepository.save(record);
        }
        return "redirect:/doctor/appointment/" + id + "/record/view?requested=true";
    }

    @GetMapping("/appointment/{id}/record/edit")
    public String showEditForm(@PathVariable String id, Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);

        // 🔴 ՈՒՂՂՎԱԾ Է. Հիմա որոնում ենք բժշկի սեփական ID-ով (user.getId())
        Doctor loggedInDoctor = doctorRepository.findByUserId(user.getId()).orElse(null);

        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        if (appointment == null || loggedInDoctor == null) {
            System.out.println("❌ ԽՄԲԱԳՐՄԱՆ ՁԱԽՈՂՈՒՄ: Appointment կամ Բժիշկ չի գտնվել:");
            return "redirect:/doctor/appointments";
        }

        // 2. 🛡️ ԱՆՎՏԱՆԳՈՒԹՅՈՒՆ. Ստուգում ենք սեփականատիրոջը
        if (!appointment.getDoctorId().equals(loggedInDoctor.getId())) {
            System.out.println("❌ ԽՄԲԱԳՐՄԱՆ ՁԱԽՈՂՈՒՄ: Փորձում է խմբագրել ուրիշի քարտ:");
            return "redirect:/doctor/appointment/" + id + "/record/view?error=not_owner";
        }

        // 🔴 ՈՒՂՂՎԱԾ Է. Patient-ին վերցնում ենք appointment-ից, ապահով ձևով
        User patient = userRepository.findById(appointment.getPatientId()).orElse(null);
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);

        // 3. Ստուգում ենք քարտի առկայությունը
        if (record == null) {
            System.out.println("❌ ԽՄԲԱԳՐՄԱՆ ՁԱԽՈՂՈՒՄ: Քարտը դեռ ստեղծված չէ (record == null):");
            // Եթե քարտը դեռ չկա, տանում ենք view էջ, որտեղից կսկսի լրացնել
            return "redirect:/doctor/appointment/" + id + "/record/view";
        }

        // 4. Ստուգում ենք ժամանակի սահմանափակումը (canDoctorEdit)
        if (!canDoctorEdit(record)) {
            System.out.println("❌ ԽՄԲԱԳՐՄԱՆ ՁԱԽՈՂՈՒՄ: Խմբագրման ժամանակը սպառվել է կամ թույլտվություն չկա:");
            return "redirect:/doctor/appointment/" + id + "/record/view?error=locked";
        }

        // 1. Գտնում ենք բաժանմունքները անունով (անունները պետք է ճիշտ համապատասխանեն բազայում գրվածին)
        // 1. Խելացի որոնում (Smart Search) բաժանմունքների համար
        Department labDept = departmentRepository.findByNameContainingIgnoreCase("Laboratory").stream().findFirst()
                .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Լաբորատորիա").stream().findFirst()
                        .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Lab").stream().findFirst().orElse(null)));

        Department radDept = departmentRepository.findByNameContainingIgnoreCase("Radiology").stream().findFirst()
                .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Ռադիոլոգիա").stream().findFirst()
                        .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Գործիքային").stream().findFirst()
                                .orElseGet(() -> departmentRepository.findByNameContainingIgnoreCase("Rad").stream().findFirst().orElse(null))));
        // 2. Ստեղծում ենք դատարկ ցուցակներ որպես նախնական արժեք
        List<ClinicService> labServices = new ArrayList<>();
        List<ClinicService> radServices = new ArrayList<>();

        // 3. Եթե բաժանմունքները գտնվել են, քաշում ենք դրանց ծառայությունները բազայից
        if (labDept != null) {
            labServices = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(labDept.getId());
        }
        if (radDept != null) {
            radServices = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(radDept.getId());
        }

        // 4. Փոխանցում ենք HTML մոդելին
        model.addAttribute("labServices", labServices);
        model.addAttribute("radServices", radServices);
        model.addAttribute("record", record);
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment);

        return "doctor-medical-record";
    }

    @GetMapping("/appointment/{id}/record/print")
    public String printMedicalRecord(@PathVariable String id, Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        // Անվտանգության ստուգում
        if (appointment == null || doctor == null || !appointment.getDoctorId().equals(doctor.getId())) {
            return "redirect:/doctor/appointments";
        }

        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointment.getId()).orElse(null);
        User patient = userRepository.findById(appointment.getPatientId()).orElse(null);

        if (record == null) {
            return "redirect:/doctor/appointments"; // Եթե քարտը դեռ լրացված չէ, չենք կարող տպել
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", patient);
        model.addAttribute("doctor", doctor); // Բժշկի տվյալներն էլ ենք ուղարկում կնիքի համար
        model.addAttribute("record", record);

        return "doctor-medical-record-print"; // Այս ֆայլը կստեղծենք հաջորդիվ
    }

    @PostMapping("/appointment/{id}/order-test")
    public String orderTest(@PathVariable String id, @RequestParam String testName, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        User doctorUser = userRepository.findByEmail(principal.getName()).orElse(null);

        if (appointment != null) {
            LabTest newTest = LabTest.builder()
                    .appointmentId(id)
                    .patientId(appointment.getPatientId())
                    .doctorId(doctorUser.getId())
                    .testName(testName)
                    .status("PENDING")
                    .orderDate(LocalDateTime.now())
                    .build();

            labTestRepository.save(newTest);
        }
        return "redirect:/doctor/appointment/" + id + "/record/view?success=test_ordered";
    }

    @PostMapping("/appointment/{id}/order-tests")
    @ResponseBody
    public ResponseEntity<?> orderMultipleTests(@PathVariable String id,
                                                @RequestBody List<String> testIds,
                                                Principal principal) {

        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        User doctorUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctorProfile = doctorRepository.findByUserId(doctorUser.getId()).orElse(null);

        if (appointment == null || doctorProfile == null) {
            return ResponseEntity.badRequest().build();
        }

        // Loop over the incoming IDs
        for (String currentTestId : testIds) {

            // 1. Fetch the service using currentTestId, not the appointment 'id'
            // Note: If your ClinicService ID in DB is a Long, you might need to do: Long.parseLong(currentTestId)
            ClinicService service = clinicServiceRepository.findById(currentTestId).orElse(null);

            // 2. Get the real name from the service
            String realTestName = (service != null) ? service.getName() : "Անհայտ անալիզ";

            LabTest newTest = LabTest.builder()
                    .appointmentId(id)
                    .patientId(appointment.getPatientId())
                    .doctorId(doctorProfile.getId())
                    .serviceId(currentTestId) // Save the correct Service ID
                    .testName(realTestName)   // Save the real Name!
                    .status("PENDING")
                    .orderDate(LocalDateTime.now())
                    .build();

            labTestRepository.save(newTest);
        }

        return ResponseEntity.ok().build();
    }

//    @PostMapping("/appointment/{id}/refer")
//    public String referToDepartment(@PathVariable String id,
//                                    @RequestParam String targetDeptId,
//                                    @RequestParam String reason,
//                                    Principal principal) {
//        Appointment currentAppt = appointmentRepository.findById(id).orElse(null);
//        if (currentAppt != null) {
//            currentAppt.setStatus("REFERRED");
//            appointmentRepository.save(currentAppt);
//
//            // Ստեղծում ենք նոր Appointment նոր բաժանմունքի համար
//            Appointment referral = Appointment.builder()
//                    .patientId(currentAppt.getPatientId())
//                    .referredFromId(currentAppt.getDoctorId())
//                    .referredToDeptId(targetDeptId)
//                    .referralReason(reason)
//                    .date(LocalDateTime.now().plusDays(1)) // Օրինակ՝ վաղվա համար
//                    .status("PENDING")
//                    .build();
//            appointmentRepository.save(referral);
//        }
//        return "redirect:/doctor/appointments?msg=referred_successfully";
//    }


//    @PostMapping("/api/drugs/check")
//    @ResponseBody
//    public ResponseEntity<?> checkDrugInteractions(@RequestBody List<String> currentDrugs) {
//        String alertMessage = drugInteractionService.checkInteractions(currentDrugs);
//        return ResponseEntity.ok(java.util.Map.of("alert", alertMessage));
//    }

    // --- 1.2 ՈՒՂԵԳՐՄԱՆ (REFERRAL) API ---
    @PostMapping("/appointment/{id}/refer")
    public String referPatient(@PathVariable String id,
                               @RequestParam String targetDeptId,
                               @RequestParam String reason,
                               Principal principal) {
        Appointment currentAppt = appointmentRepository.findById(id).orElse(null);
        if (currentAppt != null) {
            currentAppt.setStatus("REFERRED");
            appointmentRepository.save(currentAppt);

            Appointment referral = Appointment.builder()
                    .patientId(currentAppt.getPatientId())
                    .referredFromId(currentAppt.getDoctorId())
                    .referredToDeptId(targetDeptId)
                    .referralReason(reason)
                    .date(LocalDateTime.now().plusDays(1))
                    .status("PENDING")
                    .build();
            appointmentRepository.save(referral);
        }
        return "redirect:/doctor/appointments?msg=referred_successfully";
    }

//    @PostMapping("/api/drugs/check-ai")
//    @ResponseBody
//    public ResponseEntity<?> checkDrugsWithAI(@RequestBody List<String> drugs) {
//        // Եթե դեղերի քանակը 2-ից քիչ է, ստուգելու կարիք չկա
//        if (drugs == null || drugs.size() < 2) {
//            return ResponseEntity.ok(Map.of("alert", "OK"));
//        }
//
//        // Կազմում ենք AI պրոմպտը
//        String drugList = String.join(", ", drugs);
//        String prompt = "Որպես պրոֆեսիոնալ կլինիկական ֆարմակոլոգ, ստուգիր հետևյալ դեղերի անհամատեղելիությունը կամ վտանգավոր փոխազդեցությունը՝ "
//                + drugList
//                + "։ Պատասխանիր հակիրճ (առավելագույնը 2 նախադասություն) հայերենով: Եթե վտանգավոր է, սկսիր ՊԱՏԱՍԽԱՆԸ '⚠️ ՎՏԱՆԳ' բառով: Եթե անվտանգ է, պատասխանիր միայն 'OK':";
//
//        try {
//            // Կանչում ենք մեր նոր սարքած մեթոդը Spring AI-ի միջոցով
//            String aiResponse = aiDiagnosticService.askGpt(prompt);
//
//            return ResponseEntity.ok(Map.of("alert", aiResponse.trim()));
//        } catch (Exception e) {
//            System.err.println("AI Error: " + e.getMessage());
//            return ResponseEntity.ok(Map.of("alert", "⚠️ AI համակարգը ժամանակավորապես անհասանելի է:"));
//        }
//    }
        @PostMapping("/api/drugs/check")
        @ResponseBody
        public ResponseEntity<?> checkDrugInteractionsFast(@RequestBody List<String> currentDrugs) {
            String alertMessage = drugInteractionService.checkInteractions(currentDrugs);
            return ResponseEntity.ok(Map.of("alert", alertMessage));
        }

    @PostMapping("/appointment/{appointmentId}/order-exam")
    @ResponseBody // 👈 Tells Spring to treat this as an AJAX response, not a page redirect
    public ResponseEntity<?> orderInstrumentalExam(@PathVariable String appointmentId, // 👈 FIXED variable name
                                                   @RequestParam String serviceId, // 👈 Ավելացրու սա
                                                   @RequestParam String examType,
                                                   @RequestParam String areaToExamine,
                                                   @RequestParam String clinicalReason,
                                                   Principal principal) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        User doctorUser = userRepository.findByEmail(principal.getName()).orElse(null);
        Doctor doctor = doctorRepository.findByUserId(doctorUser.getId()).orElse(null);

        if (appointment == null || doctor == null) {
            return ResponseEntity.badRequest().body("Error: Missing appointment or doctor.");
        }

        // Ensure a medical record exists to attach the exam to. If not, auto-create a DRAFT.
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId).orElse(null);
        if (record == null) {
            record = MedicalRecord.builder()
                    .appointmentId(appointmentId)
                    .patientId(appointment.getPatientId())
                    .doctorId(doctor.getId())
                    .recordDate(LocalDateTime.now())
                    .status("DRAFT")
                    .build();
            record = medicalRecordRepository.save(record);
        }

        InstrumentalExam newExam = InstrumentalExam.builder()
                .patientId(appointment.getPatientId())
                .orderingDoctorId(doctor.getId())
                .appointmentId(appointmentId)
                .medicalRecordId(record.getId()) // Guaranteed to exist now
                .serviceId(serviceId) // 👈 Պահպանում ենք ID-ն բիլինգի համար
                .examType(examType)
                .areaToExamine(areaToExamine)
                .clinicalReason(clinicalReason)
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        instrumentalExamRepository.save(newExam);

        System.out.println("✅ SUCCESS: Instrumental Exam Saved to Database!");

        return ResponseEntity.ok().build();
    }




}
