package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.services.CloudinarySecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private CloudinarySecurityService cloudinaryService;

    // Show all departments
    @GetMapping
    public String listDepartments(Model model) {
        List<Department> departments = departmentRepository.findAll();
        List<Doctor> allDoctors = doctorRepository.findAll();

        // Calculate specialist count for EACH department
        Map<String, Long> counts = new HashMap<>();


        for (Department dept : departments) {
            if ("Laboratory".equalsIgnoreCase(dept.getName()) || "Radiology".equalsIgnoreCase(dept.getName())) {
                continue;
            }
            long count = allDoctors.stream()
                    .filter(d -> dept.getId().equals(d.getDepartmentId()))
                    .count();
            counts.put(dept.getId(), count);
        }

        model.addAttribute("departments", departments);
        model.addAttribute("counts", counts);
        return "departments";
    }

    // Show a specific department with its services and doctors!
    @GetMapping("/{id}")
    public String viewDepartment(@PathVariable String id, Model model) {
        Department department = departmentRepository.findById(id).orElse(null);

        if (department == null) {
            return "redirect:/departments";
        }

        for (Department.DepartmentImage img : department.getImages()) {
            if (img.getUrl() != null && img.getUrl().startsWith("http")) {
                // It's already a full URL from Excel, do nothing
            } else {
                // It's a Cloudinary ID, generate the signed URL
                img.setUrl(cloudinaryService.generateSignedUrl(img.getPublicId()));
            }
        }

        if (department.getImages() != null) {
            department.getImages().forEach(img ->
                    img.setUrl(cloudinaryService.generateSignedUrl(img.getPublicId()))
            );
        }
        List<ClinicService> services = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(id);
        List<Doctor> doctors = doctorRepository.findByDepartmentIdAndActiveTrue(id);

        model.addAttribute("department", department);
        model.addAttribute("services", services);
        model.addAttribute("doctors", doctors);
        model.addAttribute("images", department.getImages());
        model.addAttribute("location",department.getLocation());

        return "departments-details"; // We will create department-details.html later
    }
}