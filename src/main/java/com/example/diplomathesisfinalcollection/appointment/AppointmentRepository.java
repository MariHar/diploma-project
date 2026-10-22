package com.example.diplomathesisfinalcollection.appointment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    // Find all appointments for a specific patient
    List<Appointment> findByPatientIdOrderByDateAsc(String patientId);

    // Find all appointments for a specific doctor
    List<Appointment> findByDoctorIdOrderByDateAsc(String doctorId);
    boolean existsByDoctorIdAndDate(String doctorId, LocalDateTime date);
    boolean existsByDoctorIdAndDateAndStatusNot(String doctorId, LocalDateTime date, String status);
//    boolean existsByPatientEmailAndDateBetweenAndStatusNot(String email, LocalDateTime startOfDay, LocalDateTime endOfDay, String status);
//    boolean existsByDoctorIdAndDateAndStatusNot(String doctorId, LocalDateTime date, String status);
    boolean existsByPatientIdAndDateBetweenAndStatusNot(String patientId, LocalDateTime startOfDay, LocalDateTime endOfDay, String status);
    List<Appointment> findByDateBetweenAndStatusAndWasNotifiedFalse(LocalDateTime start, LocalDateTime end, String status);
    long countByPatientIdAndDateAfterAndStatus(String patientId, LocalDateTime now, String status);
    List<Appointment> findByDoctorId(String doctorId);

    List<Appointment> findByServiceId(String serviceId);

    List<Appointment> findByDoctorIdAndServiceId(String doctorId, String serviceId);

    // Սա պետք է Controller-ում բաժանմունքով (մի քանի service-ներով) ֆիլտրելու համար
    List<Appointment> findByServiceIdIn(List<String> serviceIds);

    // 1. Գտնել ավարտված, բայց դեռ չգնահատված և բաց չթողնված այցերը
    List<Appointment> findByPatientIdAndStatusAndIsRatedFalseAndIsSkippedFalse(String patientId, String status);

    // 2. Հաշվել, թե քանի անգամ է այս պացիենտը եղել այս բժշկի մոտ (ստատուսը "COMPLETED")
    long countByPatientIdAndDoctorIdAndStatus(String patientId, String doctorId, String status);
    List<Appointment> findByStatusAndIsInvoicedFalse(String status);
    List<Appointment> findByStatusAndIsInvoicedFalseAndPatientId(String status, String patientId);

    boolean existsByDoctorIdAndPatientIdAndDate(String doctorId, String patientId, LocalDateTime date);//    Ֆիլտր ըստ բաժանմունքի
//    List<Appointment> findByDepartment(String department);
//
//    // Ֆիլտր երկուսը միասին
////    List<Appointment> findByDoctorIdAndDepartment(String doctorId, String department);
////    List<Appointment> findByServiceName(String serviceName);
//    List<Appointment> findByServiceIdIn(List<String> serviceIds);

}