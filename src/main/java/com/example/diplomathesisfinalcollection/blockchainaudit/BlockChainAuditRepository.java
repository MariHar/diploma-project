package com.example.diplomathesisfinalcollection.blockchainaudit;



import com.example.diplomathesisfinalcollection.blockchainaudit.BlockChainAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BlockChainAuditRepository extends MongoRepository<BlockChainAudit, String> {
    // Գտնում է տվյալ քարտի ամենավերջին հեշը (նախորդ հեշը վերցնելու համար)
    Optional<BlockChainAudit> findTopByRecordIdOrderByTimestampDesc(String recordId);
}
