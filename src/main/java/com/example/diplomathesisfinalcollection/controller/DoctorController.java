package com.example.diplomathesisfinalcollection.doctor;


import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public String listAllDoctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findByActiveTrue());
        return "doctors"; // We will create doctors.html later
    }

    @GetMapping("/{id}")
    public String viewDoctorProfile(@PathVariable String id, Model model) {
        Doctor doctor = doctorRepository.findById(id).orElse(null);

        if (doctor == null) {
            return "redirect:/doctors";
        }

        Department department = null;
        if (doctor.getDepartmentId() != null) {
            department = departmentRepository.findById(doctor.getDepartmentId()).orElse(null);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("department", department);

        return "doctor-details"; // We will create doctor-details.html later
    }
}
