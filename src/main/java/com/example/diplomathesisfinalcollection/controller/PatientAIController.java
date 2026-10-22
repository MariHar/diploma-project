package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.aichatbot.ChatRequest;
import com.example.diplomathesisfinalcollection.aichatbot.ChatResponse;
import com.example.diplomathesisfinalcollection.aichatbot.MedicalChatService;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExam;
import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExamRepository;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.laboratory.LabTestRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/patient/ai")
public class PatientAIController {

    @Autowired
    private MedicalChatService medicalChatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private InstrumentalExamRepository instrumentalExamRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @PostMapping("/chat")
    public Map<String, String> chatWithAI(@RequestBody Map<String, String> request, Principal principal) {
        String userMsg = request.get("message");
        String conversationId = request.get("conversationId");
        String imageBase64 = request.get("image"); // Ավելացված է նկարների համար

        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(userMsg);
        chatRequest.setConversationId(conversationId);
        chatRequest.setImageBase64(imageBase64); // Փոխանցում ենք սերվիսին

        ChatResponse response = medicalChatService.chat(chatRequest, patient.getId());

        return Map.of(
                "reply", response.getReply(),
                "conversationId", response.getConversationId() != null ? response.getConversationId() : ""
        );
    }

    @GetMapping("/explain-record/{recordId}")
    public Map<String, String> explainRecordInArmenian(@PathVariable String recordId, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName()).orElseThrow();
        MedicalRecord record = medicalRecordRepository.findById(recordId).orElseThrow();
        if (!record.getPatientId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized access: This record does not belong to you.");
        }
        String dataToAnalyze = "Diagnosis: " + record.getDiagnosis() + ". Symptoms: " + record.getSymptoms() + ". Treatment Plan: " + record.getTreatment() + ". Doctor's Notes: " + record.getNotes();

