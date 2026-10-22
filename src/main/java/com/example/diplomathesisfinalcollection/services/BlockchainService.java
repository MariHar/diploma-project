package com.example.diplomathesisfinalcollection.services;

//import com.clinic.dto.CardHashDto;
//import com.clinic.model.BlockChainAudit;
//import com.clinic.repository.BlockchainAuditRepository;
//import com.clinic.util.HashUtil;
import com.example.diplomathesisfinalcollection.blockchainaudit.BlockChainAudit;
import com.example.diplomathesisfinalcollection.blockchainaudit.BlockChainAuditRepository;
import com.example.diplomathesisfinalcollection.dto.CardHashDTO;
import com.example.diplomathesisfinalcollection.dto.FieldDiff;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BlockchainService {

    private final BlockChainAuditRepository auditRepository;
    private final ObjectMapper objectMapper; // Spring-ի լռելյայն գործիքը JSON սարքելու համար

    // Փոխարինիր կոնստրուկտորը այս մեկով.
    public BlockchainService(BlockChainAuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper.copy().configure(
        com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public void addBlock(String recordId, CardHashDTO newData) {
        try {
            Optional<BlockChainAudit> lastBlockOptional =
                    auditRepository.findTopByRecordIdOrderByTimestampDesc(recordId);

            String previousHash;
            List<String> changes = new ArrayList<>();

            if (lastBlockOptional.isPresent()) {
                BlockChainAudit lastBlock = lastBlockOptional.get();
                previousHash = lastBlock.getHash();
                changes = compareChanges(lastBlock.getDataSnapshot(), newData);
            } else {
                previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
                changes.add("Առաջնային ստեղծում (Genesis Block)");
            }

            String jsonData = objectMapper.writeValueAsString(newData);
            String dataToHash = previousHash + "|" + jsonData;
            String newHash = HashUtil.applySha256(dataToHash);

            BlockChainAudit newAuditLog = new BlockChainAudit(recordId, newHash,
                    previousHash, newData, changes);
            auditRepository.save(newAuditLog);

        } catch (Exception e) {
            throw new RuntimeException("Բլոկչեյնի բլոկի ստեղծման սխալ", e);
        }
    }

    private List<String> compareChanges(CardHashDTO oldData, CardHashDTO newData) {
        List<String> diff = new ArrayList<>();

        if (oldData == null) return diff;

        // Ստուգում ենք բացարձակապես բոլոր կլինիկական դաշտերը
        if (!Objects.equals(oldData.complaints(), newData.complaints())) {
            diff.add("Գանգատներ");
        }
        if (!Objects.equals(oldData.anamnesisMorbi(), newData.anamnesisMorbi())) {
            diff.add("Հիվանդության պատմություն (Anamnesis Morbi)");
        }
        if (!Objects.equals(oldData.anamnesisVitae(), newData.anamnesisVitae())) {
            diff.add("Կյանքի պատմություն (Anamnesis Vitae)");
        }
        if (!Objects.equals(oldData.symptoms(), newData.symptoms())) {
            diff.add("Ախտանիշներ (Symptoms)");
        }
        if (!Objects.equals(oldData.diagnosis(), newData.diagnosis())) {
            diff.add("Ախտորոշում");
        }
        if (!Objects.equals(oldData.treatment(), newData.treatment())) {
            diff.add("Բուժում");
        }
        if (!Objects.equals(oldData.contraindications(), newData.contraindications())) {
            diff.add("Հակացուցումներ");
        }
        if (!Objects.equals(oldData.notes(), newData.notes())) {
            diff.add("Նշումներ");
        }
        if (!Objects.equals(oldData.prescriptions(), newData.prescriptions())) {
            diff.add("Դեղատոմսեր");
        }
//        if (!Objects.equals(oldData.vitals(), newData.vitals())) {
//            diff.add("Կենսական ցուցանիշներ (Vitals)");
//        }
        if (!Objects.equals(oldData.objectiveExamination(), newData.objectiveExamination())) {
            diff.add("Օբյեկտիվ զննում (Ընդհանուր)");
        }
        if (!Objects.equals(oldData.inspection(), newData.inspection())) {
            diff.add("Դիտում (Inspection)");
        }
        if (!Objects.equals(oldData.palpation(), newData.palpation())) {
            diff.add("Շոշափում (Palpation)");
        }
        if (!Objects.equals(oldData.percussion(), newData.percussion())) {
            diff.add("Թակոց (Percussion)");
        }
        if (!Objects.equals(oldData.auscultation(), newData.auscultation())) {
            diff.add("Լսում (Auscultation)");
        }
//        if (!Objects.equals(oldData.instrumentalExams(), newData.instrumentalExams())) {
//            diff.add("Գործիքային հետազոտություններ");
//        }

        if (diff.isEmpty()) {
            diff.add("Տեսանելի կլինիկական փոփոխություններ չեն հայտնաբերվել");
        }

        return diff;
    }

    public String verifyRecordIntegrity(String recordId, MedicalRecord currentRecordFromDb) {
        Optional<BlockChainAudit> lastBlockOptional = auditRepository.findTopByRecordIdOrderByTimestampDesc(recordId);

        if (lastBlockOptional.isEmpty()) {
            return "NO_BLOCKCHAIN_DATA";
        }

        BlockChainAudit lastBlock = lastBlockOptional.get();

        try {
            CardHashDTO currentDto = new CardHashDTO(
                    currentRecordFromDb.getComplaints(),
                    currentRecordFromDb.getAnamnesisMorbi(),
                    currentRecordFromDb.getAnamnesisVitae(),
                    currentRecordFromDb.getSymptoms(),
                    currentRecordFromDb.getDiagnosis(),
                    currentRecordFromDb.getTreatment(),
                    currentRecordFromDb.getContraindications(),
                    currentRecordFromDb.getNotes(),
                    currentRecordFromDb.getPrescriptions(),
                    currentRecordFromDb.getObjectiveExamination(),
                    currentRecordFromDb.getInspection(),
                    currentRecordFromDb.getPalpation(),
                    currentRecordFromDb.getPercussion(),
                    currentRecordFromDb.getAuscultation()
            );
            String jsonData = objectMapper.writeValueAsString(currentDto);
            String dataToHash = lastBlock.getPreviousHash() + "|" + jsonData;
            String recalculatedHash = HashUtil.applySha256(dataToHash);

            if (recalculatedHash.equals(lastBlock.getHash())) {
                return "VERIFIED";
            } else {
                return "TAMPERED";
            }

        } catch (Exception e) {
            throw new RuntimeException("Վերիֆիկացիայի սխալ", e);
        }
    }
    public List<String> detectManualChanges(String recordId, MedicalRecord currentRecordFromDb) {
        Optional<BlockChainAudit> lastBlockOptional = auditRepository.findTopByRecordIdOrderByTimestampDesc(recordId);

        if (lastBlockOptional.isEmpty()) return List.of("Բլոկչեյն տվյալներ չկան");

        BlockChainAudit lastBlock = lastBlockOptional.get();

        // Ստեղծում ենք DTO բազայի ներկա վիճակից
        CardHashDTO currentDtoFromDb = new CardHashDTO(
                currentRecordFromDb.getComplaints(),
                currentRecordFromDb.getAnamnesisMorbi(),
                currentRecordFromDb.getAnamnesisVitae(),
                currentRecordFromDb.getSymptoms(),
                currentRecordFromDb.getDiagnosis(),
                currentRecordFromDb.getTreatment(),
                currentRecordFromDb.getContraindications(),
                currentRecordFromDb.getNotes(),
                currentRecordFromDb.getPrescriptions(),
//                currentRecordFromDb.getVitals(),
                currentRecordFromDb.getObjectiveExamination(),
                currentRecordFromDb.getInspection(),
                currentRecordFromDb.getPalpation(),
                currentRecordFromDb.getPercussion(),
                currentRecordFromDb.getAuscultation()
//                currentRecordFromDb.getInstrumentalExams()
        );

        // Համեմատում ենք բազայի ներկա վիճակը բլոկչեյնում պահված վերջին Snapshot-ի հետ
        return compareChanges(lastBlock.getDataSnapshot(), currentDtoFromDb);
    }

    public List<FieldDiff> getDetailedDifferences(String recordId, MedicalRecord currentRecordFromDb) {
        Optional<BlockChainAudit> lastBlockOptional = auditRepository.findTopByRecordIdOrderByTimestampDesc(recordId);
        List<FieldDiff> diffs = new ArrayList<>();

        if (lastBlockOptional.isEmpty() || lastBlockOptional.get().getDataSnapshot() == null) {
            return diffs; // No blockchain data to compare against
        }

        // The safe, original data from the blockchain
        CardHashDTO oldData = lastBlockOptional.get().getDataSnapshot();

        // --- 1. Subjective & Anamnesis ---
        if (!Objects.equals(oldData.complaints(), currentRecordFromDb.getComplaints())) {
            diffs.add(new FieldDiff("Գանգատներ (Complaints)", String.valueOf(oldData.complaints()), String.valueOf(currentRecordFromDb.getComplaints())));
        }
        if (!Objects.equals(oldData.anamnesisMorbi(), currentRecordFromDb.getAnamnesisMorbi())) {
            diffs.add(new FieldDiff("Հիվանդության պատմություն (Anamnesis Morbi)", String.valueOf(oldData.anamnesisMorbi()), String.valueOf(currentRecordFromDb.getAnamnesisMorbi())));
        }
        if (!Objects.equals(oldData.anamnesisVitae(), currentRecordFromDb.getAnamnesisVitae())) {
            diffs.add(new FieldDiff("Կյանքի պատմություն (Anamnesis Vitae)", String.valueOf(oldData.anamnesisVitae()), String.valueOf(currentRecordFromDb.getAnamnesisVitae())));
        }
        if (!Objects.equals(oldData.symptoms(), currentRecordFromDb.getSymptoms())) {
            diffs.add(new FieldDiff("Ախտանիշներ (Symptoms)", String.valueOf(oldData.symptoms()), String.valueOf(currentRecordFromDb.getSymptoms())));
        }

        // --- 2. Objective Exam ---
        if (!Objects.equals(oldData.objectiveExamination(), currentRecordFromDb.getObjectiveExamination())) {
            diffs.add(new FieldDiff("Օբյեկտիվ Զննում (Ընդհանուր)", String.valueOf(oldData.objectiveExamination()), String.valueOf(currentRecordFromDb.getObjectiveExamination())));
        }
        if (!Objects.equals(oldData.inspection(), currentRecordFromDb.getInspection())) {
            diffs.add(new FieldDiff("Դիտում (Inspection)", String.valueOf(oldData.inspection()), String.valueOf(currentRecordFromDb.getInspection())));
        }
        if (!Objects.equals(oldData.palpation(), currentRecordFromDb.getPalpation())) {
            diffs.add(new FieldDiff("Շոշափում (Palpation)", String.valueOf(oldData.palpation()), String.valueOf(currentRecordFromDb.getPalpation())));
        }
        if (!Objects.equals(oldData.percussion(), currentRecordFromDb.getPercussion())) {
            diffs.add(new FieldDiff("Թակոց (Percussion)", String.valueOf(oldData.percussion()), String.valueOf(currentRecordFromDb.getPercussion())));
        }
        if (!Objects.equals(oldData.auscultation(), currentRecordFromDb.getAuscultation())) {
            diffs.add(new FieldDiff("Լսում (Auscultation)", String.valueOf(oldData.auscultation()), String.valueOf(currentRecordFromDb.getAuscultation())));
        }

        // --- 3. Assessment & Plan ---
        if (!Objects.equals(oldData.diagnosis(), currentRecordFromDb.getDiagnosis())) {
            diffs.add(new FieldDiff("Ախտորոշում (Diagnosis)", String.valueOf(oldData.diagnosis()), String.valueOf(currentRecordFromDb.getDiagnosis())));
        }
        if (!Objects.equals(oldData.treatment(), currentRecordFromDb.getTreatment())) {
            diffs.add(new FieldDiff("Բուժում (Treatment)", String.valueOf(oldData.treatment()), String.valueOf(currentRecordFromDb.getTreatment())));
        }
        if (!Objects.equals(oldData.contraindications(), currentRecordFromDb.getContraindications())) {
            diffs.add(new FieldDiff("Հակացուցումներ (Contraindications)", String.valueOf(oldData.contraindications()), String.valueOf(currentRecordFromDb.getContraindications())));
        }
        if (!Objects.equals(oldData.notes(), currentRecordFromDb.getNotes())) {
            diffs.add(new FieldDiff("Նշումներ (Notes)", String.valueOf(oldData.notes()), String.valueOf(currentRecordFromDb.getNotes())));
        }

        // --- 4. Complex Nested Objects (Vitals & Prescriptions) ---
        // Because you use Lombok @Data, String.valueOf() will neatly print Vitals(bp=120/80...)
//        if (!Objects.equals(oldData.vitals(), currentRecordFromDb.getVitals())) {
//            diffs.add(new FieldDiff("Կենսական Ցուցանիշներ (Vitals)", String.valueOf(oldData.vitals()), String.valueOf(currentRecordFromDb.getVitals())));
//        }
        if (!Objects.equals(oldData.prescriptions(), currentRecordFromDb.getPrescriptions())) {
            diffs.add(new FieldDiff("Դեղատոմսեր (Prescriptions)", String.valueOf(oldData.prescriptions()), String.valueOf(currentRecordFromDb.getPrescriptions())));
        }

        return diffs;
    }
}