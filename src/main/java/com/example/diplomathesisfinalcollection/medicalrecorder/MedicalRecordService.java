//package com.example.diplomathesisfinalcollection.medicalrecorder;
//
//
////import com.clinic.dto.CardHashDto;
////import com.clinic.model.MedicalRecord;
////import com.clinic.repository.MedicalRecordRepository;
//import com.example.diplomathesisfinalcollection.dto.CardHashDTO;
//import com.example.diplomathesisfinalcollection.services.BlockchainService;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//
//@Service
//public class MedicalRecordService {
//
//    private final MedicalRecordRepository recordRepository;
//    private final BlockchainService blockchainService;
//
//    public MedicalRecordService(MedicalRecordRepository recordRepository, BlockchainService blockchainService) {
//        this.recordRepository = recordRepository;
//        this.blockchainService = blockchainService;
//    }
//
//    // Միայն պահպանում է բազայում (Սևագիր), Բլոկչեյնը ՉԻ աշխատում
//    public MedicalRecord saveDraft(MedicalRecord record) {
//        record.setStatus("DRAFT");
//        record.setLastModified(LocalDateTime.now());
//        return recordRepository.save(record);
//    }
//
//    // Պահպանում է և ակտիվացնում բլոկչեյնը (Հաստատում)
//    public MedicalRecord confirmRecord(String id, MedicalRecord updatedRecord) {
//        updatedRecord.setId(id);
//        updatedRecord.setStatus("CONFIRMED");
//        updatedRecord.setLastModified(LocalDateTime.now());
//
//        // 1. Պահպանում ենք հիմնական բազայում
//        MedicalRecord savedRecord = recordRepository.save(updatedRecord);
//
//        // 2. Հավաքում ենք միայն մաքուր DTO-ն հեշավորման համար
//        CardHashDTO hashDto = new CardHashDTO(
//                savedRecord.getSubjectiveExam(),
//                savedRecord.getObjectiveExam(),
//                savedRecord.getTreatment()
//        );
//
//        // 3. Ուղարկում ենք Բլոկչեյն Սերվիսին նոր բլոկ ստեղծելու
//        blockchainService.addBlock(savedRecord.getId(), hashDto);
//
//        return savedRecord;
//    }
//}
