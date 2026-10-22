package com.example.diplomathesisfinalcollection.instrumental;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface InstrumentalExamRepository extends MongoRepository<InstrumentalExam, String> {

    // For the Radiologist Dashboard
    List<InstrumentalExam> findByStatusOrderByOrderDateAsc(String status);
    List<InstrumentalExam> findByPatientId(String patientId);

    // To show results on the doctor's page
    List<InstrumentalExam> findByMedicalRecordId(String medicalRecordId);
    List<InstrumentalExam> findByAppointmentId(String appointmentId);
    List<InstrumentalExam> findByStatusAndIsInvoicedFalse(String status);
    List<InstrumentalExam> findByStatusAndIsInvoicedFalseAndPatientId(String status, String patientId);

}