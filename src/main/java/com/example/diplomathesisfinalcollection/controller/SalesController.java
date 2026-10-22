package com.example.diplomathesisfinalcollection.controller;


import com.example.diplomathesisfinalcollection.medicine.Sale;
import com.example.diplomathesisfinalcollection.medicine.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pharmacy-admin/sales")
public class SalesController {

    @Autowired
    private SaleRepository saleRepository;

//    @GetMapping("/history")
//    public String getSalesHistory(Model model) {
//        // Վերցնում ենք բոլոր վաճառքները (առայժմ առանց բարդ ֆիլտրերի, որ պարզ լինի)
//        // Իրականում պետք է սորտավորել ըստ ամսաթվի (նորերը վերևում)
//        List<Sale> allSales = saleRepository.findAll();
//
//        // Հաշվում ենք այսօրվա եկամուտը
//        LocalDate today = LocalDate.now();
//        double todayRevenue = allSales.stream()
//                .filter(s -> s.getSaleDate().toLocalDate().equals(today))
//                .mapToDouble(Sale::getTotalAmount)
//                .sum();
//
//        // Հաշվում ենք ընդհանուր եկամուտը
//        double totalRevenue = allSales.stream()
//                .mapToDouble(Sale::getTotalAmount)
//                .sum();
//
//        model.addAttribute("sales", allSales);
//        model.addAttribute("todayRevenue", todayRevenue);
//        model.addAttribute("totalRevenue", totalRevenue);
//        model.addAttribute("totalSalesCount", allSales.size());
//
//        return "pharmacy-sales-history"; // Սա մեր նոր HTML-ն է
//    }
}
