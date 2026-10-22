package com.example.diplomathesisfinalcollection.laboratory;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IndicatorRepository extends MongoRepository<Indicator, String> {
    // Գտնում ենք բոլոր ցուցանիշները՝ ըստ անալիզի անվան (օր.՝ "Արյան ընդհանուր")
    List<Indicator> findByTestName(String testName);
    List<Indicator> findAllByActiveTrue();
}
