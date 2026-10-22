package com.example.diplomathesisfinalcollection.controller;



import com.example.diplomathesisfinalcollection.medicine.Medicine;

import com.example.diplomathesisfinalcollection.medicine.PharmacyOrder;

import com.example.diplomathesisfinalcollection.medicine.MedicineRepository;

import com.example.diplomathesisfinalcollection.medicine.PharmacyOrderRepository;

import com.lowagie.text.Document;

import com.lowagie.text.Element;

import com.lowagie.text.FontFactory;

import com.lowagie.text.Paragraph;

import com.lowagie.text.pdf.PdfPTable;

import com.lowagie.text.pdf.PdfWriter;

//import com.openhtmltopdf.css.parser.property.PageSize;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;



import java.time.ZoneId;

import java.time.format.DateTimeFormatter;



import java.io.InputStream;

import java.time.LocalDate;

import java.util.ArrayList;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import java.util.stream.Collectors;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lowagie.text.Font;

import com.lowagie.text.PageSize;



@Controller

@RequestMapping("/pharmacy-admin")

@RequiredArgsConstructor

public class PharmacyAdminController {



    private final MedicineRepository medicineRepository;

    private final PharmacyOrderRepository pharmacyOrderRepository;



    @GetMapping("/dashboard")

    public String dashboard(Model model) {

        return "pharmacyadmin/dashboard";

    }



    @GetMapping("/pharmacy/dashboard")

    public String getAdminDashboard(

            Model model,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(required = false) String search,

            @RequestParam(required = false) String category,

            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startEntry,

            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endEntry,

            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startExp,

            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endExp,

            @RequestParam(defaultValue = "true") boolean active,

            @RequestParam(required = false) String quickFilter) { // ✅ ՆՈՐ ԴԱՇՏ ԿՈՃԱԿՆԵՐԻ ՀԱՄԱՐ



        List<Medicine> allMedicines = medicineRepository.findAll();

        int pageSize = 10;



// ✅ Ընդհանուր դեղերի ԻՐԱԿԱՆ քանակը (անկախ էջերից)

        long totalMedicinesCount = allMedicines.stream().filter(Medicine::isActive).count();



        List<String> categories = allMedicines.stream()

                .map(Medicine::getCategory).filter(Objects::nonNull).distinct().collect(Collectors.toList());



        List<Medicine> filtered = allMedicines.stream().filter(m -> {

            boolean matchesActive = (m.isActive() == active);

            boolean matchesSearch = (search == null || search.isEmpty()) || m.getName().toLowerCase().contains(search.toLowerCase());

            boolean matchesCat = (category == null || category.isEmpty()) || category.equals(m.getCategory());



            boolean matchesEntry = true;

            if (startEntry != null && m.getAddedDate() != null) matchesEntry = !m.getAddedDate().isBefore(startEntry);

            if (endEntry != null && m.getAddedDate() != null) matchesEntry = matchesEntry && !m.getAddedDate().isAfter(endEntry);



            boolean matchesExp = true;

            if (startExp != null && m.getExpirationDate() != null) matchesExp = !m.getExpirationDate().isBefore(startExp);

            if (endExp != null && m.getExpirationDate() != null) matchesExp = matchesExp && !m.getExpirationDate().isAfter(endExp);



            return matchesActive && matchesSearch && matchesCat && matchesEntry && matchesExp;

        }).collect(Collectors.toList());



// ✅ ԱՐԱԳ ՖԻԼՏՐԵՐ (Երբ սեղմում ենք վերևի քարտերը)

        if ("low".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getStock() != null && m.getStock() < 10).collect(Collectors.toList());

        } else if ("expiring".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getExpirationDate() != null && m.getExpirationDate().isBefore(LocalDate.now().plusDays(30))).collect(Collectors.toList());

        }



// Pagination

        int totalItems = filtered.size();

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int fromIndex = page * pageSize;

        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Medicine> pagedList = (fromIndex < totalItems) ? filtered.subList(fromIndex, toIndex) : new ArrayList<>();



