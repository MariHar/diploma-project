package com.example.diplomathesisfinalcollection.appointment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    // Find all appointments for a specific patient, ordered by date ascending
    List<Appointment> findByPatientIdOrderByDateAsc(String patientId);

    // Find all appointments for a specific doctor, ordered by date ascending
    List<Appointment> findByDoctorIdOrderByDateAsc(String doctorId);
    boolean existsByDoctorIdAndDate(String doctorId, LocalDateTime date);
    boolean existsByDoctorIdAndDateAndStatusNot(String doctorId, LocalDateTime date, String status);
    boolean existsByPatientIdAndDateBetweenAndStatusNot(String patientId, LocalDateTime startOfDay, LocalDateTime endOfDay, String status);
    List<Appointment> findByDateBetweenAndStatusAndWasNotifiedFalse(LocalDateTime start, LocalDateTime end, String status);
    long countByPatientIdAndDateAfterAndStatus(String patientId, LocalDateTime now, String status);
    List<Appointment> findByDoctorId(String doctorId);

    List<Appointment> findByServiceId(String serviceId);

    List<Appointment> findByDoctorIdAndServiceId(String doctorId, String serviceId);

    // Needed in the Controller to filter by department (several services)
    List<Appointment> findByServiceIdIn(List<String> serviceIds);

    // Find completed appointments that are not yet rated and not skipped
    List<Appointment> findByPatientIdAndStatusAndIsRatedFalseAndIsSkippedFalse(String patientId, String status);

    // Count how many times this patient visited this doctor (status "COMPLETED")
    long countByPatientIdAndDoctorIdAndStatus(String patientId, String doctorId, String status);
    List<Appointment> findByStatusAndIsInvoicedFalse(String status);
    List<Appointment> findByStatusAndIsInvoicedFalseAndPatientId(String status, String patientId);

    boolean existsByDoctorIdAndPatientIdAndDate(String doctorId, String patientId, LocalDateTime date);

}