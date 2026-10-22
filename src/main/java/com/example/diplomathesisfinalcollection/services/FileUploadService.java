package com.example.diplomathesisfinalcollection.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID; // 👈 ՆՈՐ. Անհրաժեշտ է գաղտնի անուններ գեներացնելու համար

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    // =========================================================
    // 1. ՀԻՆ ՄԵԹՈԴՆԵՐԸ (Պահպանված են անփոփոխ՝ հանրային ֆայլերի համար)
    // =========================================================

    public String uploadFile(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto")
        );
        return (String) uploadResult.get("secure_url");
    }

    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> uploadImages = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) uploadImages.get("url");
        } catch (IOException e) {
            throw new RuntimeException("Could not upload file", e);
        }
    }

    // =========================================================
    // 2. ՆՈՐ ՄԵԹՈԴՆԵՐԸ (Բժշկական գաղտնի տվյալների համար)
    // =========================================================

    /**
     * ԱՆՎՏԱՆԳ ՎԵՐԲԵՌՆՈՒՄ: Վերբեռնում է ֆայլը փակ (authenticated) ռեժիմով
     * և վերադարձնում է դրա ID-ն (ոչ թե հանրային հղումը):
     */
    public String uploadSecureFile(MultipartFile file) throws IOException {
        // Գեներացնում ենք չկրկնվող, պատահական անուն պացիենտի ֆայլի համար
        String securePublicId = "medical_exams/" + UUID.randomUUID().toString();

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", securePublicId,
                "type", "authenticated", // ԽԻՍՏ ԿԱՐԵՎՈՐ Է. դարձնում է ֆայլը անհասանելի արտաքին աշխարհի համար
                "resource_type", "auto"
        ));

        return uploadResult.get("public_id").toString(); // Վերադարձնում ենք ID-ն բազայում պահելու համար
    }

    /**
     * ԱՆՎՏԱՆԳ ՀՂՄԱՆ ԳԵՆԵՐԱՑՈՒՄ: Ստեղծում է ստորագրված URL, որը կարելի է օգտագործել
     * միայն տվյալ պահին համակարգ մուտք գործած բժշկին կամ պացիենտին ցուցադրելու համար:
     */
    public String generateSignedUrl(String publicId) {
        return cloudinary.url()
                .resourceType("image")
                .type("authenticated")
                .secure(true)
                .signed(true) // Ավելացնում է գաղտնագրված ստորագրություն
                .generate(publicId);
    }
}