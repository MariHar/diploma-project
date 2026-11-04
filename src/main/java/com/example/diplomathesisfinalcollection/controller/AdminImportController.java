package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.services.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class AdminImportController {

    private final ExcelImportService excelImportService;

    @PostMapping
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Խնդրում ենք ընտրել Excel ֆայլ:");
            return "redirect:/admin/dashboard";
        }

        try {
            excelImportService.importData(file);
            redirectAttributes.addFlashAttribute("successMsg", "Տվյալները հաջողությամբ ներմուծվեցին բազա:");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Սխալ ներմուծման ժամանակ. " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/departments") // Imports department data
    public String importDepartments(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please select an Excel file.");
            return "redirect:/admin/departments";
        }

        try {
            excelImportService.importData(file); // The service must handle department import logic
            redirectAttributes.addFlashAttribute("successMsg", "Data imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Import failed: " + e.getMessage());
        }

        return "redirect:/admin/departments";
    }

    @PostMapping("/bulk")
    public String handleBulkImport(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please select a valid Excel file.");
            return "redirect:/admin/departments";
        }

        try {
            excelImportService.importData(file);
            redirectAttributes.addFlashAttribute("successMsg", "Database successfully synchronized from Excel!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Import Error: " + e.getMessage());
        }

        // Redirect to departments to show the imported data
        return "redirect:/admin/departments";
    }
}