package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.appointment.AppointmentStatusUpdater;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorSchedule;
import com.example.diplomathesisfinalcollection.doctor.DoctorScheduleRepository;
import com.example.diplomathesisfinalcollection.dto.AppointmentDTO;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.laboratory.Indicator;
import com.example.diplomathesisfinalcollection.laboratory.IndicatorRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequest;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequestRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.services.BillingService;
import com.example.diplomathesisfinalcollection.services.CloudinarySecurityService;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.services.ExcelImportService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.example.diplomathesisfinalcollection.dto.DoctorRegistrationDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest; // Համոզվիր որ այս import-ը կա վերևում
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; // Ավելացրու import-ների մեջ
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Ավելացրու վերևում

@Controller
@RequestMapping("/admin")
public class AdminReferralsController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ClinicServiceRepository clinicServiceRepository;


    // 1. Ցուցադրել Ուղեգրումների էջը
    @GetMapping("/referrals")
    public String showReferralsPage(Model model) {
        // Գտնում ենք բոլոր գրանցումները, որոնք բժիշկը ուղարկել է ադմինին
        List<Appointment> referrals = appointmentRepository.findAll().stream()
                .filter(a -> "REFERRED_TO_ADMIN".equals(a.getStatus()))
                .collect(Collectors.toList());

        // Քաշում ենք անհրաժեշտ տվյալներ աղյուսակի և Modal-ի համար
        List<User> patients = userRepository.findAll();
        List<Doctor> doctors = doctorRepository.findAll();
        List<Department> departments = departmentRepository.findAll();
        List<ClinicService> services = clinicServiceRepository.findAll();

        model.addAttribute("referrals", referrals);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departments);
        model.addAttribute("services", services);

        return "admin/referrals"; // Կբացի մեր նոր HTML ֆայլը
    }

    // 2. Մշակել Ուղեգրումը և ստեղծել նոր Appointment
    // 2. Մշակել Ուղեգրումը և ստեղծել նոր Appointment
    @PostMapping("/referrals/process")
    public String processReferral(
            @RequestParam String oldAppointmentId,
            @RequestParam String newDepartmentId,
            @RequestParam String newServiceId,
            @RequestParam String newDoctorId,
            @RequestParam String referralDate, // HTML-ից եկած օրը (օր. "2026-05-20")
            @RequestParam String referralTime, // HTML-ից եկած ժամը (օր. "14:30")
            RedirectAttributes redirectAttributes) {

        try {
            Appointment oldApp = appointmentRepository.findById(oldAppointmentId).orElse(null);
            if (oldApp == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Նախկին գրանցումը չի գտնվել:");
                return "redirect:/admin/referrals";
            }

            // Միացնում ենք օրը և ազատ ժամը՝ ստանալով ճշգրիտ LocalDateTime
            LocalDateTime scheduledDate = LocalDateTime.of(
                    LocalDate.parse(referralDate),
                    LocalTime.parse(referralTime)
            );

            // Ստեղծում ենք նոր գրանցումը
            Appointment newApp = Appointment.builder()
                    .patientId(oldApp.getPatientId())
                    .doctorId(newDoctorId)
                    .serviceId(newServiceId)
                    .date(scheduledDate)
                    .status("CONFIRMED") // Արդեն հաստատված է ադմինի կողմից
                    .referredFromId(oldApp.getReferredFromId())
                    .referralReason(oldApp.getReferralReason())
                    .isPaid(false) // Գումարը կգանձվի նոր այցի ավարտին
                    .build();

            appointmentRepository.save(newApp);

            // Հին գրանցումը նշում ենք որպես մշակված
            oldApp.setStatus("REFERRAL_PROCESSED");
            appointmentRepository.save(oldApp);

            // Ուղարկում ենք Email Պացիենտին
            User patient = userRepository.findById(oldApp.getPatientId()).orElse(null);
            Doctor newDoctor = doctorRepository.findById(newDoctorId).orElse(null);
            Department dept = departmentRepository.findById(newDepartmentId).orElse(null);

            if (patient != null && newDoctor != null && dept != null) {
                emailService.sendReferralNotificationEmail(patient, newDoctor, dept.getName(), scheduledDate);
            }

            redirectAttributes.addFlashAttribute("successMsg", "Պացիենտը հաջողությամբ ուղեգրվեց նոր բժշկի մոտ և ծանուցվեց էլ. փոստով:");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Սխալ տեղի ունեցավ. " + e.getMessage());
        }

        return "redirect:/admin/referrals";
    }

}
