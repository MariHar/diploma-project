package com.example.diplomathesisfinalcollection.verifiaction;


import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email); // To clean up old codes before generating a new one
}