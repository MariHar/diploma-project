package com.example.diplomathesisfinalcollection.service;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ClinicServiceRepository extends MongoRepository<com.example.diplomathesisfinalcollection.service.ClinicService, String> {
    List<ClinicService> findByActiveTrueOrderByNameAsc();
    List<ClinicService> findByDepartmentIdAndActiveTrueOrderByNameAsc(String departmentId);
}