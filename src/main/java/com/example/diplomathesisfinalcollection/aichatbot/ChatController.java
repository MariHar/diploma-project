package com.example.diplomathesisfinalcollection.newaiclaude;


//import jakarta.validation.Valid;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // tighten in production
public class ChatControllerClaude {

    private final MedicalChatService chatService;

    /**
     * Send a message (creates new conversation if conversationId is null).
     *
     * POST /api/chat/send
     * {
     *   "patientId": "patient123",
     *   "conversationId": null,        ← null for new chat
     *   "message": "I have a headache"
     * }
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {

        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all conversations for a patient.
     * GET /api/chat/conversations/{patientId}
     */
    @GetMapping("/conversations/{patientId}")
    public ResponseEntity<List<Conversation>> getConversations(
            @PathVariable String patientId) {

        return ResponseEntity.ok(chatService.getConversations(patientId));
    }

    /**
     * Get a single conversation with full history.
     * GET /api/chat/conversations/{conversationId}/patient/{patientId}
     */
    @GetMapping("/conversations/{conversationId}/patient/{patientId}")
    public ResponseEntity<Conversation> getConversation(
            @PathVariable String conversationId,
            @PathVariable String patientId) {

        return ResponseEntity.ok(
                chatService.getConversation(conversationId, patientId));
    }

    /**
     * Delete a conversation.
     * DELETE /api/chat/conversations/{conversationId}/patient/{patientId}
     */
    @DeleteMapping("/conversations/{conversationId}/patient/{patientId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable String conversationId,
            @PathVariable String patientId) {

        chatService.deleteConversation(conversationId, patientId);
        return ResponseEntity.noContent().build();
    }
}