// Քարտերի թվերը (միայն ակտիվների համար)

        long lowStock = allMedicines.stream().filter(m -> m.isActive() && m.getStock() != null && m.getStock() < 10).count();

        long expiring = allMedicines.stream().filter(m -> m.isActive() && m.getExpirationDate() != null &&

                m.getExpirationDate().isBefore(LocalDate.now().plusDays(30))).count();



        model.addAttribute("medicines", pagedList);

        model.addAttribute("categories", categories);

        model.addAttribute("lowStockCount", lowStock);

        model.addAttribute("expiringCount", expiring);

        model.addAttribute("totalMedicinesCount", totalMedicinesCount); // ✅ Ուղարկում ենք ճիշտ թիվը

        model.addAttribute("newMedicine", new Medicine());

        model.addAttribute("currentPage", page);

        model.addAttribute("totalPages", totalPages);

        model.addAttribute("activeStatus", active);

        model.addAttribute("searchQ", search);

        model.addAttribute("selectedCat", category);

        model.addAttribute("quickFilter", quickFilter);



        return "pharmacyadmin/pharmacy-dashboard";

    }



// ✅ ՆՈՐ ՄԵԹՈԴ. Մեկ կոճակով մաքրում է արդեն ժամկետանց դեղերը

    @PostMapping("/medicines/archive-expired")

    public String archiveExpiredMedicines(RedirectAttributes redirectAttributes) {

        List<Medicine> allActive = medicineRepository.findAll().stream().filter(Medicine::isActive).collect(Collectors.toList());

        int archivedCount = 0;



        LocalDate today = LocalDate.now(ZoneId.of("Asia/Yerevan"));



        for (Medicine med : allActive) {

// Եթե ժամկետն անցել է (նախքան այսօրվա օրը)

            if (med.getExpirationDate() != null && med.getExpirationDate().isBefore(today)) {

                med.setActive(false);

                medicineRepository.save(med);

                archivedCount++;

            }

        }



        if (archivedCount > 0) {

            redirectAttributes.addFlashAttribute("successMsg", "Հաջողությամբ արխիվացվեց " + archivedCount + " ժամկետանց խմբաքանակ:");

        } else {

            redirectAttributes.addFlashAttribute("errorMsg", "Այս պահին արդեն ժամկետանց դեղեր չկան:");

        }



        return "redirect:/pharmacy-admin/dashboard";

    }



    @PostMapping("/medicines/save")

    public String saveMedicine(@ModelAttribute Medicine medicine) {



// ՈՒՂՂՈՒՄ: Եթե ID-ն դատարկ տեքստ է, դարձնում ենք null, որ Mongo-ն ստեղծի իրենը

        if (medicine.getId() != null && medicine.getId().trim().isEmpty()) {

            medicine.setId(null);

        }



        if (medicine.getId() == null) {

// ՆՈՐ ԴԵՂԻ ՍՏԵՂԾՈՒՄ

// ՈՒՂՂՈՒՄ: Պարտադրում ենք Երևանի ժամային գոտին, որ ամսաթիվը հետ չընկնի

            medicine.setAddedDate(LocalDate.now(ZoneId.of("Asia/Yerevan")));

            medicine.setActive(true);

            medicineRepository.save(medicine);

        } else {

// ԽՄԲԱԳՐՈՒՄ

            medicineRepository.findById(medicine.getId()).ifPresent(existing -> {

                existing.setName(medicine.getName());

                existing.setCategory(medicine.getCategory());

                existing.setPrice(medicine.getPrice());

                existing.setStock(medicine.getStock());

                existing.setExpirationDate(medicine.getExpirationDate());

                existing.setDescription(medicine.getDescription());

// AddedDate և Active դաշտերին չենք կպնում

                medicineRepository.save(existing);

            });

        }

        return "redirect:/pharmacy-admin/pharmacy/dashboard";

    }



    @PostMapping("/medicines/toggle-status/{id}")

    public String toggleStatus(@PathVariable String id, HttpServletRequest request) {

        medicineRepository.findById(id).ifPresent(m -> {

            m.setActive(!m.isActive());

            medicineRepository.save(m);

        });

        String referer = request.getHeader("Referer");

        return "redirect:" + (referer != null ? referer : "/pharmacyadmin/pharmacy-dashboard");

    }



    @PostMapping("/medicines/add-stock/{id}")

    public String addStockToMedicine(@PathVariable String id, @RequestParam("addedStock") int addedStock, @RequestParam("newExpirationDate") LocalDate newExpirationDate) {

        medicineRepository.findById(id).ifPresent(med -> {

            med.setStock(med.getStock() + addedStock);

            med.setExpirationDate(newExpirationDate);

            medicineRepository.save(med);

        });

        return "redirect:/pharmacy-admin/dashboard";

    }





    @PostMapping("/medicines/upload-excel")

    public String uploadExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {

            redirectAttributes.addFlashAttribute("errorMsg", "Դուք չեք ընտրել որևէ ֆայլ:");

            return "redirect:/pharmacy-admin/pharmacy/dashboard";

        }



        DataFormatter formatter = new DataFormatter();



        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            int addedCount = 0;



            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; // Վերնագիրը բաց ենք թողնում



                String name = formatter.formatCellValue(row.getCell(0)).trim();

                if (name.isEmpty()) continue;



                String category = formatter.formatCellValue(row.getCell(1)).trim();



