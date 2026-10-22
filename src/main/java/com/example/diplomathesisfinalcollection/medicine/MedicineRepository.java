package com.example.diplomathesisfinalcollection.medicine;


import com.example.diplomathesisfinalcollection.medicine.Medicine;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface MedicineRepository extends MongoRepository<Medicine, String> {
    // Որոնում ենք դեղը ըստ անվան (Case-Insensitive)
    Optional<Medicine> findByNameIgnoreCase(String name);
    @Aggregation(pipeline = { "{ '$group': { '_id': '$category' } }" })
    List<String> findAllCategories();
    List<Medicine> findAllByNameIgnoreCase(String name);
    List<Medicine> findByNameIgnoreCaseAndActiveTrueOrderByExpirationDateAsc(String name);
    // Ավելացրու MedicineRepository-ի մեջ
}
