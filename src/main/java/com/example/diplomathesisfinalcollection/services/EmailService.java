package com.example.diplomathesisfinalcollection.service;

import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.laboratory.LabTest;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.user.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.pdf.BaseFont;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.util.Base64;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine; // Thymeleaf-ի շարժիչը նամակի համար

    public void sendMedicalRecordEmail(String to, MedicalRecord record, User patient, Doctor doctor, List<LabTest> labTests) {
        if (to == null || to.trim().isEmpty()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Բժշկական փաստաթղթերի փաթեթ - " + patient.getFirstName() + " " + patient.getLastName());

            // Թարմացված տեքստ
            String emailText = "Հարգելի պացիենտ, Ձեր հետազոտությունների արդյունքները և դեղատոմսը կցված են այս նամակին PDF ձևաչափով։\n\n" +
                    "Առողջություն ենք մաղթում,\nMedicare Pro System";
            helper.setText(emailText, false);

            // --- 1. ԳԼԽԱՎՈՐ ՔԱՐՏ (Medical Report) ---
            byte[] recordPdf = generateMedicalRecordPdfBytes(record, patient, doctor, labTests);
            helper.addAttachment("Medical_Report_" + patient.getLastName() + ".pdf", new ByteArrayResource(recordPdf));

            // --- 2. ԼԱԲՈՐԱՏՈՐ ԱՆԱԼԻԶՆԵՐ (All Lab Results) ---
            byte[] labPdf = generateCombinedLabResultsPdfBytes(patient, labTests);
            if (labPdf != null) {
                helper.addAttachment("All_Lab_Results_" + patient.getLastName() + ".pdf", new ByteArrayResource(labPdf));
            }

            // --- 3. ԹՎԱՅԻՆ ԴԵՂԱՏՈՄՍ (E-Prescription) ---
            // Ուղարկում ենք միայն եթե բժիշկը դեղեր է լրացրել
            if (record.getPrescriptions() != null && !record.getPrescriptions().isEmpty()) {
                byte[] prescriptionPdf = generatePrescriptionPdfBytes(record, patient, doctor);
                helper.addAttachment("E_Prescription_" + patient.getLastName() + ".pdf", new ByteArrayResource(prescriptionPdf));
                System.out.println("💊 Դեղատոմսը կցվեց նամակին:");
            }

            mailSender.send(message);
            System.out.println("✅ Ամբողջական բժշկական փաթեթը ուղարկվեց: " + to);

        } catch (Exception e) {
            System.err.println("❌ Սխալ նամակ ուղարկելիս: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@medicarepro.com");

            mailSender.send(message);

            // Եթե այս տողին հասավ ու չքրաշվեց, ուրեմն ամեն ինչ լավ է
            return true;

        } catch (Exception e) {
            // Եթե մեյլը սխալ է կամ Google-ը մերժեց (BadCredentials)
            System.out.println("❌ ՍԽԱԼ ՆԱՄԱԿ ՈՒՂԱՐԿԵԼԻՍ: " + e.getMessage());

            // Ասում ենք Controller-ին, որ չստացվեց
            return false;
        }
    }

