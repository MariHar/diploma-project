//package com.example.diplomathesisfinalcollection.security;
//
//
//import com.example.diplomathesisfinalcollection.department.Department;
//import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
//import com.example.diplomathesisfinalcollection.doctoruser.DoctorUser;
//import com.example.diplomathesisfinalcollection.doctoruser.DoctorUserRepository;
//import com.example.diplomathesisfinalcollection.team.Team;
//import com.example.diplomathesisfinalcollection.team.TeamRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin")
//public class AdminController {
//
//    @Autowired
//    private DepartmentRepository departmentRepository;
//
//    @Autowired
//    private TeamRepository teamRepository;
//
//    @Autowired
//    private DoctorUserRepository userRepository;
//
//    @GetMapping
//    public String adminLogin() {
//        return "admin";
//    }
//
//
//    @PostMapping
//    public String handleAdminLogin(@RequestParam String username,
//                                   @RequestParam String password,
//                                   Model model) {
//        if ("admin".equals(username) && "admin123".equals(password)) {
//            model.addAttribute("adminName", username);
//            return "admindashboard";
//        } else {
//            model.addAttribute("error", "Սխալ լոգին կամ գաղտնաբառ");
//            return "admin";
//        }
//    }
//
//    @GetMapping("dashboard")
//    public String adminDashboard() {
//        return "admindashboard";
//    }
//
//
//    @GetMapping("/team")
//    public String adminTeam(Model model) {
//        model.addAttribute("teams", teamRepository.findAll());
//        return "adminteam";
//    }
//
//    @GetMapping("/newdoctoruser")
//    public String newDoctorUser() {
//        return "newuserform";
//    }
//
//    @GetMapping("/new/{teamId}")
//    public String newUserForm(@PathVariable String teamId, Model model) {
//        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new RuntimeException("Team member not found"));
//        model.addAttribute("team", team);
//        model.addAttribute("user", new DoctorUser());
//        return "newuserform";
//    }
//
//    @PostMapping("team/save/{teamId}")
//    public String saveUser(
//            @PathVariable String teamId,
//            @ModelAttribute DoctorUser user
//    ) {
//        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new RuntimeException("Team member not found"));
//
//        DoctorUser savedUser = userRepository.save(user);
//
//        team.setDoctorUserAccount(savedUser);
//        teamRepository.save(team);
//
//        return "redirect:/admin/team";
//    }
//
//
//    @GetMapping("/team/edit/{id}")
//    public String editTeam(@PathVariable String id, Model model) {
//        Team team = teamRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Team member not found with "));
//        if (team == null) return "redirect:/admin/" +
//                "team";
//        List<Department> departments = departmentRepository.findAll();
//        model.addAttribute("team", team);
//        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());
//        return "newteam";
//    }
//
//
//    @GetMapping("/team/delete/{id}")
//    public String deleteTeam(@PathVariable String id) {
//        teamRepository.deleteById(id);
//        return "redirect:/admin/team";
//    }
//
//    @GetMapping("/team/new")
//    public String newTeam(Model model) {
//        model.addAttribute("team", new Team());
//        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());
//        return "newteam";
//    }
//
//    @PostMapping("/team/save")
//    public String saveTeam(@ModelAttribute Team team,
//                           @RequestParam("photo") MultipartFile file) throws IOException {
//
//        if (team.getDepartment() != null && team.getDepartment().getId() != null) {
//            Department dep = departmentRepository.findById(team.getDepartment().getId())
//                    .orElse(null);
//            team.setDepartment(dep);
//            if (dep != null) team.setDepartmentName(dep.getName());
//        }
//        if (file != null && !file.isEmpty()) {
//            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
//            String uploadDir = "src/main/resources/static/images/";
//
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//            try (InputStream inputStream = file.getInputStream()) {
//                Path filePath = uploadPath.resolve(fileName);
//                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
//
//                team.setPhotoUrl("/images/" + fileName);
//            }
//        }
//
//        if (team.getId() != null && team.getId().trim().isEmpty()) {
//            team.setId(null);
//        }
//
//
//        Team savedTeam = teamRepository.save(team);
//        System.out.println("Saved Team ID: " + savedTeam.getId());
//        System.out.println("Saved Photo URL: " + savedTeam.getPhotoUrl());
//
//
//        return "redirect:/admin/team";
//    }
//
//
//    @GetMapping("/departments")
//    public String listDepartments(Model model) {
//        model.addAttribute("department", departmentRepository.findAll());
//        return "admindepartments";
//    }
//
//
//    @GetMapping("/departmentnew")
//    public String newDepartment(Model model) {
//        model.addAttribute("department", new Department());
//        return "departmentform";
//    }
//
//    @PostMapping("/save/{departmenid}")
//    public String saveDepartment(Department department,
//                                 @RequestParam(required = false) List<String> services) {
//        department.setServices(services);
//        departmentRepository.save(department);
//        return "redirect:/admin/departments";
//    }
//
//
//    @GetMapping("/department/delete/{id}")
//    public String deleteDepartment(@PathVariable String id) {
//        departmentRepository.deleteById(id);
//        return "redirect:/admin/departments";
//    }
//
//
//    @GetMapping("/department/edit/{id}")
//    public String editDepartment(@PathVariable String id, Model model) {
//        Department department = departmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Department not found"));
//        model.addAttribute("department", department);
//        return "departmentform";
//    }
//
//}
