package com.example.diplomathesisfinalcollection.newaiclaude;//package com.example.claudeaiip.newaiclaude;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.util.retry.Retry;
//
//import java.time.Duration;
//import java.util.*;
//
//@Service
//@Slf4j
//public class GeminiAiService {
//
//    private final WebClient webClient;
//
//    @Value("${gemini.api.key}")
//    private String apiKey;
//
//    @Value("${gemini.api.url}")
//    private String apiUrl;
//
//    @Value("${gemini.api.model}")
//    private String model;
//
//    @Value("${gemini.generation.temperature}")
//    private double temperature;
//
//    @Value("${gemini.generation.max-output-tokens}")
//    private int maxOutputTokens;
//
//    @Value("${gemini.generation.top-p}")
//    private double topP;
//
//    @Value("${gemini.generation.top-k}")
//    private int topK;
//
//    private static final String MEDICAL_SYSTEM_PROMPT = """
//        You are MediAssist AI, a professional medical assistant chatbot integrated
//        into a licensed healthcare platform.
//
//        YOUR ROLE:
//        1. Provide general health information and education
//        2. Help patients understand medical terminology and lab results
//        3. Offer guidance on when to seek immediate medical attention
//        4. Assist patients in understanding symptoms WITHOUT diagnosing
//        5. Share evidence-based wellness and preventive health tips
//        6. Help patients prepare questions for their doctor appointments
//        7. Explain medications general purposes (not prescribing)
//
//        STRICT RULES:
//        - You are NOT a doctor and CANNOT replace professional medical advice
//        - NEVER provide specific diagnoses
//        - NEVER prescribe medications or suggest specific dosages
//        - NEVER tell a patient to stop taking prescribed medication
//        - ALWAYS recommend consulting their healthcare provider
//        - For EMERGENCY symptoms (chest pain, difficulty breathing, severe bleeding,
//          signs of stroke, allergic reactions, suicidal thoughts), IMMEDIATELY advise
//          calling emergency services (911/112) or going to the nearest ER
//        - Be empathetic, warm, professional, and clear
//        - Use simple language patients can understand
//        - When uncertain, say so honestly
//        """;
//
//    public GeminiAiService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.build();
//    }
//
//    public String chat(List<ChatMessage> conversationHistory, String newUserMessage) {
//        String url = String.format("%s/%s:generateContent?key=%s", apiUrl, model, apiKey);
//
//        Map<String, Object> requestBody = buildRequestBody(conversationHistory, newUserMessage);
//
//        try {
//            JsonNode response = webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
//                        if (clientResponse.statusCode().value() == 429) {
//                            log.warn("Rate limited by Gemini API. Retrying...");
//                        }
//                        return clientResponse.createException();
//                    })
//                    .bodyToMono(JsonNode.class)
//                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
//                            .maxBackoff(Duration.ofSeconds(10))
//                            .filter(throwable -> {
//                                String msg = throwable.getMessage();
//                                return msg != null && msg.contains("429");
//                            })
//                            .doBeforeRetry(signal ->
//                                    log.info("Retrying Gemini API call. Attempt: {}",
//                                            signal.totalRetries() + 1))
//                    )
//                    .block(Duration.ofSeconds(30));
//
//            return extractResponseText(response);
//
//        } catch (Exception e) {
//            log.error("Gemini API call failed: {}", e.getMessage());
//
//            if (e.getMessage() != null && e.getMessage().contains("429")) {
//                return "⏳ I'm receiving too many requests right now. " +
//                        "Please wait 30 seconds and try again.";
//            }
//
//            return "I'm sorry, I'm temporarily unable to respond. " +
//                    "Please try again or contact your healthcare provider directly.";
//        }
//    }
//
//    private Map<String, Object> buildRequestBody(
//            List<ChatMessage> history, String newMessage) {
//
//        Map<String, Object> request = new LinkedHashMap<>();
//
//        request.put("systemInstruction", Map.of(
//                "parts", List.of(Map.of("text", MEDICAL_SYSTEM_PROMPT))
//        ));
//
//        List<Map<String, Object>> contents = new ArrayList<>();
//
//        for (ChatMessage msg : history) {
//            String geminiRole = msg.getRole().equals("assistant") ? "model" : "user";
//            contents.add(Map.of(
//                    "role", geminiRole,
//                    "parts", List.of(Map.of("text", msg.getContent()))
//            ));
//        }
//
//        contents.add(Map.of(
//                "role", "user",
//                "parts", List.of(Map.of("text", newMessage))
//        ));
//
//        request.put("contents", contents);
//
//        request.put("generationConfig", Map.of(
//                "temperature", temperature,
//                "maxOutputTokens", maxOutputTokens,
//                "topP", topP,
//                "topK", topK
//        ));
//
//        return request;
//    }
//
//    private String extractResponseText(JsonNode response) {
//        if (response == null) {
//            return "I couldn't process your request. Please try again.";
//        }
//
//        JsonNode candidates = response.path("candidates");
//        if (candidates.isEmpty()) {
//            JsonNode blockReason = response.path("promptFeedback").path("blockReason");
//            if (!blockReason.isMissingNode()) {
//                log.warn("Gemini blocked the request. Reason: {}", blockReason.asText());
//                return "I'm unable to respond to that query. " +
//                        "If you have a medical concern, please contact your healthcare provider.";
//            }
//            return "I couldn't generate a response. Please rephrase your question.";
//        }
//
//        return candidates.get(0)
//                .path("content")
//                .path("parts")
//                .get(0)
//                .path("text")
//                .asText("I couldn't generate a response. Please try again.");
//    }
//}