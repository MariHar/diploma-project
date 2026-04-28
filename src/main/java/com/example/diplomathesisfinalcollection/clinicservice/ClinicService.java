package com.example.diplomathesisfinalcollection.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clinic_services")
public class ClinicService {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // 🔴 Եզակի կոդ (օր.՝ ECG_01)

    private String departmentId; // Կապը Department-ի հետ

    // Եռալեզու դաշտեր
    private String nameAm;
    private String nameRu;
    private String nameEn;

    private String descriptionAm;
    private String descriptionRu;
    private String descriptionEn;

    private String preparationRulesAm; // Նախապատրաստման կանոններ (օր.՝ քաղցած գալ)
    private String preparationRulesRu;
    private String preparationRulesEn;

    private Double price;
    private Integer durationMinutes;

    @Builder.Default
    private boolean isOnlineBookable = true; // Կարո՞ղ է հիվանդն ինքնուրույն գրանցվել

    @Builder.Default
    private boolean active = true;
}