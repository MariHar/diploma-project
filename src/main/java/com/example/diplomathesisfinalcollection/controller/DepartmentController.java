package com.example.diplomathesisfinalcollection.department;

import com.example.diplomathesisfinalcollection.service.ClinicService;
import com.example.diplomathesisfinalcollection.service.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // Show all departments
    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentRepository.findByActiveTrueOrderByNameAsc());
        return "departments"; // We will create departments.html later
    }

    // Show a specific department with its services and doctors!
    @GetMapping("/{id}")
    public String viewDepartment(@PathVariable String id, Model model) {
        Department department = departmentRepository.findById(id).orElse(null);

        if (department == null) {
            return "redirect:/departments";
        }

        List<ClinicService> services = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(id);
        List<Doctor> doctors = doctorRepository.findByDepartmentIdAndActiveTrue(id);

        model.addAttribute("department", department);
        model.addAttribute("services", services);
        model.addAttribute("doctors", doctors);

        return "department-details"; // We will create department-details.html later
    }
}