// Գին

                double price = 0;

                if (row.getCell(2) != null) {

                    try { price = Double.parseDouble(formatter.formatCellValue(row.getCell(2)).replace(",", ".")); }

                    catch (Exception e) { price = 0; }

                }



// Քանակ

                int excelStock = 0;

                if (row.getCell(3) != null) {

                    try { excelStock = Integer.parseInt(formatter.formatCellValue(row.getCell(3)).trim()); }

                    catch (Exception e) { excelStock = 0; }

                }



// Նկարագրություն

                String description = row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)).trim() : "";



// Ժամկետ

                LocalDate expDate = parseExcelDate(row.getCell(5), formatter);



// Մուտքի ամսաթիվ

                LocalDate entryDate = parseExcelDate(row.getCell(6), formatter);

                if (entryDate == null) entryDate = LocalDate.now(ZoneId.of("Asia/Yerevan"));



// Ստատուս

                boolean isActive = true;

                if (row.getCell(7) != null) {

                    String stat = formatter.formatCellValue(row.getCell(7)).trim().toLowerCase();

                    if (stat.equals("պասիվ") || stat.equals("ոչ") || stat.equals("false")) isActive = false;

                }



// Ստուգում ենք կա՞ նման խմբաքանակ

                List<Medicine> existingMeds = medicineRepository.findAllByNameIgnoreCase(name);

                Medicine medicineToSave = null;



                for (Medicine existing : existingMeds) {

                    if ((existing.getExpirationDate() == null && expDate == null) ||

                            (existing.getExpirationDate() != null && existing.getExpirationDate().equals(expDate))) {

                        medicineToSave = existing;

                        break;

                    }

                }



                if (medicineToSave == null) {

                    medicineToSave = new Medicine();

                    medicineToSave.setName(name);

                    medicineToSave.setCategory(category);

                    medicineToSave.setPrice(price);

                    medicineToSave.setStock(excelStock);

                    medicineToSave.setDescription(description);

                    medicineToSave.setExpirationDate(expDate);

                    medicineToSave.setAddedDate(entryDate);

                    medicineToSave.setActive(isActive);

                } else {

                    medicineToSave.setStock(medicineToSave.getStock() + excelStock);

                    medicineToSave.setPrice(price);

                    if (!description.isEmpty()) medicineToSave.setDescription(description);

                }



                medicineRepository.save(medicineToSave);

                addedCount++;

            }



// ՀԱՋՈՂՈՒԹՅԱՆ ՀԱՂՈՐԴԱԳՐՈՒԹՅՈՒՆ

            redirectAttributes.addFlashAttribute("successMsg", "Հաջողությամբ մշակվեց " + addedCount + " դեղ:");



        } catch (Exception e) {

            e.printStackTrace();

// ՍԽԱԼԻ ՀԱՂՈՐԴԱԳՐՈՒԹՅՈՒՆ (եթե ֆայլը սխալ ֆորմատի է)

            redirectAttributes.addFlashAttribute("errorMsg", "Սխալ ֆայլի բեռնման ժամանակ: Համոզվեք որ այն .xlsx ֆորմատի է: Սխալ. " + e.getMessage());

        }



        return "redirect:/pharmacy-admin/pharmacy/dashboard";

    }