        String prompt = "Դուք պրոֆեսիոնալ, հեղինակավոր և էթիկապես գրագետ բժիշկ եք: Ներկայացրեք բժշկական քարտի տվյալները պացիենտին հասկանալի, պաշտոնական, բայց հոգատար հայերենով: Տվյալներ. " + dataToAnalyze + ". " +
                "ԽՍՏԻՎ ՊԱՀԱՆՋՆԵՐ. " +
                "1. ԿԱՌՈՒՑՎԱԾՔ. Տվեք ախտորոշման օբյեկտիվ բացատրությունը, կապեք այն գանգատների հետ և բացատրեք բուժման նպատակը: Տեքստը բաժանեք աբզացների նոր տողով (Enter): " +
                "2. ԲԺՇԿԱԿԱՆ ԷԹԻԿԱ. Պահպանեք բժշկական ճշգրտություն: Մի թերագնահատեք ռիսկերը (օրինակ՝ Ռիսկ 3-ը վաղ փուլ չէ), բայց և մի ահաբեկեք պացիենտին: Արգելվում է տալ 100% երաշխիքներ (չասել «ամեն ինչ լավ կլինի»), փոխարենը նշեք, որ բուժումը միտված է վիճակի բարելավմանը: " +
                "3. ՏՈՆԱՅՆՈՒԹՅՈՒՆ ԵՎ ՖՈՐՄԱՏ. ՉՈՂՋՈՒՆԵԼ և ՉՆԵՐԿԱՅԱՆԱԼ: Չօգտագործել մանկամիտ տոն («եկեք սկսենք», «սիրելիս»): Չառաջարկել դիմել բժշկի, քանի որ դուք եք բացատրող բժիշկը: " +
                "4. ԼԵԶՈՒ ԵՎ ՍԱՀՄԱՆԱՓԱԿՈՒՄՆԵՐ. ԲԱՑԱՐՁԱԿԱՊԵՍ ՉՕԳՏԱԳՈՐԾԵԼ Markdown ձևավորում և ոչ մի աստղանիշ (*): Խստիվ արգելվում են «ինչը նշանակում է», «ինչը վկայում է» շաբլոնները. փոխարենը կառուցեք գրագետ պատճառահետևանքային նախադասություններ:";
        String aiExplanation = medicalChatService.generateStatelessExplanation(patient.getId(), prompt);
        return Map.of("explanation", aiExplanation);
    }

    @GetMapping("/explain-exam-direct/{examId}")
    public Map<String, String> explainExamDirect(@PathVariable String examId, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName()).orElseThrow();

        InstrumentalExam exam = instrumentalExamRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam record not found"));

        if (!exam.getPatientId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized access: This exam does not belong to you.");
        }

        String prompt = "Դուք պրոֆեսիոնալ ռադիոլոգ եք: Տվեք հետազոտության արդյունքների հստակ և օբյեկտիվ կլինիկական մեկնաբանությունը պարզ հայերենով: " +
                "Հետազոտություն՝ " + exam.getExamType() + ", Տիրույթ՝ " + exam.getAreaToExamine() + ", Գտածոներ՝ " + exam.getFindings() + ". " +
                "ԽՍՏԻՎ ՊԱՀԱՆՋՆԵՐ. " +
                "1. Գրեք 3-4 տրամաբանորեն կապված նախադասություն: Իրերը կոչեք իրենց անուններով (օրինակ՝ սրտի լայնացումը նկարագրեք որպես օբյեկտիվ փաստ՝ առանց կոծկելու, որ դա նորմալ չէ): " +
                "2. Պահպանեք ճշգրիտ բժշկական տերմիններ, օրինակ՝ բրոնխիտը շնչուղիների բորբոքումն է, ոչ թե թոքերի: " +
                "3. ԼԵԶՎԱԿԱՆ ԿԱՆՈՆՆԵՐ. Խստիվ արգելվում է օգտագործել «ինչը նշանակում է», «ինչը պայմանավորված է» մեխանիկական կառույցները: Կառուցեք հարուստ և բնական հայերեն տեքստ: " +
                "4. ԱՐԳԵԼՎԱԾ Է. Չօգտագործել ոչ մի աստղանիշ (*) կամ Markdown: Չողջունել պացիենտին, չտալ էմոցիոնալ գնահատականներ (չասել «սա լավ/վատ նորություն է») և չառաջարկել դիմել բժշկի:";

        String aiExplanation = medicalChatService.generateStatelessExplanation(patient.getId(), prompt);
        return Map.of("explanation", aiExplanation);
    }

    @GetMapping("/explain-lab/{labId}")
    public Map<String, String> explainLabInArmenian(@PathVariable String labId, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName()).orElseThrow();

        LabTest lab = labTestRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        if (!lab.getPatientId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized access: This lab test does not belong to you.");
        }

        String prompt = "Դուք լաբորատոր ախտորոշման և կլինիկական պաթոլոգիայի բարձրակարգ մասնագետ եք: Վերլուծեք հետևյալ անալիզի արդյունքները. " +
                lab.getTestName() + ", Արդյունքներ: " + lab.getResults() + ". " +
                "ԽՍՏԻՎ ՊԱՀԱՆՋՆԵՐ ԲԺՇԿԱԿԱՆ ԷԹԻԿԱՅԻ ԵՎ ՇԱՐԱԴՐԱՆՔԻ ՀԱՄԱՐ. " +
                "1. ԽՄԲԱՎՈՐՈՒՄ ԵՎ ՎԵՐԼՈՒԾՈՒԹՅՈՒՆ. Բացարձակապես արգելվում է թվարկել յուրաքանչյուր ցուցանիշն առանձին: Խմբավորեք արդյունքները ըստ համակարգերի (օրինակ՝ հեմոգլոբինն ու էրիթրոցիտները դիտարկելով միասին որպես արյան թթվածնամատակարարման պատկեր): " +
                "2. ՊԱՑԻԵՆՏԻ ԱՆՎՏԱՆԳՈՒԹՅՈՒՆ. Խստիվ արգելվում է ահաբեկել պացիենտին: Մի ներկայացրեք ցածր/բարձր ցուցանիշները որպես կրիտիկական վտանգ, եթե դրանք չափավոր շեղումներ են: Խուսափեք «ձեր մարմինը չի կարողանում պայքարել» կարգի արտահայտություններից: " +
                "3. ՄԱՔՈՒՐ ՏԵՔՍՏ. Տեքստում բացարձակապես արգելվում է օգտագործել անգլերեն հապավումներ, թվեր ու փակագծեր (չգրել hgb=6, mono=-21): Թվերը վերածեք մատչելի կլինիկական նկարագրության: " +
                "4. ԼԵԶՈՒ ԵՎ ՖՈՐՄԱՏ. ՈՉ ՄԻ Markdown և ոչ մի աստղանիշ (*): Արգելվում են «ինչը նշանակում է», «ինչը վկայում է» շաբլոնները: Չողջունել և չուղարկել բժշկի մոտ: Խոսքը պետք է լինի անթերի, սահուն և պրոֆեսիոնալ:";

        String aiExplanation = medicalChatService.generateStatelessExplanation(patient.getId(), prompt);
        return Map.of("explanation", aiExplanation);
    }
}