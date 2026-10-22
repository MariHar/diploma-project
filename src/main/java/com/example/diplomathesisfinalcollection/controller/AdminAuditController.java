package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.dto.FieldDiff;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.services.BlockchainService;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final MedicalRecordRepository medicalRecordRepository;
    private final BlockchainService blockchainService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

     @GetMapping("/list")
    public String showAuditList(Model model) {
         List<MedicalRecord> records = medicalRecordRepository.findAll();

        model.addAttribute("records", records);
        model.addAttribute("userRepo", userRepository);
        model.addAttribute("doctorRepo", doctorRepository);

        return "admin/audit-list";
    }

     @GetMapping("/report/{recordId}")
    public String runAuditReport(@PathVariable String recordId, Model model) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical Record not found"));

         String integrityStatus = blockchainService.verifyRecordIntegrity(recordId, record);

         List<FieldDiff> detailedDiffs = blockchainService.getDetailedDifferences(recordId, record);

         String patientName = userRepository.findById(record.getPatientId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");
        String doctorName = doctorRepository.findById(record.getDoctorId())
                .map(d -> "Dr. " + d.getFirstName() + " " + d.getLastName()).orElse("Unknown");

         model.addAttribute("record", record);
        model.addAttribute("patientName", patientName);
        model.addAttribute("doctorName", doctorName);
        model.addAttribute("integrityStatus", integrityStatus); // "VERIFIED", "TAMPERED", or "NO_BLOCKCHAIN_DATA"

         model.addAttribute("detailedDiffs", detailedDiffs);

        return "admin/audit-report";
    }
}