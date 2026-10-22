package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExam;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExamRepository;
import com.example.diplomathesisfinalcollection.services.FileUploadService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/radiologist")
@RequiredArgsConstructor
public class RadiologistController {

    private final InstrumentalExamRepository instrumentalExamRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository; // 👈 Added this to find the doctor
    private final FileUploadService fileUploadService;

    // --- DTO to hold Exam + Names for the Dashboard ---
    @Data
    @AllArgsConstructor
    public static class ExamDashboardDto {
        private InstrumentalExam exam;
        private String patientName;
        private String doctorName;
    }

    @GetMapping("/dashboard")
    public String radiologistDashboard(Model model) {
        List<InstrumentalExam> pendingExams = instrumentalExamRepository.findByStatusOrderByOrderDateAsc("PENDING");

        // 🚀 Match each exam with its Patient and Doctor names
        List<ExamDashboardDto> enrichedExams = new ArrayList<>();
        for (InstrumentalExam exam : pendingExams) {
            User patient = userRepository.findById(exam.getPatientId()).orElse(null);
            Doctor doctor = doctorRepository.findById(exam.getOrderingDoctorId()).orElse(null);

            String patientName = (patient != null) ? patient.getFirstName() + " " + patient.getLastName() : "Unknown Patient";
            String doctorName = (doctor != null) ? "Dr. " + doctor.getFirstName() + " " + doctor.getLastName() : "Unknown Doctor";

            enrichedExams.add(new ExamDashboardDto(exam, patientName, doctorName));
        }

        model.addAttribute("pendingExams", enrichedExams);
        return "radiologist-dashboard";
    }

    @GetMapping("/exam/{examId}")
    public String viewExamDetails(@PathVariable String examId, Model model) {
        InstrumentalExam exam = instrumentalExamRepository.findById(examId).orElse(null);
        if (exam == null) {
            return "redirect:/radiologist/dashboard";
        }

        User patient = userRepository.findById(exam.getPatientId()).orElse(null);
        Doctor doctor = doctorRepository.findById(exam.getOrderingDoctorId()).orElse(null);

        model.addAttribute("exam", exam);
        model.addAttribute("patient", patient);
        model.addAttribute("doctor", doctor); // 👈 Pass doctor to details page too

        return "radiologist-exam-details";
    }

    @PostMapping("/exam/{examId}/complete")
    public String completeExam(@PathVariable String examId,
                               @RequestParam("findings") String findings,
                               @RequestParam("reportFile") MultipartFile reportFile,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        InstrumentalExam exam = instrumentalExamRepository.findById(examId).orElse(null);
        User radiologistUser = userRepository.findByEmail(principal.getName()).orElse(null);

        if (exam == null || radiologistUser == null || reportFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid data or missing file.");
            return "redirect:/radiologist/exam/" + examId;
        }

        try {
            // 🚀 UPLOAD TO THE CLOUD (ԱՆՎՏԱՆԳ)
            // fileUrl-ի փոխարեն ստանում ենք գաղտնի ID
            String secureFileId = fileUploadService.uploadSecureFile(reportFile);

            exam.setFindings(findings);
            // Բազայում պահում ենք ՄԻԱՅՆ ID-ն, օրինակ՝ "medical_exams/abc-123"
            exam.setReportFilePath(secureFileId);
            exam.setStatus("COMPLETED");
            exam.setRadiologistId(radiologistUser.getId());
            exam.setCompletionDate(LocalDateTime.now());
            instrumentalExamRepository.save(exam);

            redirectAttributes.addFlashAttribute("success", "Report submitted securely to the cloud!");
            return "redirect:/radiologist/dashboard";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Cloud upload failed! Please try again.");
            return "redirect:/radiologist/exam/" + examId;
        }
    }


}