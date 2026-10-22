package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.medicine.Medicine;
import com.example.diplomathesisfinalcollection.medicine.MedicineRepository;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrder;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrderRepository;
import com.example.diplomathesisfinalcollection.services.PaymentService;
import com.example.diplomathesisfinalcollection.transaction.Transaction;
import com.example.diplomathesisfinalcollection.transaction.TransactionRepository;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineRepository medicineRepository;
    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;

    // 1. Prepare Order and Validate User
    @GetMapping("/checkout/{recordId}")
    public String checkout(@PathVariable String recordId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        List<PharmacyOrder> existingOrders = pharmacyOrderRepository.findByMedicalRecordIdOrderByOrderDateDesc(recordId);
        if (!existingOrders.isEmpty()) {
            PharmacyOrder latestOrder = existingOrders.get(0);
            if ("PENDING".equals(latestOrder.getStatus())) {
                model.addAttribute("order", latestOrder);
                return "checkout-payment";
            } else if ("PAID".equals(latestOrder.getStatus())) {
                return "redirect:/profile?error=already_paid";
            }
        }

        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        MedicalRecord record = medicalRecordRepository.findById(recordId).orElse(null);

        if (patient == null || record == null || record.getPrescriptions() == null || record.getPrescriptions().isEmpty()) {
            return "redirect:/profile?error=invalid_request";
        }

        if (patient.getAddress() == null || patient.getAddress().trim().isEmpty() ||
                patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            return "redirect:/profile?error=missing_delivery_info";
        }

        Doctor doctor = doctorRepository.findById(record.getDoctorId()).orElse(null);
        String docName = (doctor != null) ? doctor.getFirstName() + " " + doctor.getLastName() : "Անհայտ բժիշկ";
        String docSpec = (doctor != null) ? doctor.getSpecialization() : "Անհայտ";

        double totalAmount = 0.0;
        List<String> foundDrugs = new ArrayList<>();

        for (var prescription : record.getPrescriptions()) {
            String searchName = prescription.getName().trim();
            Medicine medicine = medicineRepository.findByNameIgnoreCase(searchName).orElse(null);

            if (medicine != null) {
                int qty = (prescription.getQuantity() != null && prescription.getQuantity() > 0) ? prescription.getQuantity() : 1;

                // 🔴 ՓՈՓՈԽՎԱԾ ՀԱՏՎԱԾ. Հաշվում ենք տվյալ դեղի ընդհանուր գինը և պահպանում տեքստի մեջ
                double itemTotalPrice = medicine.getPrice() * qty;
                totalAmount += itemTotalPrice;

                // Ֆորմատը կդառնա օրինակ՝ "Aspirin x2 :3000.0"
                foundDrugs.add(medicine.getName() + " x" + qty + " :" + itemTotalPrice);
            }
        }

        if (totalAmount == 0) {
            model.addAttribute("emptyStockMessage", "Նշված դեղերը առկա չեն պահեստում:");
            return "checkout-payment";
        }

        PharmacyOrder order = PharmacyOrder.builder()
                .patientId(patient.getId())
                .medicalRecordId(record.getId())
                .deliveryAddress(patient.getAddress())
                .deliveryPhone(patient.getPhone())
                .prescribingDoctor(docName)
                .doctorSpecialization(docSpec)
                .purchasedDrugs(foundDrugs)
                .totalAmount(totalAmount)
                .status("PENDING")
                .orderDate(LocalDateTime.now())
                .build();

        pharmacyOrderRepository.save(order);
        model.addAttribute("order", order);
        return "checkout-payment";
    }

    // 2. Redirect to Stripe
    @PostMapping("/pay-stripe/{orderId}")
    public String initiateStripePayment(@PathVariable String orderId) throws StripeException {
        PharmacyOrder order = pharmacyOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String checkoutUrl = paymentService.createPharmacyCheckoutSession(order);
        return "redirect:" + checkoutUrl;
    }

    // 3. Handle Stripe Success
    @GetMapping("/payment-success")
    public String handleSuccessfulPayment(@RequestParam String orderId) {
        PharmacyOrder order = pharmacyOrderRepository.findById(orderId).orElseThrow();

        if ("PENDING".equals(order.getStatus())) {

            order.setStatus("PAID");
            pharmacyOrderRepository.save(order);

            // Reduce Warehouse Stock
            reduceStock(order);

            Transaction transaction = Transaction.builder()
                    .patientId(order.getPatientId())
                    .invoiceId(order.getId())
                    .itemType(Transaction.ItemType.PHARMACY_ORDER)
                    .amount(order.getTotalAmount())
                    .currency("AMD")
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .paymentGateway("Stripe")
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(transaction);
        }

        return "redirect:/profile";
    }

    // 4. Show Receipt
    @GetMapping("/receipt/{orderId}")
    public String viewReceipt(@PathVariable String orderId, Model model, Principal principal) {
        PharmacyOrder order = pharmacyOrderRepository.findById(orderId).orElse(null);
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);

        if (order == null || !"PAID".equals(order.getStatus()) || currentUser == null || !order.getPatientId().equals(currentUser.getId())) {
            return "redirect:/profile?error=access_denied";
        }

        model.addAttribute("order", order);
        model.addAttribute("patient", currentUser);
        return "pharmacy-receipt";
    }

    // Helper: Reduce Stock
    private void reduceStock(PharmacyOrder order) {
        // Regex: Matches [Medicine Name] [Space] [x] [Quantity] [Space] [:]
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.+)\\s+x(\\d+)\\s*:");

        for (String drugInfo : order.getPurchasedDrugs()) {
            try {
                java.util.regex.Matcher matcher = pattern.matcher(drugInfo);

                if (matcher.find()) {
                    String name = matcher.group(1).trim();
                    int qtyToSubtract = Integer.parseInt(matcher.group(2).trim());

                    medicineRepository.findByNameIgnoreCase(name).ifPresent(med -> {
                        int currentStock = (med.getStock() != null) ? med.getStock() : 0;
                        med.setStock(Math.max(0, currentStock - qtyToSubtract));
                        medicineRepository.save(med);
                    });
                }
            } catch (Exception e) {
                System.err.println("Critical: Failed to reduce stock for " + drugInfo);
            }
        }
    }
}