package com.example.diplomathesisfinalcollection.medicalrecorder;


import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    // Գտնել կոնկրետ այցի բժշկական քարտը
    Optional<MedicalRecord> findByAppointmentId(String appointmentId);

    // Գտնել պացիենտի ամբողջ պատմությունը
    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(String patientId);
    List<MedicalRecord> findByPatientIdAndStatusOrderByRecordDateDesc(String patientId, String status);
//    List<MedicalRecord> findByPatientId(String patientId);

}
