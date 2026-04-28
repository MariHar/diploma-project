package com.example.medicalweb.department;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DepartmentRepository extends MongoRepository<Department, String> {

    List<Department> findAllByOrderByNameAsc();
    List<Department> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
    List<Department> findAllByOrderByNameDesc();
    List<Department> findByNameContainingIgnoreCaseOrderByNameDesc(String name);

}
