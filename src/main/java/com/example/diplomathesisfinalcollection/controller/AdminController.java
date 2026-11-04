package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.appointment.AppointmentStatusUpdater;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorSchedule;
import com.example.diplomathesisfinalcollection.doctor.DoctorScheduleRepository;
import com.example.diplomathesisfinalcollection.dto.AppointmentDTO;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.laboratory.Indicator;
import com.example.diplomathesisfinalcollection.laboratory.IndicatorRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequest;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequestRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecord;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.services.BillingService;
import com.example.diplomathesisfinalcollection.services.CloudinarySecurityService;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.services.ExcelImportService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.example.diplomathesisfinalcollection.dto.DoctorRegistrationDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ClinicServiceRepository clinicServiceRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private EditRequestRepository editRequestRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private AppointmentStatusUpdater statusUpdater;
    @Autowired
    private IndicatorRepository indicatorRepository;
    @Autowired
    private ExcelImportService excelImportService;

    @GetMapping("/dashboard")
    public String adminDashboard() { return "admin/dashboard"; }

    @GetMapping("/appointments")
    public String showAppointmentsPage() {
        return "admin/appointments";
    }

        @ResponseBody
        @GetMapping("/api/appointments")
        public Map<String, Object> getAppointments(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(required = false) String departmentId,
                @RequestParam(required = false) String doctorId,
                @RequestParam(required = false) String serviceId,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                @RequestParam(required = false) String searchQuery,
                @RequestParam(required = false) String status) {

            List<Appointment> appointments = appointmentRepository.findAll();

            if (doctorId != null && !doctorId.isEmpty()) {
                appointments = appointments.stream().filter(a -> doctorId.equals(a.getDoctorId())).collect(Collectors.toList());
            }
            if (serviceId != null && !serviceId.isEmpty()) {
                appointments = appointments.stream().filter(a -> serviceId.equals(a.getServiceId())).collect(Collectors.toList());
            }
            if (departmentId != null && !departmentId.isEmpty()) {
                List<String> validServiceIds = clinicServiceRepository.findAll().stream()
                        .filter(s -> departmentId.equals(s.getDepartmentId()))
                        .map(ClinicService::getId)
                        .collect(Collectors.toList());
                appointments = appointments.stream().filter(a -> validServiceIds.contains(a.getServiceId())).collect(Collectors.toList());
            }
            if (startDate != null) {
                appointments = appointments.stream()
                        .filter(a -> a.getDate() != null && !a.getDate().isBefore(startDate))
                        .collect(Collectors.toList());
            }

             if (endDate != null) {
                appointments = appointments.stream()
                        .filter(a -> a.getDate() != null && !a.getDate().isAfter(endDate))
                        .collect(Collectors.toList());
            }


             List<AppointmentDTO> dtoList = new ArrayList<>();

            for (Appointment app : appointments) {
                  String pName = "Անհայտ Պացիենտ";
                if (app.getPatientId() != null) {
                    pName = userRepository.findById(app.getPatientId())
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Անհայտ Պացիենտ");
                }

                 String dName = "Անհայտ Բժիշկ";
                String rawDoctorId = app.getDoctorId(); // Here is your Doctor ID

                if (rawDoctorId != null) {
                     dName = doctorRepository.findById(rawDoctorId)
                            .map(d -> d.getFirstName() + " " + d.getLastName())
                            .orElse("Անհայտ Բժիշկ");
                }

                 String sName = "Անհայտ Ծառայություն";
                String deptName = "-";

                if (app.getServiceId() != null) {
                    ClinicService srv = clinicServiceRepository.findById(app.getServiceId()).orElse(null);
                    if (srv != null) {
                        sName = srv.getName();
                        if (srv.getDepartmentId() != null) {
                            deptName = departmentRepository.findById(srv.getDepartmentId())
                                    .map(Department::getName)
                                    .orElse("Անհայտ բաժանմունք");
                        }
                    }
                }

                  dtoList.add(new AppointmentDTO(
                        app.getId(),
                        pName,
                        dName,
                        sName,
                        deptName,
                        app.getDate(),
                        app.getStatus()
                ));
            }
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String q = searchQuery.toLowerCase().trim();

                dtoList = dtoList.stream()
                        .filter(dto ->
                                (dto.getPatientName() != null && dto.getPatientName().toLowerCase().contains(q)) ||
                                        (dto.getDoctorName() != null && dto.getDoctorName().toLowerCase().contains(q)) ||
                                        (dto.getServiceName() != null && dto.getServiceName().toLowerCase().contains(q)) ||
                                        (dto.getDepartmentId() != null && dto.getDepartmentId().toLowerCase().contains(q)) ||
                                        (dto.getStatus() != null && dto.getStatus().toLowerCase().contains(q)) ||
                                        (dto.getDate() != null && dto.getDate().toString().contains(q))
                        )
                        .collect(Collectors.toList());
            }
            if (status != null && !status.trim().isEmpty()) {
                dtoList = dtoList.stream()
                        .filter(dto -> dto.getStatus() != null && dto.getStatus().equalsIgnoreCase(status))
                        .collect(Collectors.toList());
            }
            int totalItems = dtoList.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);

             int start = Math.min(page * size, totalItems);
            int end = Math.min(start + size, totalItems);
             List<AppointmentDTO> pagedList = (start < totalItems) ? dtoList.subList(start, end) : new ArrayList<>();

             Map<String, Object> response = new HashMap<>();
            response.put("appointments", pagedList);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalItems", totalItems);

            return response;
        }

         @GetMapping("/api/filter-data")
        @ResponseBody
        public Map<String, Object> getFilterData() {
            Map<String, Object> filters = new HashMap<>();

             filters.put("doctors", doctorRepository.findAll().stream()
                    .map(d -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", d.getId());
                         String firstName = d.getFirstName() != null ? d.getFirstName() : "";
                        String lastName = d.getLastName() != null ? d.getLastName() : "";
                        map.put("name", (firstName + " " + lastName).trim());
                        return map;
                    })
                    .collect(Collectors.toList()));

             filters.put("services", clinicServiceRepository.findAll().stream()
                    .map(s -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", s.getId());
                        map.put("name", s.getName());
                         map.put("departmentId", s.getDepartmentId());
                        return map;
                    })
                    .collect(Collectors.toList()));


            filters.put("departments", departmentRepository.findAll().stream()
                    .map(d -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", d.getId());
                        map.put("name", d.getName());
                        return map;
                    })
                    .collect(Collectors.toList()));

            return filters;
        }

     @ResponseBody
    @GetMapping("/api/stats")
    public Map<String, Long> getMonthlyStats() {
        List<Appointment> all = appointmentRepository.findAll();

        return all.stream()
                .filter(a -> a.getDate() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getDate().toString().substring(0, 7),
                        Collectors.counting()
                ));
    }

     @PutMapping("/api/appointments/{id}/status")
    @ResponseBody
    public Map<String, String> updateAppointmentStatus(
            @PathVariable String id,
            @RequestParam String status) {

        Optional<Appointment> optionalApp = appointmentRepository.findById(id);
        if (optionalApp.isPresent()) {
            Appointment app = optionalApp.get();
            app.setStatus(status);
            appointmentRepository.save(app);
            return Map.of("message", "Success");
        }
        return Map.of("error", "Not found");
    }


    @GetMapping("/api/test-scheduler")
    @ResponseBody
    public String testScheduler() {
        statusUpdater.updateConfirmedToPending();
        return "Scheduler-ը ձեռքով աշխատեցվեց!";
    }

    @ResponseBody
    @GetMapping("/api/statistics")
    public Map<String, Long> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Appointment> apps = getFilteredAppointmentsByDate(startDate, endDate);
        return apps.stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
    }

    @ResponseBody
    @GetMapping("/api/statistics/departments")
    public Map<String, Long> getDepartmentStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Appointment> apps = getFilteredAppointmentsByDate(startDate, endDate);
        Map<String, Long> deptCounts = new HashMap<>();

        for (Appointment app : apps) {
            if (!"CANCELLED".equals(app.getStatus()) && app.getServiceId() != null) {
                ClinicService srv = clinicServiceRepository.findById(app.getServiceId()).orElse(null);
                String deptName = (srv != null && srv.getDepartmentId() != null) ?
                        departmentRepository.findById(srv.getDepartmentId()).map(d -> d.getName()).orElse("Այլ") : "Այլ";
                deptCounts.put(deptName, deptCounts.getOrDefault(deptName, 0L) + 1);
            }
        }
        return deptCounts;
    }

    private List<Appointment> getFilteredAppointmentsByDate(String startDate, String endDate) {
        List<Appointment> apps = appointmentRepository.findAll();

        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            apps = apps.stream().filter(a -> a.getDate() != null && !a.getDate().isBefore(start)).collect(Collectors.toList());
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            apps = apps.stream().filter(a -> a.getDate() != null && !a.getDate().isAfter(end)).collect(Collectors.toList());
        }
        return apps;
    }

    @GetMapping("/statistics")
    public String showStatisticsPage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        List<Appointment> allAppointments = appointmentRepository.findAll();

      
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            allAppointments = allAppointments.stream()
                    .filter(a -> a.getDate() != null && !a.getDate().isBefore(start))
                    .collect(Collectors.toList());
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            allAppointments = allAppointments.stream()
                    .filter(a -> a.getDate() != null && !a.getDate().isAfter(end))
                    .collect(Collectors.toList());
        }

       
        long pendingCount = allAppointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long completedCount = allAppointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long cancelledCount = allAppointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();

        Map<String, Long> deptCounts = new HashMap<>();
        for (Appointment app : allAppointments) {
            if (!"CANCELLED".equals(app.getStatus()) && app.getServiceId() != null) {
                ClinicService srv = clinicServiceRepository.findById(app.getServiceId()).orElse(null);
                String deptName = "Այլ/Անհայտ";
                if (srv != null && srv.getDepartmentId() != null) {
                    deptName = departmentRepository.findById(srv.getDepartmentId())
                            .map(d -> d.getName())
                            .orElse("Այլ/Անհայտ");
                }
                deptCounts.put(deptName, deptCounts.getOrDefault(deptName, 0L) + 1);
            }
        }


        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("deptNames", deptCounts.keySet());
        model.addAttribute("deptValues", deptCounts.values());

     
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/statistics";
    }


    @GetMapping("/edit-requests")
    public String viewEditRequests(Model model) {
        List<EditRequest> allRequests = editRequestRepository.findAllByOrderByRequestDateDesc();

      
        Map<String, Long> doctorEditCounts = new HashMap<>();
        for (EditRequest req : allRequests) {
            if (!doctorEditCounts.containsKey(req.getDoctorId())) {
                long count = editRequestRepository.countByDoctorIdAndStatus(req.getDoctorId(), "APPROVED");
                doctorEditCounts.put(req.getDoctorId(), count);
            }
        }

        List<EditRequest> activeRequests = allRequests.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());

        List<EditRequest> history = allRequests.stream()
                .filter(r -> !"PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("activeRequests", activeRequests);
        model.addAttribute("history", history);
        model.addAttribute("doctorEditCounts", doctorEditCounts); 
        return "admin/edit-requests";
    }


    @PostMapping("/approve-edit/{requestId}")
    public String approveEdit(@PathVariable String requestId) {
        EditRequest req = editRequestRepository.findById(requestId).orElse(null);
        if (req != null) {
            req.setStatus("APPROVED");
            req.setApprovalDate(LocalDateTime.now());
            req.setExpiryDate(LocalDateTime.now().plusHours(2));
            editRequestRepository.save(req);

            MedicalRecord record = medicalRecordRepository.findById(req.getRecordId()).orElse(null);
            if (record != null) {
                record.setAdminUnlocked(true);
                medicalRecordRepository.save(record);
            }
        }
        return "redirect:/admin/edit-requests";
    }

    @PostMapping("/reject-edit/{requestId}")
    public String rejectEdit(@PathVariable String requestId, @RequestParam String adminComment) {
        EditRequest req = editRequestRepository.findById(requestId).orElse(null);
        if (req != null) {
            req.setStatus("REJECTED");
            req.setAdminComment(adminComment);
            editRequestRepository.save(req);

            MedicalRecord record = medicalRecordRepository.findById(req.getRecordId()).orElse(null);
            if (record != null) {
                record.setEditRequestReason(null); 
                medicalRecordRepository.save(record);
            }
        }
        return "redirect:/admin/edit-requests";
    }

