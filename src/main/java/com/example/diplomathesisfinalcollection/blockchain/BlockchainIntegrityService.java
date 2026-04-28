package com.example.diplomathesisfinalcollection.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainIntegrityService {

    private static final String GENESIS_PREVIOUS_HASH = "0";

    private final BlockchainRecordRepository repository;

    public BlockchainMedicalRecord addSecureRecord(String patientId, String clinicalData) {
        if (patientId == null || patientId.isBlank()) {
            throw new BlockchainException("patientId must not be null/blank");
        }
        if (clinicalData == null) {
            throw new BlockchainException("clinicalData must not be null");
        }

        Optional<BlockchainMedicalRecord> last = repository.findTopByPatientIdOrderByTimestampDesc(patientId);
        String previousHash = last.map(BlockchainMedicalRecord::getCurrentHash).orElse(GENESIS_PREVIOUS_HASH);

        LocalDateTime timestamp = LocalDateTime.now();
        String input = patientId + clinicalData + timestamp + previousHash;
        String currentHash = calculateSHA256(input);

        BlockchainMedicalRecord record = BlockchainMedicalRecord.builder()
                .patientId(patientId)
                .clinicalData(clinicalData)
                .timestamp(timestamp)
                .previousHash(previousHash)
                .currentHash(currentHash)
                .build();

        return repository.save(record);
    }

    public boolean isChainValid(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new BlockchainException("patientId must not be null/blank");
        }

        List<BlockchainMedicalRecord> records = repository.findAllByPatientIdOrderByTimestampAsc(patientId);
        if (records.isEmpty()) {
            return true;
        }

        for (int i = 0; i < records.size(); i++) {
            BlockchainMedicalRecord current = records.get(i);

            String expectedPreviousHash = (i == 0)
                    ? GENESIS_PREVIOUS_HASH
                    : records.get(i - 1).getCurrentHash();

            if (!Objects.equals(expectedPreviousHash, current.getPreviousHash())) {
                log.error("Blockchain link broken for patientId={}, index={}, expectedPreviousHash={}, actualPreviousHash={}",
                        patientId, i, expectedPreviousHash, current.getPreviousHash());
                return false;
            }

            String input = current.getPatientId()
                    + current.getClinicalData()
                    + current.getTimestamp()
                    + current.getPreviousHash();

            String recalculatedHash = calculateSHA256(input);

            if (!Objects.equals(recalculatedHash, current.getCurrentHash())) {
                log.error("Blockchain tampering detected for patientId={}, index={}, expectedHash={}, storedHash={}",
                        patientId, i, recalculatedHash, current.getCurrentHash());
                return false;
            }
        }

        return true;
    }

    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("SHA-256 algorithm not available", e);
        } catch (RuntimeException e) {
            throw new BlockchainException("Hashing failed", e);
        }
    }
}