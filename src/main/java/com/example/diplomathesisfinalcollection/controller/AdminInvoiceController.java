package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.dto.FinancialReportDTO;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.dto.FinancialReportDTO;
import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invoices")
@RequiredArgsConstructor
public class AdminInvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/dashboard")
    public String getFinancialDashboard(@RequestParam(required = false) String status, Model model) {
        // Fetch all invoices to display on the dashboard
        List<Invoice> allInvoices = invoiceRepository.findAll();

        // This list will hold the "flattened" data formatted for the UI
        List<FinancialReportDTO> report = new ArrayList<>();

        // Define the threshold for a "pending" invoice turning into an "unpaid" (overdue) invoice
        LocalDateTime limit = LocalDateTime.now().minusHours(24);

        for (Invoice inv : allInvoices) {
            String patientName = "Անհայտ պացիենտ";
            String doctorName = "Ընդհանուր կլինիկա";
            String deptName = "Ընդհանուր";
            String serviceName = "Տարբեր ծառայություններ";

            // 1. Resolve Patient Name
            if (inv.getPatientId() != null) {
                User patient = userRepository.findById(inv.getPatientId()).orElse(null);
                if (patient != null) {
                    patientName = patient.getFirstName() + " " + patient.getLastName();
                }
            }

            // 2. Resolve Doctor, Department, and ALL Services
            if (inv.getLineItems() != null && !inv.getLineItems().isEmpty()) {
                List<String> allServiceNames = new ArrayList<>();
                for (Invoice.LineItem item : inv.getLineItems()) {
                    if (item.getDescription() != null) {
                        allServiceNames.add(item.getDescription());
                    }
                }
                serviceName = String.join(", ", allServiceNames);

                for (Invoice.LineItem item : inv.getLineItems()) {
                    Appointment appt = appointmentRepository.findById(item.getSourceId()).orElse(null);
                    if (appt != null) {
                        if (appt.getDoctorId() != null) {
                            Doctor doctor = doctorRepository.findById(appt.getDoctorId()).orElse(null);
                            if (doctor != null) {
                                doctorName = doctor.getFirstName() + " " + doctor.getLastName();
                            }
                        }
                        if (appt.getServiceId() != null) {
                            ClinicService service = clinicServiceRepository.findById(appt.getServiceId()).orElse(null);
                            if (service != null && service.getDepartmentId() != null) {
                                Department dept = departmentRepository.findById(service.getDepartmentId()).orElse(null);
                                if (dept != null) {
                                    deptName = dept.getName();
                                }
                            }
                        }
                        break;
                    }
                }
            }

            // 3. Logic to calculate the UI Status
            String uiStatus = inv.getStatus().name();
            if (inv.getStatus() == InvoiceStatus.PENDING_PAYMENT) {
                if (inv.getIssueDate() != null && inv.getIssueDate().isBefore(limit)) {
                    uiStatus = "UNPAID"; // Ժամկետանց
                } else {
                    uiStatus = "PENDING"; // Սպասվող
                }
            }

            report.add(FinancialReportDTO.builder()
                    .invoiceId(inv.getId())
                    .patientName(patientName)
                    .doctorName(doctorName)
                    .departmentName(deptName)
                    .serviceName(serviceName)
                    .amount(inv.getTotalAmount())
                    .status(uiStatus)
                    .issueDate(inv.getIssueDate())
                    .build());
        }

        // 🔀 ԱՎՏՈՄԱՏ ՖԻԼՏՐՈՒՄ ԸՍՏ ԱԴՄԻՆԻ ԸՆՏՐՈՒԹՅԱՆ
        List<FinancialReportDTO> filteredReport = report;
        if (status != null && !status.isEmpty()) {
            filteredReport = report.stream()
                    .filter(r -> r.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // Դասավորում ենք ամենանորերը սկզբում
        filteredReport.sort((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()));

        model.addAttribute("report", filteredReport);
        model.addAttribute("currentStatusFilter", status); // Հիշում ենք ընթացիկ ֆիլտրը

        // Ստատիստիկան միշտ հաշվարկվում է ամբողջական բազայի վրա
        double totalPaid = report.stream()
                .filter(r -> "PAID".equals(r.getStatus()))
                .mapToDouble(FinancialReportDTO::getAmount)
                .sum();

        double totalOutstanding = report.stream()
                .filter(r -> "PENDING".equals(r.getStatus()) || "UNPAID".equals(r.getStatus()))
                .mapToDouble(FinancialReportDTO::getAmount)
                .sum();

        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalOutstanding", totalOutstanding);

        return "admin/financial-dashboard";
    }

    @GetMapping("/analytics")
    public String getFinancialAnalytics(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws JsonProcessingException {

        // 1. Get Base Data
        List<FinancialReportDTO> allData = buildBaseReportData();

        // 2. Apply Filters dynamically
        List<FinancialReportDTO> filteredData = allData.stream().filter(r -> {
            boolean matchSearch = search == null || search.isEmpty() || r.getPatientName().toLowerCase().contains(search.toLowerCase()) || r.getServiceName().toLowerCase().contains(search.toLowerCase());
            boolean matchDept = department == null || department.isEmpty() || r.getDepartmentName().equals(department);
            boolean matchDoc = doctor == null || doctor.isEmpty() || r.getDoctorName().equals(doctor);
            boolean matchStatus = status == null || status.isEmpty() || r.getStatus().equals(status);
            boolean matchStart = startDate == null || !r.getIssueDate().toLocalDate().isBefore(startDate);
            boolean matchEnd = endDate == null || !r.getIssueDate().toLocalDate().isAfter(endDate);
            return matchSearch && matchDept && matchDoc && matchStatus && matchStart && matchEnd;
        }).collect(Collectors.toList());

        // 3. Prepare Chart Data
        Map<String, Double> revenueByDept = new HashMap<>();
        Map<String, Long> statusCount = new HashMap<>();
        Map<String, Double> dailyRevenue = new TreeMap<>(); // TreeMap keeps dates sorted

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (FinancialReportDTO r : filteredData) {
            // Department Revenue (Only count PAID)
            if ("PAID".equals(r.getStatus())) {
                revenueByDept.put(r.getDepartmentName(), revenueByDept.getOrDefault(r.getDepartmentName(), 0.0) + r.getAmount());

                // Daily Revenue
                String dateKey = r.getIssueDate().format(dateFormatter);
                dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + r.getAmount());
            }
            // Status distribution
            statusCount.put(r.getStatus(), statusCount.getOrDefault(r.getStatus(), 0L) + 1);
        }

        ObjectMapper mapper = new ObjectMapper();

        // 4. Populate Dropdowns for the filter UI
        model.addAttribute("allDepartments", allData.stream().map(FinancialReportDTO::getDepartmentName).distinct().collect(Collectors.toList()));
        model.addAttribute("allDoctors", allData.stream().map(FinancialReportDTO::getDoctorName).filter(d -> !d.equals("Ընդհանուր կլինիկա")).distinct().collect(Collectors.toList()));

        // 5. Send data to frontend
        model.addAttribute("report", filteredData);
        model.addAttribute("chartDeptLabels", mapper.writeValueAsString(revenueByDept.keySet()));
        model.addAttribute("chartDeptData", mapper.writeValueAsString(revenueByDept.values()));

        model.addAttribute("chartStatusLabels", mapper.writeValueAsString(statusCount.keySet()));
        model.addAttribute("chartStatusData", mapper.writeValueAsString(statusCount.values()));

        model.addAttribute("chartDateLabels", mapper.writeValueAsString(dailyRevenue.keySet()));
        model.addAttribute("chartDateData", mapper.writeValueAsString(dailyRevenue.values()));

        // Keep current filter values
        model.addAttribute("search", search);
        model.addAttribute("selectedDept", department);
        model.addAttribute("selectedDoc", doctor);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/financial-analytics";
    }

    // --- 3. EXCEL EXPORT ENDPOINT ---
    @GetMapping("/export")
    public void exportToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        List<FinancialReportDTO> allData = buildBaseReportData();
        List<FinancialReportDTO> filteredData = allData.stream().filter(r -> {
            boolean matchSearch = search == null || search.isEmpty() || r.getPatientName().toLowerCase().contains(search.toLowerCase());
            boolean matchDept = department == null || department.isEmpty() || r.getDepartmentName().equals(department);
            boolean matchDoc = doctor == null || doctor.isEmpty() || r.getDoctorName().equals(doctor);
            boolean matchStatus = status == null || status.isEmpty() || r.getStatus().equals(status);
            boolean matchStart = startDate == null || !r.getIssueDate().toLocalDate().isBefore(startDate);
            boolean matchEnd = endDate == null || !r.getIssueDate().toLocalDate().isAfter(endDate);
            return matchSearch && matchDept && matchDoc && matchStatus && matchStart && matchEnd;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Financial_Report_" + LocalDate.now() + ".xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Եկամուտներ");

        // Header Row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Ինվոյս ID", "Ամսաթիվ", "Պացիենտ", "Բժիշկ", "Բաժանմունք", "Ծառայություններ", "Գումար (֏)", "Կարգավիճակ"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Data Rows
        int rowIdx = 1;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (FinancialReportDTO dto : filteredData) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(dto.getInvoiceId());
            row.createCell(1).setCellValue(dto.getIssueDate().format(dtf));
            row.createCell(2).setCellValue(dto.getPatientName());
            row.createCell(3).setCellValue(dto.getDoctorName());
            row.createCell(4).setCellValue(dto.getDepartmentName());
            row.createCell(5).setCellValue(dto.getServiceName());
            row.createCell(6).setCellValue(dto.getAmount());
            row.createCell(7).setCellValue(dto.getStatus());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // --- HELPER METHOD: Builds the flattened data structure ---
    private List<FinancialReportDTO> buildBaseReportData() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<FinancialReportDTO> report = new ArrayList<>();
        LocalDateTime limit = LocalDateTime.now().minusHours(24);

        for (Invoice inv : allInvoices) {
            String patientName = "Անհայտ պացիենտ", doctorName = "Ընդհանուր կլինիկա", deptName = "Ընդհանուր", serviceName = "Տարբեր ծառայություններ";

            if (inv.getPatientId() != null) {
                User patient = userRepository.findById(inv.getPatientId()).orElse(null);
                if (patient != null) patientName = patient.getFirstName() + " " + patient.getLastName();
            }

            if (inv.getLineItems() != null && !inv.getLineItems().isEmpty()) {
                List<String> allServiceNames = new ArrayList<>();
                for (Invoice.LineItem item : inv.getLineItems()) {
                    if (item.getDescription() != null) allServiceNames.add(item.getDescription());
                }
                serviceName = String.join(", ", allServiceNames);

                for (Invoice.LineItem item : inv.getLineItems()) {
                    Appointment appt = appointmentRepository.findById(item.getSourceId()).orElse(null);
                    if (appt != null) {
                        if (appt.getDoctorId() != null) {
                            Doctor doctor = doctorRepository.findById(appt.getDoctorId()).orElse(null);
                            if (doctor != null) doctorName = "Բժ. " + doctor.getFirstName() + " " + doctor.getLastName();
                        }
                        if (appt.getServiceId() != null) {
                            ClinicService service = clinicServiceRepository.findById(appt.getServiceId()).orElse(null);
                            if (service != null && service.getDepartmentId() != null) {
                                Department dept = departmentRepository.findById(service.getDepartmentId()).orElse(null);
                                if (dept != null) deptName = dept.getName();
                            }
                        }
                        break;
                    }
                }
            }

            String uiStatus = inv.getStatus().name();
            if (inv.getStatus() == InvoiceStatus.PENDING_PAYMENT) {
                uiStatus = (inv.getIssueDate() != null && inv.getIssueDate().isBefore(limit)) ? "UNPAID" : "PENDING";
            }

            report.add(FinancialReportDTO.builder()
                    .invoiceId(inv.getId()).patientName(patientName).doctorName(doctorName)
                    .departmentName(deptName).serviceName(serviceName).amount(inv.getTotalAmount())
                    .status(uiStatus).issueDate(inv.getIssueDate()).build());
        }
        // Sort newest first
        report.sort((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()));
        return report;
    }

}