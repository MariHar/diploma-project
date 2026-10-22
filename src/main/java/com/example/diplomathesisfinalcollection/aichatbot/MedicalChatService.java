package com.example.diplomathesisfinalcollection.aichatbot;

import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.laboratory.LabTestRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.example.diplomathesisfinalcollection.user.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalChatService {

    private final ConversationRepository conversationRepository;
    private final GeminiAiService geminiAiService;
    private final LabTestRepository labTestRepository;
    private final UserRepository userRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    private static final String DISCLAIMER = "⚕️ Այս տեղեկատվությունը տրամադրվում է միայն կրթական նպատակներով և չի կարող փոխարինել բժշկի պրոֆեսիոնալ խորհրդատվությանը:";

    public ChatResponse chat(ChatRequest request, String patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<LabTest> labs = labTestRepository.findByPatientId(patientId);
        String patientContext = buildPatientContext(patient, labs);

        String clinicSnapshot = buildClinicSnapshot();

        Conversation conversation;
        if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
            conversation = conversationRepository
                    .findByIdAndPatientId(request.getConversationId(), patientId)
                    .orElseGet(() -> createNewConversation(request, patientId));
        } else {
            conversation = createNewConversation(request, patientId);
        }

        conversation.getMessages().add(ChatMessage.builder()
                .role("user")
                .content(request.getMessage())
                .timestamp(LocalDateTime.now())
                .build());

        String aiReply;
        try {
            aiReply = geminiAiService.chat(
                    conversation.getMessages(),
                    request.getMessage(),
                    request.getImageBase64(),
                    patientContext,
                    clinicSnapshot
            );
        } catch (Exception e) {
            log.error("AI failure: ", e);
            aiReply = "⚠️ Տեխնիկական խնդիր: Փորձեք մի փոքր ուշ:";
        }

        conversation.getMessages().add(ChatMessage.builder()
                .role("assistant")
                .content(aiReply)
                .timestamp(LocalDateTime.now())
                .build());

        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conversation);

        return ChatResponse.builder()
                .conversationId(saved.getId())
                .reply(aiReply)
                .history(saved.getMessages())
                .disclaimer(DISCLAIMER)
                .build();
    }

    public String generateStatelessExplanation(String patientId, String prompt) {
        User patient = userRepository.findById(patientId).orElseThrow();
        String patientContext = buildPatientContext(patient, List.of());
        return geminiAiService.explainStateless(prompt, patientContext);
    }

    private String buildPatientContext(User user, List<LabTest> labs) {
        StringBuilder sb = new StringBuilder("PATIENT PROFILE:\n");
        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            sb.append("Age: ").append(profile.getAge()).append(", Gender: ").append(profile.getGender()).append("\n");
        }
        sb.append("LAB RESULTS:\n");
        for (LabTest lab : labs) {
            sb.append("- ").append(lab.getTestName()).append(": ").append(lab.getResults()).append("\n");
        }
        return sb.toString();
    }

    private String buildClinicSnapshot() {
        StringBuilder sb = new StringBuilder("CLINIC SNAPSHOT:\n");
        List<Department> departments = departmentRepository.findAll().stream().filter(Department::isActive).toList();
        List<Doctor> doctors = doctorRepository.findAll().stream().filter(Doctor::isActive).toList();
        List<ClinicService> services = clinicServiceRepository.findAll().stream().filter(ClinicService::isActive).toList();

        for (Department dept : departments) {
            sb.append("Department: ").append(dept.getName()).append("\n");
            doctors.stream().filter(d -> dept.getId().equals(d.getDepartmentId()))
                    .forEach(d -> sb.append("  - Doctor: ").append(d.getFirstName()).append(" ").append(d.getLastName()).append(" (").append(d.getSpecialization()).append(")\n"));
            services.stream().filter(s -> dept.getId().equals(s.getDepartmentId()))
                    .forEach(s -> sb.append("  * Service: ").append(s.getName()).append(" (Price: ").append(s.getPrice()).append(")\n"));
        }
        return sb.toString();
    }

    private Conversation createNewConversation(ChatRequest request, String patientId) {
        return Conversation.builder()
                .patientId(patientId)
                .title(request.getMessage().length() > 30 ? request.getMessage().substring(0, 30) + "..." : request.getMessage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public List<Conversation> getConversations(String patientId) {
        return conversationRepository.findByPatientIdOrderByUpdatedAtDesc(patientId);
    }

    public Conversation getConversation(String conversationId, String patientId) {
        return conversationRepository.findByIdAndPatientId(conversationId, patientId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }
}