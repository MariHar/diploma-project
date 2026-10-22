//package com.example.diplomathesisfinalcollection.doctor;
//
//import org.springframework.data.mongodb.repository.MongoRepository;
//import java.util.Optional;
//
//public interface DoctorProfileRepository extends MongoRepository<DoctorProfile, String> {
//    Optional<DoctorProfile> findByUserId(String userId);
//    boolean existsByUserId(String userId);
//}