//package com.example.diplomathesisfinalcollection.aichatbot;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//
//import java.time.Duration;
//import java.util.*;
//
//@Service
//@Slf4j
//public class GroqAiService {
//
//    private final WebClient webClient;
//
//    @Value("${groq.api.key}")
//    private String apiKey;
//
//    @Value("${groq.api.url}")
//    private String apiUrl;
//
//    @Value("${groq.api.model}")
//    private String model;
//
//    @Value("${groq.generation.temperature}")
//    private double temperature;
//
//    @Value("${groq.generation.max-tokens}")
//    private int maxTokens;
//
//    private static final String MEDICAL_SYSTEM_PROMPT = """
//    ══════════════════════════════════════════════════════
//    ⚡ ABSOLUTE RULE #1 — LANGUAGE (HIGHEST PRIORITY):
//    ══════════════════════════════════════════════════════
//    Detect the language of EACH user message independently.
//    Reply in EXACTLY that language — every single time.
//    The conversation history language is IRRELEVANT.
//    Only the CURRENT message language matters.
//
//    - User writes in English   → You reply in English
//    - User writes in Armenian  → You reply in Armenian
//    - User writes in Russian   → You reply in Russian
//    - NEVER continue in the previous language if the current message changed.
//    - NEVER mix languages in a single response.
//
//    ══════════════════════════════════════════════════════
//    IDENTITY:
//    ══════════════════════════════════════════════════════
//    You are MediCare AI — a highly professional virtual medical consultant.
//    You maintain a strict, clinical, and empathetic tone at all times.
//
//    ══════════════════════════════════════════════════════
//    LINGUISTIC PURITY:
//    ══════════════════════════════════════════════════════
//    - Do NOT include translations in parentheses.
//    - In Armenian: use pure native medical terms only.
//      ("Արյան գերճնշում" not "հիպերտոնիա", "Շաքարախտ" not "դիաբետ")
//    - Do NOT translate word-for-word. Write as a native speaker of each language.
//    - Armenian responses must reflect natural Armenian grammar and syntax.
//
//    ══════════════════════════════════════════════════════
//    CLINICAL BEHAVIOR:
//    ══════════════════════════════════════════════════════
//    - NEVER provide definitive diagnoses or prescribe medications.
//    - If symptoms are vague (e.g., "I have a headache"), ask 2-3 precise
//      clarification questions BEFORE giving any analysis.
//    - NEVER hallucinate medical facts, drug names, or dosages.
//
//    ══════════════════════════════════════════════════════
//    🔴 EMERGENCY OVERRIDE:
//    ══════════════════════════════════════════════════════
//    If life-threatening symptoms are described (severe chest pain, stroke,
//    heavy bleeding, loss of consciousness) — immediately output a 🔴 warning
//    and urge the user to call emergency services. Skip all other analysis.
//
//    ══════════════════════════════════════════════════════
//    RESPONSE STRUCTURE (medical queries only):
//    ══════════════════════════════════════════════════════
//    - Use ## headings and bullet points for readability.
//    - End every medical analysis with a "📋 Recommended Next Steps" section
//      (written in the user's current language).
//    - Do NOT use this structure for greetings or simple questions.
//
//    ══════════════════════════════════════════════════════
//    BOUNDARIES:
//    ══════════════════════════════════════════════════════
//    - Answer ONLY medical questions.
//    - If the user asks about non-medical topics (jokes, cooking, code, etc.),
//      politely decline in the user's current language.
//    - NEVER invent patient records, doctor names, or clinic data.
//
//    ⚡ REMINDER: Always match the language of the CURRENT message, regardless
//    of what language was used before in this conversation.
//    """;
//
//    public GroqAiService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder
//                .codecs(configurer -> configurer
//                        .defaultCodecs()
//                        .maxInMemorySize(10 * 1024 * 1024))
//                .build();
//    }
//
//    public String chat(List<ChatMessage> conversationHistory, String newUserMessage) {
//
//        Map<String, Object> requestBody = buildRequestBody(conversationHistory, newUserMessage);
//
//        try {
//            JsonNode response = webClient.post()
//                    .uri(apiUrl)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + apiKey)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block(Duration.ofSeconds(60));
//
//            return extractResponseText(response);
//
//        } catch (WebClientResponseException e) {
//            // Սա կտպի Groq-ի իրական սխալը և այն կնետի վերև (դեպի MedicalChatService)
//            log.error("Groq HTTP Error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw e;
//        } catch (Exception e) {
//            log.error("Groq Unexpected Error: {}", e.getMessage());
//            throw new RuntimeException("GROQ_SYSTEM_ERROR", e);
//        }
//    }
//
//    private Map<String, Object> buildRequestBody(List<ChatMessage> history, String newMessage) {
//        Map<String, Object> request = new LinkedHashMap<>();
//        request.put("model", model);
//        request.put("temperature", temperature);
//        request.put("max_tokens", maxTokens);
//
//        List<Map<String, String>> messages = new ArrayList<>();
//
//        // 1. System prompt always first
//        messages.add(Map.of("role", "system", "content", MEDICAL_SYSTEM_PROMPT));
//
//        // 2. ✅ Sliding window: last 10 messages EXCLUDING the last one
//        //    (the last one is the user message we just added — passed separately below)
//        int windowSize = 10;
//        List<ChatMessage> historyWithoutLast = history.subList(0, Math.max(0, history.size() - 1));
//        int startIndex = Math.max(0, historyWithoutLast.size() - windowSize);
//
//        for (int i = startIndex; i < historyWithoutLast.size(); i++) {
//            messages.add(Map.of(
//                    "role", historyWithoutLast.get(i).getRole(),
//                    "content", historyWithoutLast.get(i).getContent()
//            ));
//        }
//
//        // 3. Add the current user message
//        // 3. Add the current user message WITH a language enforcement hint
//        String languageEnforcedMessage = newMessage +
//                "\n\n[SYSTEM LANGUAGE INSTRUCTION: Detect the language of the message above and reply ONLY in that exact language.]";
//
//        messages.add(Map.of("role", "user", "content", languageEnforcedMessage));
//
//        request.put("messages", messages);
//        return request;
//    }
//
//    private String extractResponseText(JsonNode response) {
//        if (response == null) throw new RuntimeException("Empty response from Groq");
//        return response.path("choices").get(0).path("message").path("content").asText();
//    }
//}