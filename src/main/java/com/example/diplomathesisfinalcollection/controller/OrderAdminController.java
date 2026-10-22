package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.medicine.MedicineRepository;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrder;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrderRepository;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pharmacy-admin/orders")
public class OrderAdminController {

    @Autowired
    private PharmacyOrderRepository orderRepository;
    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // 1. ԿԱՆԲԱՆ ԳՐԱՏԱԽՏԱԿ
    // ==========================================
    @GetMapping
    public String getOrdersDashboard(Model model) {
        // 1. Վերցնում ենք բազայի օրիգինալ օբյեկտները
        List<PharmacyOrder> paid = orderRepository.findByStatusOrderByOrderDateDesc("PAID");
        List<PharmacyOrder> processing = orderRepository.findByStatusOrderByOrderDateDesc("PROCESSING");
        List<PharmacyOrder> outForDelivery = orderRepository.findByStatusOrderByOrderDateDesc("OUT_FOR_DELIVERY");
        List<PharmacyOrder> delivered = orderRepository.findByStatusOrderByOrderDateDesc("DELIVERED");

        // 2. Փոխակերպում ենք DTO-ների, որոնք արդեն ունեն patientName դաշտը
        model.addAttribute("pendingOrders", paid.stream().map(this::convertToDisplayDto).collect(Collectors.toList()));
        model.addAttribute("processingOrders", processing.stream().map(this::convertToDisplayDto).collect(Collectors.toList()));
        model.addAttribute("outForDeliveryOrders", outForDelivery.stream().map(this::convertToDisplayDto).collect(Collectors.toList()));
        model.addAttribute("deliveredOrders", delivered.stream().map(this::convertToDisplayDto).collect(Collectors.toList()));

        return "pharmacyadmin/pharmacy-orders-kanban";
    }
    private OrderDisplayDTO convertToDisplayDto(PharmacyOrder order) {
        OrderDisplayDTO dto = new OrderDisplayDTO();
        dto.setId(order.getId());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setDeliveryPhone(order.getDeliveryPhone());
        dto.setPurchasedDrugs(order.getPurchasedDrugs());
        dto.setTotalAmount(order.getTotalAmount());

        // Վերցնում ենք բժշկի տվյալները ուղիղ PharmacyOrder-ից
        dto.setPrescribingDoctor(order.getPrescribingDoctor() != null ? order.getPrescribingDoctor() : "-");
        dto.setDoctorSpecialization(order.getDoctorSpecialization() != null ? order.getDoctorSpecialization() : "-");

        // 🔴 Պացիենտի անունը վերցնում ենք userRepository-ով՝ օգտագործելով patientId-ն
        String pName = userRepository.findById(order.getPatientId())
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Անհայտ Պացիենտ");
        dto.setPatientName(pName);

        return dto;
    }
    // ==========================================
    // 2. ՍՏԱՏՈՒՍԻ ՓՈՓՈԽՈՒՄ ԵՎ ՊԱՀԵՍՏԻ ՆՎԱԶԵՑՈՒՄ
    // ==========================================
    @PostMapping("/{id}/change-status")
    public String changeStatus(@PathVariable String id, @RequestParam String newStatus) {
        PharmacyOrder order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(newStatus);
            if ("DELIVERED".equals(newStatus)) {
                reduceInventory(order);
            }
            orderRepository.save(order);
        }
        return "redirect:/pharmacy-admin/orders";
    }

    private void reduceInventory(PharmacyOrder order) {
        if (order.getPurchasedDrugs() == null) return;
        for (String drugInfo : order.getPurchasedDrugs()) {
            try {
                String[] priceParts = drugInfo.split(":");
                String nameAndQty = priceParts[0].trim();
                int lastIndex = nameAndQty.lastIndexOf(" x");
                if (lastIndex != -1) {
                    String name = nameAndQty.substring(0, lastIndex).trim();
                    int qtyToSubtract = Integer.parseInt(nameAndQty.substring(lastIndex + 2).trim());

                    medicineRepository.findByNameIgnoreCase(name).ifPresent(med -> {
                        int newStock = med.getStock() - qtyToSubtract;
                        med.setStock(Math.max(newStock, 0));
                        medicineRepository.save(med);
                    });
                }
            } catch (Exception e) {
                System.out.println("Error reducing stock for: " + drugInfo);
            }
        }
    }

    // ==========================================
    // 3. ՎԱՃԱՌՔՆԵՐԻ ՊԱՏՄՈՒԹՅՈՒՆ
    // ==========================================
    @GetMapping("/sales/history")
    public String getSalesHistory(Model model) {

        List<PharmacyOrder> allOrders = orderRepository.findAll();
        System.out.println("====== ԲԱԶԱՅԻՑ ԳՏԱՎ ԱՅՍՔԱՆ ՊԱՏՎԵՐ: " + allOrders.size() + " ======");
//        List<PharmacyOrder> allOrders = orderRepository.findAll();

        // 🔴 ԼՈՒԾՈՒՄԸ. Եկամուտ ենք համարում ՄԻԱՅՆ այն պատվերները, որոնք հաստատվել են (PAID)
        // կամ գտնվում են առաքման փուլերում: PENDING-ը ավտոմատ դուրս է մնում:
        List<String> validStatuses = Arrays.asList("PAID", "PROCESSING", "OUT_FOR_DELIVERY", "DELIVERED");

        List<PharmacyOrder> successfulOrders = allOrders.stream()
                .filter(o -> o.getStatus() != null && validStatuses.contains(o.getStatus()))
                .sorted((o1, o2) -> {
                    if (o1.getOrderDate() == null || o2.getOrderDate() == null) return 0;
                    return o2.getOrderDate().compareTo(o1.getOrderDate());
                })
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Yerevan"));
        double todayRevenue = 0.0;
        double totalRevenue = 0.0;
        List<SaleRecordDTO> sales = new ArrayList<>();

        for (PharmacyOrder order : successfulOrders) {
            totalRevenue += order.getTotalAmount();

            if (order.getOrderDate() != null && order.getOrderDate().toLocalDate().equals(today)) {
                todayRevenue += order.getTotalAmount();
            }

            SaleRecordDTO record = new SaleRecordDTO();
            record.setId(order.getId());
            record.setSaleDate(order.getOrderDate());
            record.setTotalAmount(order.getTotalAmount());
            record.setItems(new ArrayList<>());

            String pName = userRepository.findById(order.getPatientId())
                    .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Անհայտ Պացիենտ");
            record.setPatientName(pName);
            record.setDoctorName(order.getPrescribingDoctor() != null ? order.getPrescribingDoctor() : "-");
            record.setDepartment(order.getDoctorSpecialization() != null ? order.getDoctorSpecialization() : "-");

            if (order.getPurchasedDrugs() != null) {
                for (String drugInfo : order.getPurchasedDrugs()) {
                    try {
                        String[] priceParts = drugInfo.split(":");
                        double totalItemPrice = priceParts.length > 1 ? Double.parseDouble(priceParts[1].trim()) : 0.0;
                        String nameAndQty = priceParts[0].trim();
                        int lastIndex = nameAndQty.lastIndexOf(" x");

                        String name = lastIndex != -1 ? nameAndQty.substring(0, lastIndex).trim() : nameAndQty;
                        int qty = lastIndex != -1 ? Integer.parseInt(nameAndQty.substring(lastIndex + 2).trim()) : 1;
                        double unitPrice = qty > 0 ? (totalItemPrice / qty) : 0.0;

                        record.getItems().add(new SaleItemDTO(name, qty, unitPrice));
                    } catch (Exception e) {
                        record.getItems().add(new SaleItemDTO(drugInfo, 1, 0.0));
                    }
                }
            }
            sales.add(record);
        }

        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalSalesCount", successfulOrders.size());
        model.addAttribute("sales", sales);

        return "pharmacyadmin/pharmacy-sales-history";
    }

    // --- DTO ԴԱՍԵՐ ՖՐՈՆՏԵՆԴԻ ՀԱՄԱՐ ---
    @Data
    public static class SaleRecordDTO {
        private String id;
        private LocalDateTime saleDate;
        private List<SaleItemDTO> items;
        private Double totalAmount;
        private String patientName;private String doctorName;    // ՆՈՐ
        private String department;
    }

    @Data
    @AllArgsConstructor
    public static class SaleItemDTO {
        private String medicineName;
        private Integer quantity;
        private Double priceAtSale;
    }

    @Data
    public static class OrderDisplayDTO {
        private String id;
        private String patientName;
        private String prescribingDoctor;
        private String doctorSpecialization;
        private String deliveryAddress;
        private String deliveryPhone;
        private List<String> purchasedDrugs;
        private Double totalAmount;
    }
}