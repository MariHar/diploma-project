package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
//import com.example.diplomathesisfinalcollection.dto.LabDashboardDTO;
import com.example.diplomathesisfinalcollection.laboratory.Indicator;
import com.example.diplomathesisfinalcollection.laboratory.IndicatorRepository;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.laboratory.LabTestRepository;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lab")
//@PreAuthorize("hasRole('LAB_WORKER')")
public class LabController {

    @Autowired private LabTestRepository labTestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private EmailService emailService;
    @Autowired private SpringTemplateEngine templateEngine;

    // ՆՈՐ: Ավելացված է IndicatorRepository-ն նորմաները քաշելու համար
    @Autowired private IndicatorRepository indicatorRepository;

    @GetMapping("/dashboard")
    public String labDashboard(Model model) {
        // 1. Ստանում ենք սպասվող անալիզները
        List<LabTest> pendingTests = labTestRepository.findByStatus("PENDING");

        // 2. Լրացնում ենք @Transient դաշտերը
        for (LabTest test : pendingTests) {

            // Գտնում և լրացնում ենք Պացիենտի անունը
            User user = userRepository.findById(test.getPatientId()).orElse(null);
            if (user != null) {
                test.setPatientFullName(user.getFirstName() + " " + user.getLastName());
            } else {
                test.setPatientFullName("Անհայտ Պացիենտ");
            }

            // Գտնում և լրացնում ենք Բժշկի տվյալները
            Doctor doctor = doctorRepository.findById(test.getDoctorId()).orElse(null);
            if (doctor != null) {
                User doctorUser = userRepository.findById(doctor.getUserId()).orElse(null);
                if (doctorUser != null) {
                    test.setDoctorFullName("Դոկ. " + doctorUser.getFirstName() + " " + doctorUser.getLastName());
                }
                test.setDoctorSpecialization(doctor.getSpecialization());
            } else {
                test.setDoctorFullName("Անհայտ Բժիշկ");
                test.setDoctorSpecialization("Անհայտ Բաժանմունք");
            }
        }

        // 3. Ուղարկում ենք հենց LabTest-երի լիստը ֆրոնտ
        model.addAttribute("tests", pendingTests);

        return "lab/dashboard";
    }

    // ՆՈՐ: REST API ֆրոնտի համար՝ ստանալու անալիզի կառուցվածքը (նորմաները)
    @GetMapping("/api/test-structure")
    @ResponseBody
    public List<Indicator> getTestStructure(@RequestParam String testName) {
        System.out.println("========== ԼԱԲՈՐԱՏՈՐԻԱ DEBUG ==========");
        System.out.println("1. Որոնում ենք բազայում այս անունը: '" + testName + "'");

        // Վերցնում ենք տվյալները և ֆիլտրում միայն ԱԿՏԻՎՆԵՐԸ
        List<Indicator> activeIndicators = indicatorRepository.findByTestName(testName).stream()
                .filter(Indicator::isActive)
                .toList();

        System.out.println("2. Բազայից գտնվեց ակտիվ ցուցանիշների քանակ: " + activeIndicators.size());
        System.out.println("=========================================");

        return activeIndicators;
    }

