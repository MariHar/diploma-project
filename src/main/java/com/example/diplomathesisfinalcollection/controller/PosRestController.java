package com.example.diplomathesisfinalcollection.controller;

//import com.example.diplomathesisfinalcollection.model.*;
//import com.example.diplomathesisfinalcollection.repository.MedicineRepository;
//import com.example.diplomathesisfinalcollection.repository.SaleRepository;
import com.example.diplomathesisfinalcollection.dto.CartItemRequest;
import com.example.diplomathesisfinalcollection.dto.CheckoutRequest;
import com.example.diplomathesisfinalcollection.medicine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos")
public class PosRestController {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private SaleRepository saleRepository;

    // 1. ՎԱՃԱՌՔԻ ԻՐԱԿԱՆԱՑՈՒՄ (FEFO ՏՐԱՄԱԲԱՆՈՒԹՅՈՒՆ)
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@RequestBody CheckoutRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Զամբյուղը դատարկ է:"));
        }

        Sale newSale = new Sale();
        newSale.setSaleDate(LocalDateTime.now(ZoneId.of("Asia/Yerevan")));
        newSale.setSellerUsername("Admin"); // Ապագայում կվերցնենք Spring Security-ից (SecurityContextHolder)

        double totalAmount = 0;
        List<SaleItem> saleItems = new ArrayList<>();
        List<Medicine> medicinesToUpdate = new ArrayList<>(); // Պահում ենք, որ վերջում պահպանենք բազայում

        // Անցնում ենք զամբյուղի յուրաքանչյուր ապրանքի վրայով
        for (CartItemRequest cartItem : request.getItems()) {
            int neededQuantity = cartItem.getQuantity();

            // Բերում ենք այս դեղի բոլոր ակտիվ խմբաքանակները՝ հին ժամկետները վերևում (FEFO)
            List<Medicine> availableBatches = medicineRepository.findByNameIgnoreCaseAndActiveTrueOrderByExpirationDateAsc(cartItem.getName());

            int totalAvailable = availableBatches.stream().mapToInt(Medicine::getStock).sum();

            if (totalAvailable < neededQuantity) {
                return ResponseEntity.badRequest().body(Map.of("error", "Պահեստում չկա բավարար քանակ: " + cartItem.getName() + " (Առկա է ընդամենը " + totalAvailable + " հատ)"));
            }

            // Սկսում ենք հանել քանակը հին խմբաքանակներից
            for (Medicine batch : availableBatches) {
                if (neededQuantity <= 0) break; // Եթե պահանջվող քանակն արդեն հավաքեցինք, դուրս ենք գալիս ցիկլից

                int availableInBatch = batch.getStock();
                int quantityToDeduct = Math.min(availableInBatch, neededQuantity);

                // Հանում ենք քանակը
                batch.setStock(availableInBatch - quantityToDeduct);

                // Եթե խմբաքանակը զրոյացավ, այն դարձնում ենք ոչ ակտիվ (Արխիվ)
                if (batch.getStock() == 0) {
                    batch.setActive(false);
                }

                medicinesToUpdate.add(batch);
                neededQuantity -= quantityToDeduct;

                // Գրանցում ենք Անդորրագրի մեջ
                SaleItem saleItem = new SaleItem();
                saleItem.setMedicineId(batch.getId());
                saleItem.setMedicineName(batch.getName());
                saleItem.setCategory(batch.getCategory());
                saleItem.setQuantity(quantityToDeduct);
                saleItem.setPriceAtSale(batch.getPrice());
                saleItem.setBatchExpirationDate(batch.getExpirationDate());

                saleItems.add(saleItem);
                totalAmount += (quantityToDeduct * batch.getPrice());
            }
        }

        // Եթե հասանք այստեղ, ուրեմն ամեն ինչ հերիքեց: Պահպանում ենք տվյալները:
        medicineRepository.saveAll(medicinesToUpdate); // Թարմացնում ենք դեղերի մնացորդները

        newSale.setItems(saleItems);
        newSale.setTotalAmount(totalAmount);
        saleRepository.save(newSale); // Պահպանում ենք անդորրագիրը

        return ResponseEntity.ok(Map.of("success", true, "message", "Վաճառքը հաջողությամբ գրանցվեց:", "saleId", newSale.getId()));
    }

    // 2. ՈՐՈՆՈՒՄ ԴՐԱՄԱՐԿՂՈՒՄ (POS) ԱՎԵԼԱՑՆԵԼՈՒ ՀԱՄԱՐ
    @GetMapping("/search")
    public ResponseEntity<?> searchMedicineForPOS(@RequestParam String query) {
        // Բերում ենք բոլոր ակտիվները, խմբավորում ենք ըստ անվան և հաշվում ընդհանուր քանակը
        List<Medicine> allMeds = medicineRepository.findAll().stream()
                .filter(m -> m.isActive() && m.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();

        // Այստեղ կարող ես վերադարձնել ցուցակը, որպեսզի JS-ը նկարի արդյունքները
        return ResponseEntity.ok(allMeds);
    }
}
