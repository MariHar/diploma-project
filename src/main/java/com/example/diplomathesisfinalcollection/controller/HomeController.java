package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorSchedule;
import com.example.diplomathesisfinalcollection.doctor.DoctorScheduleRepository;
import com.example.diplomathesisfinalcollection.rating.Rating;
import com.example.diplomathesisfinalcollection.rating.RatingRepository;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private DoctorScheduleRepository scheduleRepository;


//    @GetMapping("/homevoice")
//    public String showVoicePage(){
//        return "homevoice";
//    }

    @GetMapping("/")
    public String index(Model model) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        List<Department> allDepartments = departmentRepository.findAll();
        List<Rating> allRatings = ratingRepository.findAll();

        // 1. Process Doctor Data (First 3 doctors + their unique ratings)
        List<Map<String, Object>> doctorData = new ArrayList<>();
        List<Doctor> threeDoctors = allDoctors.stream().limit(3).collect(Collectors.toList());

        for (Doctor doc : threeDoctors) {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("details", doc);

            // Calculate Average Rating for this specific doctor
            List<Rating> docRatings = allRatings.stream()
                    .filter(r -> doc.getId().equals(r.getDoctorId()))
                    .collect(Collectors.toList());

            double avg = docRatings.stream()
                    .mapToInt(Rating::getScore)
                    .average()
                    .orElse(5.0); // Default if no ratings

            docMap.put("avgRating", String.format("%.1f", avg));
            docMap.put("reviewCount", docRatings.size());
            doctorData.add(docMap);
        }

        // 2. Get first 4 departments
        List<Department> fourDepartments = allDepartments.stream().limit(4).collect(Collectors.toList());

        // 3. Calculate Specialist Counts for these 4 departments
        Map<String, Long> specialistCounts = new HashMap<>();
        for (Department dept : fourDepartments) {
            long count = allDoctors.stream()
                    .filter(doc -> dept.getId().equals(doc.getDepartmentId()))
                    .count();
            specialistCounts.put(dept.getId(), count);
        }

        // 4. Testimonials (Latest 3)
        List<Rating> latestRatings = allRatings.stream()
                .sorted(Comparator.comparing(Rating::getCreatedAt).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<Map<String, Object>> testimonials = new ArrayList<>();
        for (Rating r : latestRatings) {
            Map<String, Object> t = new HashMap<>();
            t.put("score", r.getScore());
            t.put("comment", r.getComment());

            if (r.isAnonymous()) {
                t.put("patientName", "Verified Patient");
            } else {
                User p = userRepository.findById(r.getPatientId()).orElse(null);
                t.put("patientName", p != null ? p.getFirstName() + " " + p.getLastName() : "Anonymous");
            }

            Doctor d = doctorRepository.findById(r.getDoctorId()).orElse(null);
            t.put("specialization", d != null ? d.getSpecialization() + " Patient" : "General Patient");
            testimonials.add(t);
        }

        // Add everything to model
        model.addAttribute("doctorData", doctorData); // Use this for doctors
        model.addAttribute("departments", fourDepartments);
        model.addAttribute("counts", specialistCounts);
        model.addAttribute("testimonials", testimonials);

        return "index";
    }


    @GetMapping("/about")
    public String about() {
        List<Rating> allRatings = ratingRepository.findAll();
        List<Rating> latestRatings = allRatings.stream()
                .sorted(Comparator.comparing(Rating::getCreatedAt).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<Map<String, Object>> testimonials = new ArrayList<>();
        for (Rating r : latestRatings) {
            Map<String, Object> t = new HashMap<>();
            t.put("score", r.getScore());
            t.put("comment", r.getComment());

            if (r.isAnonymous()) {
                t.put("patientName", "Verified Patient");
            } else {
                User p = userRepository.findById(r.getPatientId()).orElse(null);
                t.put("patientName", p != null ? p.getFirstName() + " " + p.getLastName() : "Anonymous");
            }

            Doctor d = doctorRepository.findById(r.getDoctorId()).orElse(null);
            t.put("specialization", d != null ? d.getSpecialization() + " Patient" : "General Patient");
            testimonials.add(t);
        }return "about";
    }

    @GetMapping("/service")
    public String service(Model model, @RequestParam(required = false) String dept) {
        // 1. Քաշում ենք միայն ԱԿՏԻՎ բաժանմունքները
        List<Department> allDepartments = departmentRepository.findAll().stream()
                .filter(Department::isActive)
                .collect(Collectors.toList());

        // 2. Քաշում ենք միայն ԱԿՏԻՎ ծառայությունները
        List<ClinicService> allServices = clinicServiceRepository.findAll().stream()
                .filter(ClinicService::isActive)
                .collect(Collectors.toList());

        // 3. Քաշում ենք միայն ԱԿՏԻՎ բժիշկներին (Մեզ պետք են Ծառայության Մանրամասների էջի համար)
        List<Doctor> allDoctors = doctorRepository.findAll().stream()
                .filter(Doctor::isActive)
                .collect(Collectors.toList());

        // Filter services if a department is selected (ստատիկ հղումով գալու դեպքում)
        if (dept != null && !dept.isEmpty()) {
            allServices = allServices.stream()
                    .filter(s -> dept.equals(s.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        Map<String, Boolean> bookableMap = allDepartments.stream()
                .collect(Collectors.toMap(Department::getId, Department::isPubliclyBookable));

        model.addAttribute("clinicServices", allServices);
        model.addAttribute("departments", allDepartments);
        model.addAttribute("selectedDept", dept);
        model.addAttribute("bookableMap", bookableMap);
        model.addAttribute("doctors", allDoctors); // <-- Բժիշկները JS-ի համար

        return "service";
    }

    @GetMapping("/gallery")
    public String gallery() {
        return "gallery";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

//    @GetMapping("/clinic/department")
//    public String departments() {
//        return "departments";
//    }
@GetMapping("/doctors")
public String doctorsPage(Model model, @RequestParam(required = false) String dept) {
    List<Doctor> allDoctors = doctorRepository.findAll();
    List<Department> allDepartments = departmentRepository.findAll();

    // If a department filter is clicked
    if (dept != null && !dept.isEmpty()) {
        allDoctors = allDoctors.stream()
                .filter(d -> d.getDepartmentId().equals(dept))
                .collect(Collectors.toList());
    }

    model.addAttribute("doctors", allDoctors);
    model.addAttribute("departments", allDepartments);
    model.addAttribute("selectedDept", dept); // To highlight the active button

    return "doctors"; // This should match your html file name
}

//    @GetMapping("/appointments")
//    public String appointments() {
//        return "appointment";
//    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/doctors/{id}")
    public String viewDoctorDetails(@PathVariable String id, Model model) {
        // 1. Fetch Doctor
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null) {
            return "redirect:/doctors";
        }

        // 2. Fetch Schedules
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(id);

        // 3. Calculate Real Rating
        List<Rating> ratings = ratingRepository.findByDoctorId(id);
        double avgRating = ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(5.0); // Default to 5.0 if no ratings yet
        int reviewCount = ratings.size();

        String doctorEmail = "N/A";
        if (doctor.getUserId() != null) {
            User user = userRepository.findById(doctor.getUserId()).orElse(null);
            if (user != null && user.getEmail() != null) {
                doctorEmail = user.getEmail();
            }
        }
        model.addAttribute("doctorEmail", doctorEmail);
        // 4. Add to Model
        model.addAttribute("doctor", doctor);
        model.addAttribute("schedules", schedules);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));
        model.addAttribute("reviewCount", reviewCount);

        // Calculate a realistic "Patients Treated" number based on reviews
        model.addAttribute("patientsTreated", reviewCount > 0 ? (reviewCount * 15) + "+" : "100+");

        return "doctor-details"; // Corresponds to doctor-details.html
    }




//    @GetMapping("/departments")
//    public String departments(Model model) {
//        return "departments";
//    }
}