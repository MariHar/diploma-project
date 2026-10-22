package com.example.diplomathesisfinalcollection.drugs;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DrugInteractionRepository extends MongoRepository<DrugInteraction, String> {
    List<DrugInteraction> findByDrugAIgnoreCaseAndDrugBIgnoreCase(String drugA, String drugB);
}