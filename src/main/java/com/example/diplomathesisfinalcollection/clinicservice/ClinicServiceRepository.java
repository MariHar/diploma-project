package com.example.diplomathesisfinalcollection.clinicservice;

import com.example.diplomathesisfinalcollection.department.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ClinicServiceRepository extends MongoRepository<com.example.diplomathesisfinalcollection.clinicservice.ClinicService, String> {
    List<ClinicService> findByActiveTrueOrderByNameAsc();
    List<ClinicService> findByDepartmentIdAndActiveTrueOrderByNameAsc(String departmentId);
    Optional<ClinicService> findByName(String name);
    List<ClinicService> findByNameContainingIgnoreCase(String name);
    Optional<ClinicService> findByCode(String code);

}