//    @GetMapping("/api/test-billing")
//    @ResponseBody
//    public String testBilling() {
//        billingService.processCompletedProceduresAndAddToInvoice();
//        return "Billing process triggered manually. Check your console logs!";
//    }

    @GetMapping("/financials")
    public String showFinancials(Model model) {
        // Get all PAID invoices (Actual Income)
        List<Invoice> paidInvoices = invoiceRepository.findByStatus(InvoiceStatus.PAID);
        double totalRevenue = paidInvoices.stream().mapToDouble(Invoice::getTotalAmount).sum();

        // Get all PENDING invoices (Expected Future Income)
        List<Invoice> pendingInvoices = invoiceRepository.findByStatus(InvoiceStatus.PENDING_PAYMENT);
        double totalPending = pendingInvoices.stream().mapToDouble(Invoice::getTotalAmount).sum();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("paidInvoices", paidInvoices);
        return "admin/financials";
    }

    @GetMapping("/indicators")
    public String manageIndicators(org.springframework.ui.Model model) {
        List<Indicator> indicators = indicatorRepository.findAll();
        model.addAttribute("indicators", indicators);

        List<String> uniqueTestNames = indicators.stream()
                .map(Indicator::getTestName)
                .distinct()
                .toList();
        model.addAttribute("uniqueTestNames", uniqueTestNames);

        return "admin/indicators-setup";
    }

    // 2. ՊԱՀՊԱՆԵԼ ՑՈՒՑԱՆԻՇԸ
    @PostMapping("/indicators/save")
    public String saveIndicator(@ModelAttribute Indicator indicator, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (indicator.getId() != null && indicator.getId().trim().isEmpty()) {
            indicator.setId(null);
        }

        // 🔴 ՈՒՂՂՈՒՄ 2. Մաքրում ենք բանալին ավելորդ պրոբելներից
        if (indicator.getIndicatorKey() != null) {
            indicator.setIndicatorKey(indicator.getIndicatorKey().trim().toLowerCase().replaceAll("\\s+", ""));
        }

        indicatorRepository.save(indicator);
        redirectAttributes.addFlashAttribute("successMsg", "Լաբորատոր ցուցանիշը հաջողությամբ պահպանվեց բազայում:");
        return "redirect:/admin/indicators";
    }

    // 3. ԱՊԱԱԿՏԻՎԱՑՆԵԼ ԿԱՄ ԱԿՏԻՎԱՑՆԵԼ ՑՈՒՑԱՆԻՇԸ (Soft Delete)
    @PostMapping("/indicators/toggle-status/{id}")
    public String toggleIndicatorStatus(@PathVariable String id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Indicator indicator = indicatorRepository.findById(id).orElse(null);
        if (indicator != null) {
            // Եթե ակտիվ էր՝ սարքում ենք ոչ ակտիվ, և հակառակը
            indicator.setActive(!indicator.isActive());
            indicatorRepository.save(indicator);

            String statusMsg = indicator.isActive() ? "ակտիվացվեց" : "ապաակտիվացվեց";
            redirectAttributes.addFlashAttribute("successMsg", "Ցուցանիշը հաջողությամբ " + statusMsg + ":");
        }
        return "redirect:/admin/indicators";
    }

    @PostMapping("/indicators/import-csv")
    public String importIndicatorsFromCsv(@RequestParam("file") MultipartFile file, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Խնդրում ենք ընտրել Excel ֆայլ:");
            return "redirect:/admin/indicators";
        }

        try {
            // 🔴 ԿԱՆՉՈՒՄ ԵՆՔ ՔՈ ՍԵՐՎԻՍԻ ՄԵԹՈԴԸ
            int importedCount = excelImportService.importIndicators(file);

            redirectAttributes.addFlashAttribute("successMsg", "Հաջողությամբ ներմուծվեց/թարմացվեց " + importedCount + " լաբորատոր ցուցանիշ օրիգինալ Excel ֆայլից:");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Ֆայլի կարդալու սխալ: Համոզվեք, որ ներբեռնում եք վավեր .xlsx ֆայլ:");
            e.printStackTrace();
        }

        return "redirect:/admin/indicators";
    }


}
