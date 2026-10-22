package com.example.diplomathesisfinalcollection.drugs;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DrugInteractionService {

    @Autowired
    private DrugInteractionRepository repository;

    @PostConstruct
    public void seedData() {
        if (repository.count() == 0) {
            repository.save(DrugInteraction.builder().drugA("Aspirin").drugB("Warfarin").severity("HIGH").warningMessage("ՎՏԱՆԳ: Արյունահոսության բարձր ռիսկ!").build());
            repository.save(DrugInteraction.builder().drugA("Statins").drugB("Grapefruit Juice").severity("HIGH").warningMessage("ՎՏԱՆԳ: Լյարդի վնասման մեծ ռիսկ!").build());
            repository.save(DrugInteraction.builder().drugA("Insulin").drugB("Beta-Blockers").severity("MODERATE").warningMessage("ԶԳՈՒՇԱՑՈՒՄ: Կարող է քողարկել հիպոգլիկեմիան:").build());
            repository.save(DrugInteraction.builder().drugA("Amoxicillin").drugB("Methotrexate").severity("HIGH").warningMessage("ՎՏԱՆԳ: Մետոտրեքսատի տոքսիկության աճ!").build());
            repository.save(DrugInteraction.builder().drugA("Ibuprofen").drugB("Aspirin").severity("MODERATE").warningMessage("ԶԳՈՒՇԱՑՈՒՄ: Ստամոքսի գրգռում, դեղերի ազդեցության նվազում:").build());
        }
    }

    public String checkInteractions(List<String> prescribedDrugs) {
        if (prescribedDrugs == null || prescribedDrugs.size() < 2) return "OK";

        for (int i = 0; i < prescribedDrugs.size(); i++) {
            for (int j = i + 1; j < prescribedDrugs.size(); j++) {
                String d1 = prescribedDrugs.get(i).trim();
                String d2 = prescribedDrugs.get(j).trim();

                // Ստուգում ենք երկու ուղղությամբ (A-B կամ B-A)
                List<DrugInteraction> interactions = repository.findByDrugAIgnoreCaseAndDrugBIgnoreCase(d1, d2);
                if (interactions.isEmpty()) {
                    interactions = repository.findByDrugAIgnoreCaseAndDrugBIgnoreCase(d2, d1);
                }

                if (!interactions.isEmpty()) {
                    return interactions.get(0).getWarningMessage();
                }
            }
        }
        return "OK";
    }
}