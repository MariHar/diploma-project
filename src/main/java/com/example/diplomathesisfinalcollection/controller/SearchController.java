package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final DoctorRepository doctorRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/search")
    public String globalSearch(@RequestParam(value = "q", required = false) String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            return "redirect:/"; // Եթե դատարկ են փնտրել, գցում ենք գլխավոր էջ
        }

        String keyword = query.trim();

        // Փնտրում ենք բոլոր 3 տեղերում միաժամանակ
        List<Doctor> doctors = doctorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword);
        List<ClinicService> services = clinicServiceRepository.findByNameContainingIgnoreCase(keyword);
        List<Department> departments = departmentRepository.findByNameContainingIgnoreCase(keyword);

        // Փոխանցում ենք ֆրոնտենդ
        model.addAttribute("query", keyword);
        model.addAttribute("doctors", doctors);
        model.addAttribute("services", services);
        model.addAttribute("departments", departments);

        // Հաշվում ենք ընդհանուր արդյունքների քանակը
        int totalResults = doctors.size() + services.size() + departments.size();
        model.addAttribute("totalResults", totalResults);

        return "search-results"; // Նոր էջը
    }
}