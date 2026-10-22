package com.example.diplomathesisfinalcollection.doctor;

import com.example.diplomathesisfinalcollection.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends MongoRepository<Doctor, String> {
    List<Doctor> findByActiveTrue();
    List<Doctor> findByDepartmentIdAndActiveTrue(String departmentId);
    Optional<Doctor> findByUserId(String userId);
    // Փնտրում է և՛ անվան, և՛ ազգանվան մեջ (անկախ մեծատառ/փոքրատառից)
    List<Doctor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}