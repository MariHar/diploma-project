package com.example.diplomathesisfinalcollection.controller;


import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping("/prescription/{id}")
    public String verifyPrescription(@PathVariable String id, Model model) {
        // Փնտրում ենք քարտը
        MedicalRecord record = medicalRecordRepository.findById(id).orElse(null);

        if (record != null) {
            // Եթե գտանք, վերցնում ենք նաև պացիենտի և բժշկի տվյալները
            User patient = userRepository.findById(record.getPatientId()).orElse(null);
            Doctor doctor = doctorRepository.findById(record.getDoctorId()).orElse(null);

            model.addAttribute("record", record);
            model.addAttribute("patient", patient);
            model.addAttribute("doctor", doctor);
        }

        // Կանչում ենք HTML էջը (նույնիսկ եթե record-ը null է, HTML-ը ցույց կտա սխալ)
        return "verify-prescription";
    }
}
