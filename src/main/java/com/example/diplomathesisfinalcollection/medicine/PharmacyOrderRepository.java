package com.example.diplomathesisfinalcollection.medicalrecorder;


import com.example.diplomathesisfinalcollection.medicalrecorder.PharmacyOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyOrderRepository extends MongoRepository<PharmacyOrder, String> {

    // Գտնել պացիենտի բոլոր պատվերները
    List<PharmacyOrder> findByPatientIdOrderByOrderDateDesc(String patientId);

    // Ստուգել՝ արդյոք այս դեղատոմսի համար արդեն կա պատվեր, թե ոչ
    Optional<PharmacyOrder> findByMedicalRecordId(String medicalRecordId);
}
