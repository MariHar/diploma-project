package com.example.diplomathesisfinalcollection.transaction;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Aggregation;
import java.util.List;
import java.util.Map;
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {


    // Inside TransactionRepository:
    // Replace the previous calculateTotalRevenue in TransactionRepository with this:

    @Aggregation(pipeline = {
            "{ '$match': { 'status': 'COMPLETED' } }",
            "{ '$group': { " +
                    "    '_id': null, " +
                    "    'totalRevenue': { '$sum': '$amount' }, " +
                    "    'transactionCount': { '$sum': 1 } " +
                    "} }"
    })
    Map<String, Object> getFinancialSummary();
}