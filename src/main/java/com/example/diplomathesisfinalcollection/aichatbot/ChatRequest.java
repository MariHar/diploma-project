package com.example.diplomathesisfinalcollection.aichatbot;

//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Patient ID is required")

    private String conversationId;   // null = start new conversation

    @NotBlank(message = "Message cannot be empty")
    private String message;
    private String imageBase64;
}
