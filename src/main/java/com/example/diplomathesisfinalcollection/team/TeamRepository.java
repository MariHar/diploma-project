package com.example.medicalweb.team;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;
import com.example.medicalweb.user.User;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team, String> {
    List<Team> findAllByOrderByNameAsc();
    List<Team> findAllByOrderByNameDesc();

    @Query("{ 'name' : { $regex: ?0, $options: 'i' }, 'departmentName' : { $regex: ?1, $options: 'i' }, 'age' : ?2 }")
    List<Team> searchWithAge(String name, String department, int age, String sort);

    @Query("{ 'name' : { $regex: ?0, $options: 'i' }, 'departmentName' : { $regex: ?1, $options: 'i' } }")
    List<Team> searchWithoutAge(String name, String department, String sort);

}