// ✅ ԱՎԵԼԱՑՐՈՒ ԱՅՍ ՄԵԹՈԴԸ CONTROLLER-Ի ՄԵՋ

    private LocalDate parseExcelDate(Cell cell, DataFormatter formatter) {

        if (cell == null) return null;



        try {

// 1. Եթե Excel-ը այն ճանաչում է որպես ամսաթիվ

            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

                return cell.getLocalDateTimeCellValue().toLocalDate();

            }



// 2. Եթե Excel-ը այն պահել է որպես տեքստ (օր.՝ "20.10.2026")

            String dateStr = formatter.formatCellValue(cell).trim();

            if (dateStr.isEmpty()) return null;



// Ստանդարտացնում ենք՝ կետերն ու սլեշերը դարձնելով գծիկներ

            dateStr = dateStr.replace(".", "-").replace("/", "-");



// Փորձում ենք հնարավոր բոլոր ֆորմատներով

            String[] formats = {"yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy", "d-M-yyyy"};

            for (String f : formats) {

                try {

                    return LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern(f));

                } catch (Exception e) {

// Շարունակում ենք փորձել հաջորդ ֆորմատը

                }

            }

        } catch (Exception e) {

// Եթե ոչինչ չօգնեց

        }



        return null; // Եթե չկարողացավ կարդալ, կվերադարձնի null, այլ ոչ թե 6 ամիս հետոյի ամսաթիվ

    }



// @GetMapping("/statistics")

// public String showPharmacyStatistics(Model model, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String doctor, @RequestParam(required = false) String department, @RequestParam(required = false) String search) {

// List<PharmacyOrder> allOrders = pharmacyOrderRepository.findAll();

// List<PharmacyOrder> filteredOrders = allOrders.stream().filter(o -> {

// boolean matchesSearch = (search == null || search.isEmpty()) || (o.getPatientId() != null && o.getPatientId().contains(search));

// return matchesSearch;

// }).collect(Collectors.toList());

//

// List<PharmacyOrder> paidOrders = filteredOrders.stream().filter(o -> "PAID".equals(o.getStatus())).collect(Collectors.toList());

// double totalRevenue = paidOrders.stream().mapToDouble(PharmacyOrder::getTotalAmount).sum();

//

// model.addAttribute("orders", filteredOrders);

// model.addAttribute("totalRevenue", totalRevenue);

// model.addAttribute("totalSalesCount", paidOrders.size());

// model.addAttribute("allDoctors", allOrders.stream().map(PharmacyOrder::getPrescribingDoctor).filter(Objects::nonNull).distinct().collect(Collectors.toList()));

// return "pharmacy-statistics";

// }



    @GetMapping("/medicines/export/excel")

    public void exportToExcel(HttpServletResponse response,

                              @RequestParam(required = false) String search,

                              @RequestParam(required = false) String category,

                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startEntry,

                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endEntry,

                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startExp,

                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endExp,

                              @RequestParam(defaultValue = "true") boolean active,

                              @RequestParam(required = false) String quickFilter) throws Exception {



// 1. Վերցնում ենք բոլոր դեղերը

        List<Medicine> allMedicines = medicineRepository.findAll();



// 2. Ֆիլտրում ենք ՃԻՇՏ նույն ձևով, ինչ Dashboard-ում

        List<Medicine> filtered = allMedicines.stream().filter(m -> {

            boolean matchesActive = (m.isActive() == active);

            boolean matchesSearch = (search == null || search.isEmpty()) || m.getName().toLowerCase().contains(search.toLowerCase());

            boolean matchesCat = (category == null || category.isEmpty()) || category.equals(m.getCategory());



            boolean matchesEntry = true;

            if (startEntry != null && m.getAddedDate() != null) matchesEntry = !m.getAddedDate().isBefore(startEntry);

            if (endEntry != null && m.getAddedDate() != null) matchesEntry = matchesEntry && !m.getAddedDate().isAfter(endEntry);



            boolean matchesExp = true;

            if (startExp != null && m.getExpirationDate() != null) matchesExp = !m.getExpirationDate().isBefore(startExp);

            if (endExp != null && m.getExpirationDate() != null) matchesExp = matchesExp && !m.getExpirationDate().isAfter(endExp);



            return matchesActive && matchesSearch && matchesCat && matchesEntry && matchesExp;

        }).collect(Collectors.toList());



// Արագ ֆիլտրեր (քիչ քանակ / ժամկետանց)

        if ("low".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getStock() != null && m.getStock() < 10).collect(Collectors.toList());

        } else if ("expiring".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getExpirationDate() != null && m.getExpirationDate().isBefore(LocalDate.now().plusDays(30))).collect(Collectors.toList());

        }



