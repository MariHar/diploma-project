package com.example.diplomathesisfinalcollection.dto;

import com.example.diplomathesisfinalcollection.instrumental.InstrumentalExam;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.Medication;

import java.util.List;

public record CardHashDTO(
        String complaints,
        String anamnesisMorbi,
        String anamnesisVitae,
        String symptoms,
        String diagnosis,
        String treatment,
        String contraindications,
        String notes,
        List<Medication> prescriptions,
        String objectiveExamination,
        String inspection,
        String palpation,
        String percussion,
        String auscultation
) {}