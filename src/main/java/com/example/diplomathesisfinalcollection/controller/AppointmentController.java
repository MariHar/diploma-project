package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.Appointment;
import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorSchedule;
import com.example.diplomathesisfinalcollection.doctor.DoctorScheduleRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.rating.Rating;
import com.example.diplomathesisfinalcollection.rating.RatingRepository;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClinicServiceRepository clinicServiceRepository;
    @Autowired private EmailService emailService;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private RatingRepository ratingRepository;
    private static final Object bookingLock = new Object();


    @GetMapping()
    public String showBookingPage(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        List<Department> filteredDepartments = departmentRepository.findAll().stream()
                .filter(Department::isActive)
                .collect(Collectors.toList());

        model.addAttribute("departments", filteredDepartments);
        model.addAttribute("services", clinicServiceRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        return "appointment"; // We will create this HTML next
    }

    @PostMapping("/save")
    public String saveAppointment(
            @RequestParam String doctorId,
            @RequestParam String serviceId,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            Model model,
            Principal principal) {
        LocalDate today = LocalDate.now();

        User patient = userRepository.findByEmail(principal.getName()).orElse(null);
        if (patient == null) return "redirect:/login";

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "redirect:/appointments?error=DoctorNotFound";
        }

        ClinicService service = clinicServiceRepository.findById(serviceId).orElse(null);
        if (service != null) {
            Department department = departmentRepository.findById(service.getDepartmentId()).orElse(null);
            // Եթե բաժանմունքը Լաբորատորիա կամ Ռադիոլոգիա է (որտեղ isPubliclyBookable = false)
            if (department != null && !department.isPubliclyBookable()) {
                return "redirect:/appointments?error=ReferralRequired"; // Վերադարձնում ենք հատուկ սխալ
            }
        }

        String dateTimeString = appointmentDate + "T" + appointmentTime;
        LocalDateTime apptDate = LocalDateTime.parse(dateTimeString);
        LocalDateTime now = LocalDateTime.now();

        if (apptDate.isBefore(now)) {
            return "redirect:/appointments?error=PastTimeNotAllowed";
        }

        String finalStatus = apptDate.toLocalDate().isEqual(today) ? "PENDING" : "CONFIRMED";

        synchronized (bookingLock) {

            if (appointmentRepository.existsByDoctorIdAndDateAndStatusNot(doctorId, apptDate, "CANCELLED")) {
                return "redirect:/appointments?error=TimeSlotAlreadyBooked";
            }

            LocalDateTime startOfDay = apptDate.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = apptDate.toLocalDate().atTime(LocalTime.MAX);
            if (appointmentRepository.existsByPatientIdAndDateBetweenAndStatusNot(patient.getId(), startOfDay, endOfDay, "CANCELLED")) {
                return "redirect:/appointments?error=DailyBookingLimitReached";
            }

            long activeAppointmentsCount = appointmentRepository.countByPatientIdAndDateAfterAndStatus(patient.getId(), now, "CONFIRMED");
            if (activeAppointmentsCount >= 3) {
                return "redirect:/appointments?error=GlobalBookingLimitReached";
            }

            Appointment newAppointment = Appointment.builder()
                    .patientId(patient.getId())
                    .doctorId(doctorId)
                    .serviceId(serviceId)
                    .date(apptDate)
                    .status(finalStatus)
                    .wasNotified(false)
                    .build();

            appointmentRepository.save(newAppointment);
        }

        String subject = "✅ Appointment Confirmed - Avelia Clinic";
        String body = String.format("""
        Hello %s,
        
        Your appointment with Dr. %s is confirmed for:
        📅 Date: %s
        🕒 Time: %s
        
        Location: Bagramyan 1:
        
        Thank you for choosing Avelia Clinic!
        """,
                patient.getFirstName(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                finalStatus.equals("PENDING") ? "Awaiting Visit (Today)" : "Confirmed (Future)",
                apptDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")),
                apptDate.format(DateTimeFormatter.ofPattern("h:mm a"))
        );

        emailService.sendSimpleMessage(patient.getEmail(), subject, body);

        return "redirect:/profile?booked=true";
    }

    @GetMapping("/api/available-slots")
    @ResponseBody
    public List<String> getAvailableSlots(@RequestParam String doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        String dayOfWeek = localDate.getDayOfWeek().name();

        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        if (schedule == null || !schedule.isWorkingDay()) {
            return new ArrayList<>();
        }

        LocalTime startTime = LocalTime.parse(schedule.getStartTime());
        LocalTime endTime = LocalTime.parse(schedule.getEndTime());

        LocalTime breakStart = schedule.isHasBreak() ? LocalTime.parse(schedule.getBreakStartTime()) : null;
        LocalTime breakEnd = schedule.isHasBreak() ? LocalTime.parse(schedule.getBreakEndTime()) : null;

        List<String> availableSlots = new ArrayList<>();
        LocalTime currentSlot = startTime;
        LocalDateTime now = LocalDateTime.now();

        while (currentSlot.isBefore(endTime)) {
            boolean isAvailable = true;

            if (schedule.isHasBreak() && !currentSlot.isBefore(breakStart) && currentSlot.isBefore(breakEnd)) {
                isAvailable = false;
            }

            if (localDate.isEqual(now.toLocalDate()) && currentSlot.isBefore(now.toLocalTime())) {
                isAvailable = false;
            }

            if (isAvailable) {
                LocalDateTime slotDateTime = LocalDateTime.of(localDate, currentSlot);
                boolean isBooked = appointmentRepository.existsByDoctorIdAndDateAndStatusNot(doctorId, slotDateTime, "CANCELLED");
                if (isBooked) {
                    isAvailable = false;
                }
            }

            if (isAvailable) {
                availableSlots.add(currentSlot.toString()); // Օրինակ՝ "09:30"
            }

            currentSlot = currentSlot.plusMinutes(30);


        }


        return availableSlots;
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable String id,
                                    @RequestParam("cancelReason") String cancelReason,
                                    Principal principal) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        if (appointment != null) {
            appointment.setStatus("CANCELLED");
            appointment.setCancelReason(cancelReason);
            appointmentRepository.save(appointment);

            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                int newCount = user.getCancellationCount() + 1;
                user.setCancellationCount(newCount);

                if (newCount >= 3) {
                    user.setEnabled(false);
                    System.out.println("❌ ՕԳՏԱՏԵՐԸ ԲԼՈԿԱՎՈՐՎԵՑ: " + user.getEmail());

                    String blockBody = String.format("Հարգելի %s,\nԴուք չեղարկել եք 3 այց: Ձեր պրոֆիլը արգելափակվել է: Կապվեք ադմինիստրացիայի հետ:", user.getFirstName());
                    emailService.sendSimpleMessage(user.getEmail(), "Պրոֆիլի Արգելափակում", blockBody);
                } else {
                    int remaining = 3 - newCount;
                    String cancelBody = String.format("Հարգելի %s,\nՁեր այցը չեղարկվել է: Պատճառը՝ %s\nԶԳՈՒՇԱՑՈՒՄ. Մնացել է %d չեղարկման հնարավորություն:",
                            user.getFirstName(), cancelReason, remaining);
                    emailService.sendSimpleMessage(user.getEmail(), "Այցի Չեղարկում", cancelBody);
                }

                userRepository.save(user);

                if (!user.isEnabled()) {
                    return "redirect:/logout?blocked=true";
                }
            }
        }
        return "redirect:/profile";
    }

}