// 3. Պատրաստում ենք Excel ֆայլը (Binary ֆորմատ, որ չինարեն չլինի)

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader("Content-Disposition", "attachment; filename=Pharmacy_Inventory.xlsx");



        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Պահեստ");



// Վերնագրեր

        Row headerRow = sheet.createRow(0);

        String[] columns = {"Անվանում", "Նկարագրություն", "Կատեգորիա", "Գին (֏)", "Քանակ", "Մուտքի Օր", "Ժամկետ", "Ակտիվ է"};

        for (int i = 0; i < columns.length; i++) {

            headerRow.createCell(i).setCellValue(columns[i]);

        }



// Լցնում ենք ֆիլտրված տվյալները (ԱՄԲՈՂՋԱԿԱՆ ՑՈՒՑԱԿԸ, ոչ թե միայն 1 էջը)

        int rowIdx = 1;

        for (Medicine med : filtered) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(med.getName());

            row.createCell(1).setCellValue(med.getDescription() != null ? med.getDescription() : "");

            row.createCell(2).setCellValue(med.getCategory());

            row.createCell(3).setCellValue(med.getPrice());

            row.createCell(4).setCellValue(med.getStock());

            row.createCell(5).setCellValue(med.getAddedDate() != null ? med.getAddedDate().toString() : "-");

            row.createCell(6).setCellValue(med.getExpirationDate() != null ? med.getExpirationDate().toString() : "-");

            row.createCell(7).setCellValue(med.isActive() ? "Այո" : "Ոչ (Արխիվ)");

        }



        workbook.write(response.getOutputStream());

        workbook.close();

    }

    @GetMapping("/medicines/export/pdf")

    public void exportToPDF(HttpServletResponse response,

                            @RequestParam(required = false) String search,

                            @RequestParam(required = false) String category,

                            @RequestParam(defaultValue = "true") boolean active,

                            @RequestParam(required = false) String quickFilter) throws Exception {



// 1. Վերցնում ենք ԲՈԼՈՐ ֆիլտրված տվյալները (նույն լոգիկան, ինչ Excel-ում)

        List<Medicine> allMedicines = medicineRepository.findAll();

        List<Medicine> filtered = allMedicines.stream().filter(m -> {

            boolean matchesActive = (m.isActive() == active);

            boolean matchesSearch = (search == null || search.isEmpty()) || m.getName().toLowerCase().contains(search.toLowerCase());

            boolean matchesCat = (category == null || category.isEmpty()) || category.equals(m.getCategory());

            return matchesActive && matchesSearch && matchesCat;

        }).collect(Collectors.toList());



        if ("low".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getStock() < 10).collect(Collectors.toList());

        }



// 2. Պատրաստում ենք PDF պատասխանը

        response.setContentType("application/pdf");

        response.setHeader("Content-Disposition", "attachment; filename=Pharmacy_Inventory.pdf");



        Document document = new Document(PageSize.A4);

        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();



// Վերնագիր

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

        Paragraph title = new Paragraph("Դեղերի Պահեստի Հաշվետվություն", fontTitle);

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        document.add(new Paragraph(" ")); // Դատարկ տող



// Աղյուսակ

        PdfPTable table = new PdfPTable(4); // 4 սյունակ

        table.setWidthPercentage(100);

        table.addCell("Անվանում");

        table.addCell("Կատեգորիա");

        table.addCell("Քանակ");

        table.addCell("Ժամկետ");



        for (Medicine med : filtered) {

            table.addCell(med.getName());

            table.addCell(med.getCategory());

            table.addCell(String.valueOf(med.getStock()));

            table.addCell(med.getExpirationDate() != null ? med.getExpirationDate().toString() : "-");

        }



        document.add(table);

        document.close();

    }

    @GetMapping("/medicines/print-preview")

    public String printPreview(Model model,

                               @RequestParam(required = false) String search,

                               @RequestParam(required = false) String category,

                               @RequestParam(defaultValue = "true") boolean active,

                               @RequestParam(required = false) String quickFilter) {



// Վերցնում ենք բոլորը

        List<Medicine> allMedicines = medicineRepository.findAll();



// Ֆիլտրում ենք (նույն լոգիկան)

        List<Medicine> filtered = allMedicines.stream().filter(m -> {

            boolean matchesActive = (m.isActive() == active);

            boolean matchesSearch = (search == null || search.isEmpty()) || m.getName().toLowerCase().contains(search.toLowerCase());

            boolean matchesCat = (category == null || category.isEmpty()) || category.equals(m.getCategory());

            return matchesActive && matchesSearch && matchesCat;

        }).collect(Collectors.toList());



        if ("low".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getStock() != null && m.getStock() < 10).collect(Collectors.toList());

        } else if ("expiring".equals(quickFilter)) {

            filtered = filtered.stream().filter(m -> m.getExpirationDate() != null && m.getExpirationDate().isBefore(LocalDate.now().plusDays(30))).collect(Collectors.toList());

        }



        model.addAttribute("medicines", filtered);

        model.addAttribute("printDate", LocalDate.now(ZoneId.of("Asia/Yerevan"))); // Այսօրվա ամսաթիվը



        return "pharmacyadmin/pharmacy-print-preview"; // Կբացի նոր HTML էջը

    }



