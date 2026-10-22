package com.example.diplomathesisfinalcollection.aichatbot;


//import jakarta.validation.Valid;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // tighten in production
public class ChatController {


    private final UserRepository userRepository;
    private final MedicalChatService medicalChatService;

    @GetMapping("/patient/ai-assistant")
    public String showAiChatPage(@RequestParam(required = false) String convId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        if (patient != null) {
            model.addAttribute("user", patient);

            if (convId != null) {
                try {
                    Conversation conversation = medicalChatService.getConversation(convId, patient.getId());
                    model.addAttribute("conversation", conversation);
                } catch (Exception e) {
                }
            }
        }
        return "chatbot";
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("✅ [API] Ստացվեց նամակ անվտանգ պացիենտից (ID): " + patient.getId());

        ChatResponse response = medicalChatService.chat(request, patient.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{patientId}")
    public ResponseEntity<List<Conversation>> getChatHistory(@PathVariable String patientId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        if (!user.getId().equals(patientId)) {
            return ResponseEntity.status(403).build();
        }

        List<Conversation> history = medicalChatService.getConversations(patientId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/conversation/{conversationId}/{patientId}")
    public ResponseEntity<Conversation> getSingleConversation(
            @PathVariable String conversationId,
            @PathVariable String patientId) {
        Conversation conv = medicalChatService.getConversation(conversationId, patientId);
        return ResponseEntity.ok(conv);
    }
}