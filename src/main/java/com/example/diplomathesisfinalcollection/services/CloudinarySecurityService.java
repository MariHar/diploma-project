package com.example.diplomathesisfinalcollection.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinarySecurityService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * ՄԵԹՈԴ 1. ԱՆՎՏԱՆԳ ՎԵՐԲԵՌՆՈՒՄ
     * Վերբեռնում է ֆայլը և վերադարձնում դրա նոր, գաղտնիացված ID-ն:
     */
    public String uploadSecureFile(MultipartFile file) throws IOException {
        // Ստեղծում ենք պատահական և եզակի անուն ֆայլի համար
        String securePublicId = "medical_exams/" + UUID.randomUUID().toString();

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", securePublicId,
                "type", "authenticated", // ԽԻՍՏ ԿԱՐԵՎՈՐ Է. Ֆայլը դարձնում է փակ
                "resource_type", "auto"  // Ավտոմատ որոշում է՝ նկար է, թե PDF
        ));

        // Վերադարձնում ենք ֆայլի ID-ն, որը պետք է պահպանեք ձեր MongoDB-ում
        return uploadResult.get("public_id").toString();
    }

    /**
     * ՄԵԹՈԴ 2. ՍՏՈՐԱԳՐՎԱԾ (ԺԱՄԱՆԱԿԱՎՈՐ) ՀՂՄԱՆ ՍՏԵՂԾՈՒՄ
     * Ստանում է ID-ն բազայից և վերադարձնում 15 րոպեանոց աշխատող հղում:
     */
    public String generateSignedUrl(String publicId) {
        try {
            // Սահմանում ենք հղման կյանքի տևողությունը՝ ընթացիկ ժամանակին գումարած 15 րոպե (900 վայրկյան)
            long expirationTimestamp = (System.currentTimeMillis() / 1000) + 900;

            return cloudinary.url()
                    .resourceType("image") // Եթե PDF է լինելու, կարող եք դարձնել "raw" կամ "auto"
                    .type("authenticated")
                    .secure(true)          // Պարտադրում է HTTPS
                    .signed(true)         // Ստեղծում է անվտանգության ստորագրություն
//                    .transformation(new Transformation().expireAt(expirationTimestamp))
                    .generate(publicId);
        } catch (Exception e) {
            throw new RuntimeException("Հնարավոր չէ գեներացնել անվտանգ հղում", e);
        }
    }

    // Add this to your CloudinarySecurityService.java

    public String uploadDepartmentImage(MultipartFile file) throws IOException {
        // We use a different folder for gallery
        String publicId = "departments/" + UUID.randomUUID().toString();

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "type", "authenticated",
                "resource_type", "image"
        ));
        return uploadResult.get("public_id").toString();
    }
}