// 🔴 1. ԴԱՇԲՈՐԴԻ ՄԵԹՈԴԸ (Թարմացված վիճակագրություն և ֆիլտրեր)

    @GetMapping("/statistics")

    public String showPharmacyStatistics(

            Model model,

            @RequestParam(required = false) String search,

            @RequestParam(required = false) String doctor,

            @RequestParam(required = false) String department,

            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {



// 1. Վերցնում ենք բոլոր պատվերները

        List<PharmacyOrder> allOrders = pharmacyOrderRepository.findAll();



// 2. Ֆիլտրում ենք ըստ դաշտերի

        List<PharmacyOrder> filteredOrders = allOrders.stream().filter(o -> {

            boolean matchesSearch = (search == null || search.trim().isEmpty()) ||

                    (o.getPatientId() != null && o.getPatientId().toLowerCase().contains(search.toLowerCase())) ||

                    (o.getId() != null && o.getId().toLowerCase().contains(search.toLowerCase()));



            boolean matchesDoc = (doctor == null || doctor.isEmpty()) || doctor.equals(o.getPrescribingDoctor());

            boolean matchesDept = (department == null || department.isEmpty()) || department.equals(o.getDoctorSpecialization());



            boolean matchesStart = (startDate == null) || (o.getOrderDate() != null && !o.getOrderDate().toLocalDate().isBefore(startDate));

            boolean matchesEnd = (endDate == null) || (o.getOrderDate() != null && !o.getOrderDate().toLocalDate().isAfter(endDate));



            return matchesSearch && matchesDoc && matchesDept && matchesStart && matchesEnd;

        }).collect(Collectors.toList());



// 3. Առանձնացնում ենք միայն ՎՃԱՐՎԱԾ (PAID) պատվերները՝ ֆինանսների և գրաֆիկների համար

        List<PharmacyOrder> paidOrders = filteredOrders.stream()

                .filter(o -> "PAID".equals(o.getStatus()))

                .collect(Collectors.toList());



        double totalRevenue = paidOrders.stream().mapToDouble(PharmacyOrder::getTotalAmount).sum();

        long totalSalesCount = paidOrders.size();



// 4. Dropdown-ների համար բոլոր եզակի բժիշկներն ու բաժանմունքները (ամբողջ բազայից)

        List<String> allDoctors = allOrders.stream()

                .map(PharmacyOrder::getPrescribingDoctor)

                .filter(Objects::nonNull).distinct().collect(Collectors.toList());



        List<String> allDepts = allOrders.stream()

                .map(PharmacyOrder::getDoctorSpecialization)

                .filter(Objects::nonNull).distinct().collect(Collectors.toList());



// 5. ԳՐԱՖԻԿՆԵՐԻ ՏՎՅԱԼՆԵՐ. Եկամուտ ըստ Բաժանմունքի (միայն PAID)

        Map<String, Double> deptRevenue = paidOrders.stream()

                .filter(o -> o.getDoctorSpecialization() != null && !o.getDoctorSpecialization().isEmpty())

                .collect(Collectors.groupingBy(

                        PharmacyOrder::getDoctorSpecialization,

                        Collectors.summingDouble(PharmacyOrder::getTotalAmount)

                ));

        List<String> deptLabels = new ArrayList<>(deptRevenue.keySet());

        List<Double> deptData = new ArrayList<>(deptRevenue.values());



// 6. ԳՐԱՖԻԿՆԵՐԻ ՏՎՅԱԼՆԵՐ. Եկամուտ ըստ Բժշկի (միայն PAID)

        Map<String, Double> docRevenue = paidOrders.stream()

                .filter(o -> o.getPrescribingDoctor() != null && !o.getPrescribingDoctor().isEmpty())

                .collect(Collectors.groupingBy(

                        PharmacyOrder::getPrescribingDoctor,

                        Collectors.summingDouble(PharmacyOrder::getTotalAmount)

                ));

        List<String> docLabels = new ArrayList<>(docRevenue.keySet());

        List<Double> docData = new ArrayList<>(docRevenue.values());



// 7. Տվյալների փոխանցում Model-ին

        model.addAttribute("orders", filteredOrders);

        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("totalSalesCount", totalSalesCount);

        model.addAttribute("allDoctors", allDoctors);

        model.addAttribute("allDepts", allDepts);



// Պահպանում ենք ֆիլտրի ընտրված արժեքները

        model.addAttribute("selectedDoctor", doctor);

        model.addAttribute("selectedDept", department);

        model.addAttribute("startDate", startDate);

        model.addAttribute("endDate", endDate);

        model.addAttribute("searchQuery", search);



// Գրաֆիկների փոխանցում

        model.addAttribute("deptLabels", deptLabels);

        model.addAttribute("deptData", deptData);

        model.addAttribute("docLabels", docLabels);

        model.addAttribute("docData", docData);



        return "pharmacyadmin/pharmacy-statistics";

    }



// 🔴 2. ՆՈՐ ՄԵԹՈԴ՝ Ֆինանսական Արտահանում Excel (Financial Report)

    @GetMapping("/statistics/export/excel")

    public void exportFinancialsToExcel(HttpServletResponse response,

                                        @RequestParam(required = false) String search,

                                        @RequestParam(required = false) String doctor,

                                        @RequestParam(required = false) String status,

                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) throws Exception {



        List<PharmacyOrder> allOrders = pharmacyOrderRepository.findAll();

        List<PharmacyOrder> filteredOrders = allOrders.stream().filter(o -> {

            boolean mSearch = (search == null || search.isEmpty()) || (o.getDeliveryPhone() != null && o.getDeliveryPhone().contains(search)) || (o.getPurchasedDrugs() != null && o.getPurchasedDrugs().toString().toLowerCase().contains(search.toLowerCase()));

            boolean mDoc = (doctor == null || doctor.isEmpty()) || doctor.equals(o.getPrescribingDoctor());

            boolean mStat = (status == null || status.isEmpty()) || status.equals(o.getStatus());

            boolean mStart = (startDate == null) || (o.getOrderDate() != null && !o.getOrderDate().toLocalDate().isBefore(startDate));

            boolean mEnd = (endDate == null) || (o.getOrderDate() != null && !o.getOrderDate().toLocalDate().isAfter(endDate));

            return mSearch && mDoc && mStat && mStart && mEnd;

        }).collect(Collectors.toList());



        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader("Content-Disposition", "attachment; filename=Pharmacy_Financial_Report.xlsx");



        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Ֆինանսներ");



        Row headerRow = sheet.createRow(0);

        String[] columns = {"Պատվերի ID", "Ամսաթիվ", "Հեռախոս", "Բժիշկ", "Դեղեր", "Գումար (֏)", "Ստատուս"};

        for (int i = 0; i < columns.length; i++) {

            headerRow.createCell(i).setCellValue(columns[i]);

        }



        int rowIdx = 1;

        for (PharmacyOrder order : filteredOrders) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(order.getId() != null ? order.getId().substring(order.getId().length() - 8) : "-");

            row.createCell(1).setCellValue(order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "-");

            row.createCell(2).setCellValue(order.getDeliveryPhone() != null ? order.getDeliveryPhone() : "-");

            row.createCell(3).setCellValue(order.getPrescribingDoctor() != null ? order.getPrescribingDoctor() : "-");

            row.createCell(4).setCellValue(order.getPurchasedDrugs() != null ? String.join(", ", order.getPurchasedDrugs()) : "-");

            row.createCell(5).setCellValue(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);

            row.createCell(6).setCellValue(order.getStatus() != null ? order.getStatus() : "-");

        }



        workbook.write(response.getOutputStream());

        workbook.close();

    }
}