package com.example.diplomathesisfinalcollection.department;

import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends MongoRepository<Department, String> {
    List<Department> findByActiveTrueOrderByNameAsc();

    Optional<Department> findByName(String name);
    List<Department> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String desc);
    List<Department> findByNameContainingIgnoreCase(String name);
    Optional<Department> findByCode(String code);

}