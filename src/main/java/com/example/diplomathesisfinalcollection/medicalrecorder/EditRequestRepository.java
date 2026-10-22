package com.example.diplomathesisfinalcollection.medicalrecorder;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EditRequestRepository extends MongoRepository<EditRequest, String> {
    EditRequest findTopByRecordIdOrderByRequestDateDesc(String recordId);

    // Պատմության համար կարող ենք բերել բոլոր հարցումները՝ ըստ ժամանակի
    List<EditRequest> findAllByOrderByRequestDateDesc();

    // Կարող ենք նաև գտնել միայն PENDING հարցումները ադմինի համար
    List<EditRequest> findAllByStatus(String status);
    long countByDoctorIdAndStatus(String doctorId, String status);
}
