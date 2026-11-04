package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.services.CloudinarySecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/departments")
public class AdminDepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired private CloudinarySecurityService cloudinaryService;

    // 1. LIST PAGE
    @GetMapping
    public String manageDepartments(Model model) {
        List<Department> departments = departmentRepository.findAll();
        // Generate URLs for previews in the table
        for (Department d : departments) {
            d.getImages().forEach(img -> img.setUrl(cloudinaryService.generateSignedUrl(img.getPublicId())));
        }
        model.addAttribute("departments", departments);
        model.addAttribute("newDepartment", new Department()); // Bound to th:object="${newDepartment}" in the template
        return "admin/departments";
    }

    // 2. SAVE NEW (supports multiple image uploads)
    @PostMapping("/save")
    public String saveDepartment(@ModelAttribute Department department,
                                 @RequestParam("imageFiles") MultipartFile[] files) throws IOException {
        department.setActive(true);
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String publicId = cloudinaryService.uploadSecureFile(file);
                department.getImages().add(new Department.DepartmentImage(publicId, true, null));
            }
        }
        departmentRepository.save(department);
        return "redirect:/admin/departments";
    }

    // 3. EDIT PAGE
    @GetMapping("/edit/{id}")
    public String editDepartmentPage(@PathVariable String id, Model model) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) return "redirect:/admin/departments";

        // Generate the links for existing images before showing the page
        for (Department.DepartmentImage img : department.getImages()) {
            img.setUrl(cloudinaryService.generateSignedUrl(img.getPublicId()));
        }

        model.addAttribute("department", department);
        return "admin/department-edit";
    }

    @PostMapping("/toggle/{id}")
    public String toggleDoctor(@PathVariable String id) {
        departmentRepository.findById(id).ifPresent(dept -> {
            dept.setActive(!dept.isActive());
            departmentRepository.save(dept);
        });
        return "redirect:/admin/departments";
    }

    // 4. UPDATE (appends new photos)
    @PostMapping("/update/{id}")
    public String updateDepartment(@PathVariable String id,
                                   @ModelAttribute Department updatedDept,
                                   @RequestParam("imageFiles") MultipartFile[] files) throws IOException {
        departmentRepository.findById(id).ifPresent(dept -> {
            dept.setName(updatedDept.getName());
            dept.setDescription(updatedDept.getDescription());
            dept.setLocation(updatedDept.getLocation());
            dept.setContactPhone(updatedDept.getContactPhone());


            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String publicId = cloudinaryService.uploadSecureFile(file);
                        dept.getImages().add(new Department.DepartmentImage(publicId, true, null));
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }

            departmentRepository.save(dept);
        });
        return "redirect:/admin/departments/edit/" + id;
    }

    // 5. TOGGLE IMAGE STATUS
    @PostMapping("/{deptId}/image/toggle/{index}")
    public String toggleImage(@PathVariable String deptId, @PathVariable int index) {
        departmentRepository.findById(deptId).ifPresent(dept -> {
            if (index < dept.getImages().size()) {
                boolean status = dept.getImages().get(index).isActive();
                dept.getImages().get(index).setActive(!status);
                departmentRepository.save(dept);
            }
        });
        return "redirect:/admin/departments/edit/" + deptId;
    }

    // 6. DELETE IMAGE
    @PostMapping("/{deptId}/image/delete/{index}")
    public String deleteImage(@PathVariable String deptId, @PathVariable int index) {
        departmentRepository.findById(deptId).ifPresent(dept -> {
            if (index < dept.getImages().size()) {
                dept.getImages().remove(index);
                departmentRepository.save(dept);
            }
        });
        return "redirect:/admin/departments/edit/" + deptId;
    }
}