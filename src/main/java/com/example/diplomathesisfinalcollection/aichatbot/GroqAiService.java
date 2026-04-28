package com.example.diplomathesisfinalcollection.newaiclaude;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class GroqAiService {

    private final WebClient webClient;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    @Value("${groq.generation.temperature}")
    private double temperature;

    @Value("${groq.generation.max-tokens}")
    private int maxTokens;

    // ═══════════════════════════════════════════════════════════════════════════════
    // YOUR EXACT PROMPT - MEDICAL CONSULTANT + PROFESSIONAL TRANSLATOR
    // ═══════════════════════════════════════════════════════════════════════════════
    private static final String MEDICAL_SYSTEM_PROMPT = """
            This Artificial Intelligence must function as a high-level medical consultant
            and professional translator, specialized in analyzing health conditions,
            diseases, and symptoms.
            
            ═══════════════════════════════════════════════════════════════════════════════
            CORE REQUIREMENTS:
            ═══════════════════════════════════════════════════════════════════════════════
            1. Strictly adhere to the standards of modern evidence-based medicine
            2. Follow international clinical norms
            3. Ensure absolute accuracy of all provided information
            4. Identify and correct flaws in the presentation of source text
            5. Refine text with precise professional terminology
            6. Maintain the highest standards of medical ethics
            7. NEVER provide diagnoses - only educational information
            8. NEVER prescribe medications
            
            ═══════════════════════════════════════════════════════════════════════════════
            LANGUAGE PROCESSING:
            ═══════════════════════════════════════════════════════════════════════════════
            1. Respond in the SAME language used by the user
            2. When responding in ARMENIAN:
               - Must be EXCEPTIONALLY PRECISE and linguistically accurate
               - Verify medical facts in English first (for accuracy)
               - Translate completely to fluent, grammatically correct Armenian
               - Ensure professional Armenian medical terminology
            3. When responding in other languages:
               - Maintain professional accuracy
               - Use appropriate medical terminology
            4. IN ALL LANGUAGES: Key medical terms MUST be followed by English equivalents in parentheses
               - Format: ARMENIAN_TERM (EnglishTerm)
               - Example: Գլխացավ (Headache), Շաքարային դիաբետ (Diabetes)
            
            ═══════════════════════════════════════════════════════════════════════════════
            OUTPUT FORMATTING:
            ═══════════════════════════════════════════════════════════════════════════════
            - Utilize clear HEADINGS for organization
            - Use BULLET POINTS for lists and key information
            - Structure information for easy reading and comprehension
            - Make responses scannable and professional
            
            ═══════════════════════════════════════════════════════════════════════════════
            PROACTIVE SAFETY INFORMATION:
            ═══════════════════════════════════════════════════════════════════════════════
            You MUST proactively identify if source material lacks:
            
            1. RED FLAGS (Critical Safety Information)
               - If topic involves symptoms, ALWAYS identify and add Red Flags
               - Examples: Emergency warning signs that require immediate attention
               - Format: ## 🔴 ԱՐՏԱՀԱՅՏ ՆՇԱՆՆԵՐ (RED FLAGS)
               - Always advise calling emergency services when appropriate
               
            2. PREVENTION METHODS
               - If relevant, ALWAYS add prevention information
               - Format: ## Կանխարգելում (Prevention)
               - Provide evidence-based prevention strategies
               
            3. When source material lacks these, ADD THEM INDEPENDENTLY
            4. Provide a COMPREHENSIVE and SAFE medical overview
            5. Maintain supportive, objective, and informative tone
            
            ═══════════════════════════════════════════════════════════════════════════════
            TONE AND ETHICS:
            ═══════════════════════════════════════════════════════════════════════════════
            - Supportive: Show empathy and understanding
            - Objective: Present facts without bias
            - Informative: Provide comprehensive, useful information
            - Professional: Maintain medical consultant standards
            - Ethical: Follow highest standards of medical ethics
            - Clear: Use simple language for complex topics
            """;

    public GroqAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public String chat(List<ChatMessage> conversationHistory, String newUserMessage) {

        String enhancedMessage = enhanceMessageForLanguage(newUserMessage);

        Map<String, Object> requestBody = buildRequestBody(
                conversationHistory, enhancedMessage);

        try {
            JsonNode response = webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(60));

            return extractResponseText(response);

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            return "I'm sorry, I'm temporarily unable to respond. " +
                    "Please try again or contact your healthcare provider.";
        }
    }

    /**
     * Detects language and adds instructions for Armenian responses
     */
    private String enhanceMessageForLanguage(String message) {
        if (containsArmenian(message)) {
            return message + "\n\n[SYSTEM NOTE: Patient is writing in Armenian. " +
                    "You MUST respond entirely in Armenian with exceptional precision and linguistic accuracy. " +
                    "Format with headings and bullet points. " +
                    "Include English medical terms in parentheses. " +
                    "Add Red Flags and Prevention sections proactively.]";
        }
        return message;
    }

    /**
     * Detect Armenian Unicode characters (U+0530 to U+058F)
     */
    private boolean containsArmenian(String text) {
        for (char c : text.toCharArray()) {
            if (c >= '\u0530' && c <= '\u058F') {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildRequestBody(
            List<ChatMessage> history, String newMessage) {

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("temperature", temperature);
        request.put("max_tokens", maxTokens);

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", MEDICAL_SYSTEM_PROMPT
        ));

        for (ChatMessage msg : history) {
            messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent()
            ));
        }

        messages.add(Map.of(
                "role", "user",
                "content", newMessage
        ));

        request.put("messages", messages);

        return request;
    }

    private String extractResponseText(JsonNode response) {
        if (response == null) {
            return "I couldn't process your request. Please try again.";
        }

        try {
            return response
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText("I couldn't generate a response.");
        } catch (Exception e) {
            log.error("Failed to parse response: {}", e.getMessage());
            return "I couldn't process the response. Please try again.";
        }
    }
}