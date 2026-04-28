package com.example.medicalweb.user;


import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface UserRepository extends MongoRepository<User,Integer> {

        Optional<User> findByUsername(String username);
        Optional<User> findById(String id);

        String id(String id);

        Optional<User> findByEmail(String email);

}