//    // Ավելացրու EmailService-ում
//    public void sendSeparateLabResultPdfToPatient(User patient, LabTest test) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            // true-ն նշանակում է, որ նամակը multipart է (կարող է ունենալ կցված ֆայլեր)
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(patient.getEmail());
//            helper.setSubject("🧪 Ձեր լաբորատոր հետազոտության պատասխանը (" + test.getTestName() + ")");
//            helper.setText("Բարև Ձեզ, հարգելի " + patient.getFirstName() + "։\n\nՁեր լաբորատոր հետազոտության պատասխանը պատրաստ է և կցված է այս նամակին որպես PDF ֆայլ։\n\nԱռողջություն ենք մաղթում,\nMedicare Pro System", false);
//
//            // 1. Պատրաստում ենք HTML կոնտեքստը
//            Context context = new Context();
//            context.setVariable("patient", patient);
//            context.setVariable("test", test);
//
//            // "lab-result-email" - սա այն html ֆայլի անունն է, որի դիզայնը ես ուղարկել էի
//            String htmlContent = templateEngine.process("lab-result-email", context);
//
//            // 2. HTML-ը վերածում ենք PDF-ի բայթերի
//            ByteArrayOutputStream target = new ByteArrayOutputStream();
//            PdfRendererBuilder builder = new PdfRendererBuilder();
//            builder.useFastMode();
//            builder.withHtmlContent(htmlContent, null);
//            builder.toStream(target);
//            builder.run();
//
//            byte[] pdfBytes = target.toByteArray();
//
//            // 3. Կցում ենք PDF-ը նամակին
//            String fileName = "Lab_Result_" + test.getTestName().replaceAll("\\s+", "_") + ".pdf";
//            helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));
//
//            // 4. Ուղարկում ենք
//            mailSender.send(message);
//            System.out.println("✅ Անալիզի PDF-ը ուղարկվեց պացիենտին: " + patient.getEmail());
//
//        } catch (Exception e) {
//            System.out.println("❌ Սխալ Անալիզի PDF-ը ուղարկելիս:");
//            e.printStackTrace();
//        }
//    }

    // 1. Մեթոդ՝ Գլխավոր բժշկական քարտը PDF դարձնելու համար
    public byte[] generateMedicalRecordPdfBytes(MedicalRecord record, User patient, Doctor doctor, List<LabTest> labTests) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.setVariable("record", record);
            context.setVariable("patient", patient);
            context.setVariable("doctor", doctor);
            context.setVariable("labTests", labTests);

            // Համոզվիր, որ այս HTML-ը գտնվում է templates/email/medical-record-email.html հասցեում
            String htmlContent = templateEngine.process("email/medical-record-email", context);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> {
                try {
                    return new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream();
                } catch (Exception e) {
                    throw new RuntimeException("Font load error", e);
                }
            }, "DejaVu Sans");
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Medical Record PDF", e);
        }
    }

    // 2. Մեթոդ՝ Բոլոր անալիզները մեկ միասնական PDF դարձնելու համար
    public byte[] generateCombinedLabResultsPdfBytes(User patient, List<LabTest> labTests) {
        if (labTests == null || labTests.isEmpty()) return null;

        List<LabTest> completedTests = labTests.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .toList();

        if (completedTests.isEmpty()) return null;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.setVariable("patient", patient);
            context.setVariable("labTests", completedTests);

            // 🔴 ԱՄԵՆԱԿԱՐԵՎՈՐ ՏՈՂԸ: Այստեղ ՊԵՏՔ Է լինի Անալիզների HTML ֆայլի անունը
            // Եթե քո ֆայլի անունը ուրիշ է (օրինակ՝ "email/lab-results-combined"), փոխիր այստեղ
            String htmlContent = templateEngine.process("lab-result-email", context);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> {
                try {
                    return new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream();
                } catch (Exception e) {
                    throw new RuntimeException("Font load error", e);
                }
            }, "DejaVu Sans");
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Combined Lab PDF", e);
        }
    }

    public String generateQRCodeBase64(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Սարքում ենք QR կոդը 200x200 չափսի
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            // Վերադարձնում ենք որպես Base64, որպեսզի HTML-ը անմիջապես ցույց տա նկարը
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            System.err.println("QR կոդի գեներացման սխալ: " + e.getMessage());
            return ""; // Սխալի դեպքում վերադարձնում ենք դատարկ տող
        }
    }

    public byte[] generatePrescriptionPdfBytes(MedicalRecord record, User patient, Doctor doctor) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 1. Սարքում ենք վերիֆիկացիայի լինկը QR-ի համար
            String verificationUrl = "http://172.20.10.4:8080/verify/prescription/" + record.getId();            String qrCodeBase64 = generateQRCodeBase64(verificationUrl);

            // 2. Պատրաստում ենք Thymeleaf-ի կոնտեքստը
            Context context = new Context();
            context.setVariable("record", record);
            context.setVariable("patient", patient);
            context.setVariable("doctor", doctor);
            context.setVariable("qrCode", qrCodeBase64); // Միացնում ենք նկարը HTML-ին

            // 3. HTML-ը դարձնում ենք տեքստ
            String htmlContent = templateEngine.process("email/prescription-pdf", context);

            // 4. PDF-ի կառուցում
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> {
                try {
                    return new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream();
                } catch (Exception e) {
                    throw new RuntimeException("Ֆոնտի բեռնման սխալ", e);
                }
            }, "DejaVu Sans");
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            // ✅ ՎԵՐԱԴԱՐՁՆՈՒՄ ԵՆՔ ԲԱՅԹԵՐԸ
            return outputStream.toByteArray();

        } catch (Exception e) {
            System.err.println("Դեղատոմսի PDF սարքելու սխալ: " + e.getMessage());
            throw new RuntimeException("Error generating Prescription PDF", e);
        }
    }
}
