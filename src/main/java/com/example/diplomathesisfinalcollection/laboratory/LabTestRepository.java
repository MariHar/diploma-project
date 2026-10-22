package com.example.diplomathesisfinalcollection.laboratory;

import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LabTestRepository extends MongoRepository<LabTest, String> {
    List<LabTest> findByStatus(String status);
    List<LabTest> findByAppointmentId(String appointmentId);
    List<LabTest> findByPatientId(String patientId);
    List<LabTest> findByStatusAndIsInvoicedFalse(String status);
    List<LabTest> findByStatusAndIsInvoicedFalseAndPatientId(String status, String patientId);


}
