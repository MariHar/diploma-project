//package com.example.diplomathesisfinalcollection.team;
//
////import com.example.medicalweb.department.DepartmentRepository;
//import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/team")
//public class TeamController {
//
//    @Autowired
//    private TeamRepository teamRepository;
//
//    @Autowired
//    private DepartmentRepository departmentRepository;
//
////    @Autowired
////    private TeamService teamService;
//
//
//    @GetMapping
//    public String listTeams(Model model) {
//
//        List<Team> teams = teamRepository.findAllByOrderByNameAsc();
//        model.addAttribute("teams", teams);
//        return "team";
//    }
//
//    @GetMapping("/search")
//    public String searchTeams(
//            @RequestParam(value = "name", required = false) String name,
//            @RequestParam(value = "department", required = false) String department,
//            @RequestParam(value = "age", required = false) Integer age,
//            @RequestParam(value = "sort", required = false, defaultValue = "asc") String sort,
//            Model model) {
//
//        boolean ascending = "asc".equalsIgnoreCase(sort);
//        String searchName = (name != null) ? name : "";
//        String searchDept = (department != null) ? department : "";
//        String sortDirection = ascending ? "{ 'name' : 1 }" : "{ 'name' : -1 }";
//
//        List<Team> teams;
//
//        if (age != null) {
//            teams = teamRepository.searchWithAge(searchName, searchDept, age, sortDirection);
//        } else {
//            teams = teamRepository.searchWithoutAge(searchName, searchDept, sortDirection);
//        }
//
//        model.addAttribute("teams", teams);
//
//        model.addAttribute("name", name != null ? name : "");
//        model.addAttribute("department", department != null ? department : "");
//        model.addAttribute("age", age);
//        model.addAttribute("sort", sort != null ? sort : "asc");
//        return "doctors";
//    }
//
//    @GetMapping("/{id}")
//    public String viewTeamMember(@PathVariable String id, Model model) {
//        Team member = teamRepository.findById(id).orElse(null);
//        if (member == null) {
//            return "redirect:/team";
//        }
//        model.addAttribute("member", member);
//        return "teampage";
//    }
//
//}