    // ՓՈՓՈԽՎԱԾ: Ստանում է դինամիկ զանգվածներ ֆրոնտից
    @PostMapping("/test/{id}/complete")
    public String completeTest(@PathVariable String id,
                               @RequestParam("keys[]") List<String> keys,
                               @RequestParam("values[]") List<String> values,
                               @RequestParam(value = "resultNotes", required = false) String resultNotes) {

        LabTest test = labTestRepository.findById(id).orElse(null);

        if (test != null) {
            Map<String, String> resultsMap = new HashMap<>();
            List<Map<String, String>> detailedList = new ArrayList<>();

            // Fetch reference ranges from DB
            List<Indicator> indicators = indicatorRepository.findByTestName(test.getTestName());

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String val = values.get(i);
                resultsMap.put(key, val);

                Indicator ind = indicators.stream()
                        .filter(indItem -> indItem.getIndicatorKey().equals(key))
                        .findFirst().orElse(null);

                if (ind != null) {
                    Map<String, String> row = new HashMap<>();
                    row.put("name", ind.getDisplayName());
                    row.put("value", val);
                    row.put("unit", ind.getUnit() != null ? ind.getUnit() : "-");

                    boolean hasNorms = ind.getNormMin() != null && ind.getNormMax() != null;
                    row.put("norms", hasNorms ? ind.getNormMin() + " - " + ind.getNormMax() : "-");

                    String status = "✓ Նորմա";
                    // We use Tailwind classes: text-red-600 for deviations
                    String statusClass = "text-green-600 font-bold";

                    if (hasNorms) {
                        try {
                            // 1. Մաքրում ենք ԲՈԼՈՐ պրոբելները (եթե մարդը գրել է " 11, 5 ") և ստորակետը դարձնում ենք կետ
// Մաքրում ենք պրոբելները, ՋՆՋՈՒՄ ԵՆՔ բոլոր մինուս նշանները և ստորակետը դարձնում կետ
                            String safeVal = val.replaceAll("\\s+", "").replace("-", "").replace(",", ".");                            double numericVal = Double.parseDouble(safeVal);

                            if (numericVal > ind.getNormMax()) {
                                double diff = numericVal - ind.getNormMax();
                                // 2. Օգտագործում ենք Locale.US, որ արդյունքը հաստատ կետով լինի
                                status = "↑ Բարձր (+" + String.format(java.util.Locale.US, "%.1f", diff) + ")";
                                statusClass = "text-red-600 font-extrabold";
                            } else if (numericVal < ind.getNormMin()) {
                                double diff = ind.getNormMin() - numericVal;
                                // 2. Օգտագործում ենք Locale.US, որ արդյունքը հաստատ կետով լինի
                                status = "↓ Ցածր (-" + String.format(java.util.Locale.US, "%.1f", diff) + ")";
                                statusClass = "text-red-600 font-extrabold";
                            }
                        } catch (NumberFormatException e) {
                            // Եթե իսկապես տառեր են գրել (օրինակ՝ "Դրական")
                            status = "Տեքստային";
                            statusClass = "text-blue-600 font-medium";
                        }
                    }


                    row.put("status", status);
                    row.put("statusClass", statusClass);
                    detailedList.add(row);
                }
            }

            test.setResults(resultsMap);
            test.setDetailedResults(detailedList);
            test.setResultNotes(resultNotes);
            test.setStatus("COMPLETED");
            test.setCompletionDate(LocalDateTime.now());
            labTestRepository.save(test);
        }
        return "redirect:/lab/dashboard?success=completed";
    }

    @GetMapping("/fix-old-data")
    @ResponseBody
    public String fixOldData() {
        // Վերցնում ենք բոլոր ավարտվածները, որոնք ունեն detailedResults (որպեսզի հանկարծ դատարկ չլինի)
        List<LabTest> completedTests = labTestRepository.findAll().stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()) && t.getDetailedResults() != null && !t.getDetailedResults().isEmpty())
                .toList();

        int count = 0;
        for (LabTest test : completedTests) {
            boolean updated = false;

            // Անցնում ենք ԱՐԴԵՆ ԳՈՅՈՒԹՅՈՒՆ ՈՒՆԵՑՈՂ տողերի վրայով (տվյալները չենք կորցնում)
            for (Map<String, String> row : test.getDetailedResults()) {
                String val = row.get("value");
                String norms = row.get("norms");

                // Եթե արժեք չկա կամ նորմա չկա, բաց ենք թողնում
                if (val == null || val.trim().isEmpty() || norms == null || "-".equals(norms)) {
                    continue;
                }

                try {
                    // Նորմաներից (օրինակ՝ "12.0 - 15.5") ստանում ենք min և max
                    String[] normParts = norms.split("-");
                    if (normParts.length == 2) {
                        double min = Double.parseDouble(normParts[0].trim());
                        double max = Double.parseDouble(normParts[1].trim());

                        // 🔴 ԳԵՐԱՊԱՀՈՎ ՄԱՔՐՈՒՄ. թողնում ենք ՄԻԱՅՆ թվերը, կետը, ստորակետը և մինուսը (ջնջում ենք տառեր, պրոբելներ, աստղանիշեր)
                        String safeVal = val.replaceAll("[^0-9.,]", "").replace(",", ".");

                        if (safeVal.isEmpty()) {
                            // Եթե միայն տառեր էին (օրինակ՝ "Բացասական")
                            row.put("status", "Տեքստային");
                            row.put("statusClass", "text-blue-600 font-medium");
                            updated = true;
                            continue;
                        }

                        double numericVal = Double.parseDouble(safeVal);

                        // Հաշվում ենք շեղումը ճշգրիտ կերպով
                        if (numericVal > max) {
                            double diff = numericVal - max;
                            row.put("status", "↑ Բարձր (+" + String.format(java.util.Locale.US, "%.1f", diff) + ")");
                            row.put("statusClass", "text-red-600 font-extrabold");
                        } else if (numericVal < min) {
                            double diff = min - numericVal;
                            row.put("status", "↓ Ցածր (-" + String.format(java.util.Locale.US, "%.1f", diff) + ")");
                            row.put("statusClass", "text-red-600 font-extrabold");
                        } else {
                            row.put("status", "✓ Նորմա");
                            row.put("statusClass", "text-green-600 font-bold");
                        }
                        updated = true;
                    }
                } catch (Exception e) {
                    // Սխալի դեպքում տվյալը թողնում ենք անփոփոխ
                }
            }

            // Պահպանում ենք միայն, եթե ճշգրտում եղել է
            if (updated) {
                labTestRepository.save(test);
                count++;
            }
        }

        return "✅ Հաջողությամբ վերականգնվեց և ճշգրտվեց " + count + " հին արխիվացված անալիզ:";
    }

    @GetMapping("/history")
    public String labHistory(Model model) {
        // 1. Ստանում ենք ՄԻԱՅՆ ավարտված անալիզները (ստեղծում ենք նոր լիստ, որպեսզի կարողանանք սորտավորել)
        List<LabTest> completedTests = new ArrayList<>(labTestRepository.findByStatus("COMPLETED"));

        // Սորտավորում ենք ըստ ավարտի ամսաթվի (նորերը վերևում)
        completedTests.sort((t1, t2) -> {
            if (t1.getCompletionDate() == null || t2.getCompletionDate() == null) return 0;
            return t2.getCompletionDate().compareTo(t1.getCompletionDate());
        });

        // 2. Լրացնում ենք Պացիենտի և Բժշկի տվյալները (Նույն տրամաբանությամբ)
        for (LabTest test : completedTests) {
            User user = userRepository.findById(test.getPatientId()).orElse(null);
            test.setPatientFullName(user != null ? user.getFirstName() + " " + user.getLastName() : "Անհայտ Պացիենտ");

            Doctor doctor = doctorRepository.findById(test.getDoctorId()).orElse(null);
            if (doctor != null) {
                User doctorUser = userRepository.findById(doctor.getUserId()).orElse(null);
                test.setDoctorFullName(doctorUser != null ? "Դոկ. " + doctorUser.getFirstName() + " " + doctorUser.getLastName() : "Անհայտ Բժիշկ");
                test.setDoctorSpecialization(doctor.getSpecialization());
            } else {
                test.setDoctorFullName("Անհայտ Բժիշկ");
                test.setDoctorSpecialization("Անհայտ Բաժանմունք");
            }
        }

        model.addAttribute("tests", completedTests);
        return "lab/history"; // Կբացի նոր HTML էջը
    }

    @GetMapping("/test/{id}/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadPdf(@PathVariable String id) throws Exception {

        LabTest test = labTestRepository.findById(id).orElse(null);
        if (test == null) return org.springframework.http.ResponseEntity.notFound().build();

        // 1. Լրացնում ենք պացիենտի և բժշկի տվյալները (նույն լոգիկան, ինչ դաշբորդում)
        User user = userRepository.findById(test.getPatientId()).orElse(null);
        test.setPatientFullName(user != null ? user.getFirstName() + " " + user.getLastName() : "Անհայտ Պացիենտ");

        Doctor doctor = doctorRepository.findById(test.getDoctorId()).orElse(null);
        if (doctor != null) {
            User doctorUser = userRepository.findById(doctor.getUserId()).orElse(null);
            test.setDoctorFullName(doctorUser != null ? "Դոկ. " + doctorUser.getFirstName() + " " + doctorUser.getLastName() : "-");
            test.setDoctorSpecialization(doctor.getSpecialization());
        }

        // 2. Ուղարկում ենք տվյալները Thymeleaf Context-ին
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("test", test);

        // 3. Կանչում ենք մեր հատուկ PDF-ի HTML ֆայլը (որը հիմա կստեղծենք)
        String html = templateEngine.process("lab/pdf-export", context);

        // 4. HTML-ը վերածում ենք PDF ֆայլի (OpenHTMLToPDF-ի միջոցով)
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
        builder.useFastMode();
        java.net.URL fontUrl = getClass().getResource("/fonts/DejaVuSans.ttf");
        if(fontUrl != null) {
            builder.useFont(new java.io.File(fontUrl.toURI()), "DejaVu Sans");
        }
        builder.withHtmlContent(html, "");
        builder.toStream(outputStream);
        builder.run();

        byte[] pdfBytes = outputStream.toByteArray();

        // 5. Ուղարկում ենք PDF-ը բրաուզեր
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);

// Հատուկ կոդավորում ենք ֆայլի անունը UTF-8-ով, որպեսզի հայերեն տառերը ճիշտ կարդացվեն
        String fileName = "Lab_Result_" + test.getTestName() + ".pdf";
        org.springframework.http.ContentDisposition contentDisposition = org.springframework.http.ContentDisposition.builder("attachment")
                .filename(fileName, java.nio.charset.StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/reset-empty-tests")
    @ResponseBody
    public String resetEmptyTests() {
        // Գտնում ենք այն ավարտված հայտերը, որոնց տվյալները կորել են (դատարկ են)
        List<LabTest> brokenTests = labTestRepository.findAll().stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()) && (t.getDetailedResults() == null || t.getDetailedResults().isEmpty()))
                .toList();

        for (LabTest test : brokenTests) {
            // Վերադարձնում ենք սպասվող կարգավիճակի
            test.setStatus("PENDING");
            test.setCompletionDate(null);
            test.setResultNotes(null);
            labTestRepository.save(test);
        }

        return "✅ " + brokenTests.size() + " փչացած հայտ հաջողությամբ վերադարձվեց Dashboard՝ նորից լրացնելու համար:";
    }
}