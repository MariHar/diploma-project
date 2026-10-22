package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired private ClinicServiceRepository clinicServiceRepository;


    @GetMapping
    public String manageServices(Model model) {
        model.addAttribute("departments", departmentRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("services", clinicServiceRepository.findAll());
        model.addAttribute("newService", new ClinicService());
        return "admin/services";
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute ClinicService clinicService) {
        clinicService.setActive(true);
        clinicServiceRepository.save(clinicService);
        return "redirect:/admin/services";
    }

    @PostMapping("/toggle/{id}")
    public String toggleService(@PathVariable String id) {
        clinicServiceRepository.findById(id).ifPresent(service -> {
            service.setActive(!service.isActive());
            clinicServiceRepository.save(service);
        });
        return "redirect:/admin/services";
    }
    @GetMapping("/edit/{id}")
    public String editServicePage(@PathVariable String id, Model model) {
        ClinicService clinicService = clinicServiceRepository.findById(id).orElse(null);
        if (clinicService == null) return "redirect:/admin/services";

        model.addAttribute("service", clinicService);
        // Ուղարկում ենք ակտիվ բաժանմունքները՝ Dropdown-ի համար
        model.addAttribute("departments", departmentRepository.findByActiveTrueOrderByNameAsc());

        return "admin/service-edit";
    }

    // 2. Պահպանում է փոփոխված տվյալները
    @PostMapping("/update/{id}")
    public String updateService(@PathVariable String id, @ModelAttribute ClinicService updatedService) {
        clinicServiceRepository.findById(id).ifPresent(service -> {
            service.setName(updatedService.getName());
            service.setPrice(updatedService.getPrice());
            service.setDurationMinutes(updatedService.getDurationMinutes());
            service.setDescription(updatedService.getDescription());
            service.setDepartmentId(updatedService.getDepartmentId());
            // Status-ը (active) չենք դիպչում
            clinicServiceRepository.save(service);
        });
        return "redirect:/admin/services";
    }
}
