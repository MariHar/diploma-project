package com.example.diplomathesisfinalcollection.medicine;


import com.example.diplomathesisfinalcollection.medicine.Sale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends MongoRepository<Sale, String> {
    // Ապագայում այստեղ կավելացնենք որոնում ըստ ամսաթվերի
}
