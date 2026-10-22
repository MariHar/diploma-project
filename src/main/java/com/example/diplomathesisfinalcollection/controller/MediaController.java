package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
public class MediaController {

    private final FileUploadService fileUploadService;

    @GetMapping("/secure-image")
    public ResponseEntity<byte[]> getSecureImage(@RequestParam("id") String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            String targetUrl;

            // Հին բաց ֆայլերի ստուգում
            if (publicId.startsWith("http")) {
                targetUrl = publicId;
            } else {
                // Նոր անվտանգ ֆայլերի համար գեներացնում ենք հղումը
                targetUrl = fileUploadService.generateSignedUrl(publicId);
            }

            // Ստեղծում ենք RestTemplate, որպեսզի մեր ՍԵՐՎԵՐԸ գնա և բերի նկարը
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> imageResponse = restTemplate.getForEntity(targetUrl, byte[].class);

            // Ստեղծում ենք Header-ներ, որպեսզի բրաուզերը հասկանա, որ սա նկար է
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            // Նշում ենք, որ բրաուզերը չքեշավորի այն (անվտանգության համար)
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());

            // Վերադարձնում ենք բուն ՆԿԱՐԸ (բայթերը) մեր սեփական հասցեով
            return new ResponseEntity<>(imageResponse.getBody(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}