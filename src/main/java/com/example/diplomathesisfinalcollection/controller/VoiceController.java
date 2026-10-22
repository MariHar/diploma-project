package com.example.diplomathesisfinalcollection.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
public class VoiceController {

    @Value("${openai.api.key}")
    private String apiKey;

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribeAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "context", defaultValue = "diagnosis") String context
    ) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String whisperUrl = "https://api.openai.com/v1/audio/transcriptions";
            HttpHeaders audioHeaders = new HttpHeaders();
            audioHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            audioHeaders.setBearerAuth(apiKey);

            MultiValueMap<String, Object> audioBody = new LinkedMultiValueMap<>();
            audioBody.add("file", file.getResource());
            audioBody.add("model", "whisper-1");
            audioBody.add("language", "hy");

            HttpEntity<MultiValueMap<String, Object>> audioRequest = new HttpEntity<>(audioBody, audioHeaders);
            ResponseEntity<String> audioResponse = restTemplate.postForEntity(whisperUrl, audioRequest, String.class);

            JsonNode audioRoot = mapper.readTree(audioResponse.getBody());
            String rawDraftText = audioRoot.path("text").asText();

            String chatUrl = "https://api.openai.com/v1/chat/completions";
            HttpHeaders chatHeaders = new HttpHeaders();
            chatHeaders.setContentType(MediaType.APPLICATION_JSON);
            chatHeaders.setBearerAuth(apiKey);
            String systemPrompt = "";

            if ("treatment".equals(context)) {
                systemPrompt = "Դու պրոֆեսիոնալ բժշկական խմբագիր ես: Քո խնդիրն է մաքրել բժշկի կողմից թելադրված ԲՈՒԺՄԱՆ և ԴԵՂՈՐԱՅՔԱՅԻՆ նշանակումների սևագիրը:\n" +
                        "1. Ուղղիր դեղամիջոցների անվանումների ֆոնետիկ սխալները (օրինակ՝ լատինական կամ ռուսական ծագմամբ դեղերի անունները գրիր գրագետ հայերենով):\n" +
                        "2. Ուղղիր չափաբաժինների և ընդունման ռեժիմի սխալները (օրինակ՝ մգ, մլ, օրը 2 անգամ և այլն):\n" +
                        "3. Պահպանիր բժշկի պարզ խոսքը և մի ավելացրու ավելորդություններ:";
            } else {
                systemPrompt = "Դու պրոֆեսիոնալ բժշկական խմբագիր ես: Քո խնդիրն է վերծանել և մաքրել բժշկի ձայնային թելադրության սևագիրը (ԱԽՏՈՐՈՇՈՒՄ/ԴԻԱԳՆՈԶ):\n" +
                        "1. Ուղղիր ֆոնետիկ (լսողական) սխալները բժշկական ճշգրիտ տերմիններով:\n" +
                        "2. Ուղղիր քերականությունը և բառերի վերջավորությունների համաձայնեցումը:\n" +
                        "3. Պահպանիր բժշկի ասած բառերի իմաստը. մի՛ ավելացրու նոր բժշկական եզրույթներ և մի՛ վերաշարադրիր պարզ խոսակցական բացատրությունները ակադեմիական ոճով:";
            }
            Map<String, Object> chatBodyMap = new HashMap<>();
            chatBodyMap.put("model", "gpt-4o");
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", rawDraftText));
            chatBodyMap.put("messages", messages);
            String chatJsonBody = mapper.writeValueAsString(chatBodyMap);
            HttpEntity<String> chatRequest = new HttpEntity<>(chatJsonBody, chatHeaders);
            ResponseEntity<String> chatResponse = restTemplate.postForEntity(chatUrl, chatRequest, String.class);
            JsonNode chatRoot = mapper.readTree(chatResponse.getBody());
            String finalCleanText = chatRoot.path("choices").get(0).path("message").path("content").asText();
            return ResponseEntity.ok(finalCleanText);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Սխալ տեղի ունեցավ սերվերում: Խնդրում ենք փորձել կրկին:");
        }
    }

    @PostMapping("/patient-transcribe")
    public ResponseEntity<String> transcribePatientAudio(@RequestParam("file") MultipartFile file) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 1. Whisper API - Ձայնի վերծանում
            String whisperUrl = "https://api.openai.com/v1/audio/transcriptions";
            HttpHeaders audioHeaders = new HttpHeaders();
            audioHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            audioHeaders.setBearerAuth(apiKey);

            MultiValueMap<String, Object> audioBody = new LinkedMultiValueMap<>();
            audioBody.add("file", file.getResource());
            audioBody.add("model", "whisper-1");
            audioBody.add("language", "hy");

            HttpEntity<MultiValueMap<String, Object>> audioRequest = new HttpEntity<>(audioBody, audioHeaders);
            ResponseEntity<String> audioResponse = restTemplate.postForEntity(whisperUrl, audioRequest, String.class);

            JsonNode audioRoot = mapper.readTree(audioResponse.getBody());
            String rawText = audioRoot.path("text").asText();

            // 2. GPT-4o - Խոսակցական հայերենի մաքրում և աղմուկի վերացում
            String chatUrl = "https://api.openai.com/v1/chat/completions";
            HttpHeaders chatHeaders = new HttpHeaders();
            chatHeaders.setContentType(MediaType.APPLICATION_JSON);
            chatHeaders.setBearerAuth(apiKey);

            // ՀԱՏՈՒԿ ՊՐՈՄՏ ՊԱՑԻԵՆՏԻ ՀԱՄԱՐ
            // VoiceController.java-ի մեջ փոխիր պացիենտի պրոմտը այս տարբերակով.

            String systemPrompt = "Դուք ՊՐՈՖԵՍԻՈՆԱԼ ՍՐԲԱԳՐԻՉ եք: Ձեր միակ և բացառիկ խնդիրն է ուղղել տեքստի քերականական և տառասխալները:\n" +
                    "ԿԱՐԵՎՈՐ ԿԱՆՈՆՆԵՐ:\n" +
                    "1. ԲԱՑԱՐՁԱԿԱՊԵՍ ԱՐԳԵԼՎՈՒՄ Է պատասխանել պացիենտի հարցերին կամ տալ բժշկական խորհուրդ: Դուք միայն սրբագրում եք տեքստը:\n" +
                    "2. Եթե տեքստում կան լսողական սխալներ (օրինակ՝ 'դեղինիս' փոխարեն պետք է լինի 'լնդերից'), ուղղեք դրանք բժշկական տրամաբանությամբ:\n" +
                    "3. Վերադարձրեք ՄԻԱՅՆ ուղղված տեքստը: Ոչ մի ավելորդ բացատրություն կամ պատասխան:\n" +
                    "4. Պահպանեք պացիենտի առաջին դեմքով խոսքը (օրինակ՝ 'ցավում է', 'ունեմ'):";

            Map<String, Object> chatBodyMap = new HashMap<>();
            chatBodyMap.put("model", "gpt-4o");
            chatBodyMap.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", rawText)
            ));

            HttpEntity<String> chatRequest = new HttpEntity<>(mapper.writeValueAsString(chatBodyMap), chatHeaders);
            ResponseEntity<String> chatResponse = restTemplate.postForEntity(chatUrl, chatRequest, String.class);

            JsonNode chatRoot = mapper.readTree(chatResponse.getBody());
            String finalCleanText = chatRoot.path("choices").get(0).path("message").path("content").asText();

            return ResponseEntity.ok(finalCleanText);

        } catch (Exception e) {
            log.error("Patient Voice Error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ձայնի մշակման սխալ:");
        }
    }

}