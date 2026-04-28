package com.example.diplomathesisfinalcollection.newaiclaude;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalChatService {

    private final ConversationRepository conversationRepository;
    private final GroqAiService groqAiService;   // ← CHANGED from GeminiAiService

    private static final String DISCLAIMER =
            "⚕️ This information is for educational purposes only and is not a substitute " +
                    "for professional medical advice. Please consult your healthcare provider.";

    public ChatResponse chat(ChatRequest request) {

        Conversation conversation;

        if (request.getConversationId() != null) {
            conversation = conversationRepository
                    .findByIdAndPatientId(request.getConversationId(), request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            conversation = Conversation.builder()
                    .patientId(request.getPatientId())
                    .title(generateTitle(request.getMessage()))
                    .build();
        }

        // ← CHANGED: calls Groq instead of Gemini
        String aiReply = groqAiService.chat(
                conversation.getMessages(),
                request.getMessage()
        );

        conversation.getMessages().add(ChatMessage.builder()
                .role("user")
                .content(request.getMessage())
                .timestamp(LocalDateTime.now())
                .build());

        conversation.getMessages().add(ChatMessage.builder()
                .role("assistant")
                .content(aiReply)
                .timestamp(LocalDateTime.now())
                .build());

        conversation.setUpdatedAt(LocalDateTime.now());

        Conversation saved = conversationRepository.save(conversation);

        log.info("Chat processed for patient {} | conversation {}",
                request.getPatientId(), saved.getId());

        return ChatResponse.builder()
                .conversationId(saved.getId())
                .reply(aiReply)
                .history(saved.getMessages())
                .disclaimer(DISCLAIMER)
                .build();
    }

    public List<Conversation> getConversations(String patientId) {
        return conversationRepository.findByPatientIdOrderByUpdatedAtDesc(patientId);
    }

    public Conversation getConversation(String conversationId, String patientId) {
        return conversationRepository
                .findByIdAndPatientId(conversationId, patientId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public void deleteConversation(String conversationId, String patientId) {
        Conversation conv = conversationRepository
                .findByIdAndPatientId(conversationId, patientId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversationRepository.delete(conv);
    }

    private String generateTitle(String firstMessage) {
        return firstMessage.length() > 50
                ? firstMessage.substring(0, 50) + "..."
                : firstMessage;
    }
}