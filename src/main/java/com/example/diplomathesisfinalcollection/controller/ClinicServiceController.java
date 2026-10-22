package com.example.diplomathesisfinalcollection.controller;


import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/services")
public class ClinicServiceController {

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    @GetMapping
    public String listAllServices(Model model) {
        model.addAttribute("services", clinicServiceRepository.findAll());
        return "service"; // We will create services.html later
    }
}
