package com.example.diplomathesisfinalcollection.medicine;

import com.example.diplomathesisfinalcollection.medicine.PharmacyOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyOrderRepository extends MongoRepository<PharmacyOrder, String> {
    List<PharmacyOrder> findByPatientIdOrderByOrderDateDesc(String patientId);
    List<PharmacyOrder> findByStatusOrderByOrderDateDesc(String status);
    // Inside PharmacyOrderRepository.java
    List<PharmacyOrder> findByMedicalRecordIdOrderByOrderDateDesc(String medicalRecordId);}
