package com.example.diplomathesisfinalcollection.blockchain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainRecordController {

    private final BlockchainIntegrityService integrityService;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestParam("patientId") String patientId,
                                 @RequestParam("clinicalData") String clinicalData) {
        try {
            return ResponseEntity.ok(integrityService.addSecureRecord(patientId, clinicalData));
        } catch (BlockchainException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/validate/{patientId}")
    public ResponseEntity<Boolean> validate(@PathVariable("patientId") String patientId) {
        try {
            return ResponseEntity.ok(integrityService.isChainValid(patientId));
        } catch (BlockchainException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}