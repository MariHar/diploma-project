package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.appointment.AppointmentRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.doctor.DoctorSchedule;
import com.example.diplomathesisfinalcollection.doctor.DoctorScheduleRepository;
import com.example.diplomathesisfinalcollection.dto.DoctorRegistrationDTO;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.EditRequestRepository;
import com.example.diplomathesisfinalcollection.medicalrecorder.MedicalRecordRepository;
import com.example.diplomathesisfinalcollection.services.BillingService;
import com.example.diplomathesisfinalcollection.services.CloudinarySecurityService;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/doctors")
public class AdminDoctorController {


    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired private ClinicServiceRepository clinicServiceRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;

    @GetMapping
    public String manageDoctors(Model model) {
        model.addAttribute("departments", departmentRepository.findByActiveTrueOrderByNameAsc());

        List<Doctor> doctors = doctorRepository.findAll();
        Map<String, Boolean> loginStatuses = new HashMap<>();

        for (Doctor doc : doctors) {
            User user = userRepository.findById(doc.getUserId()).orElse(null);

            if (user != null) {
                // Ստուգում ենք մուտքի կարգավիճակը
                loginStatuses.put(doc.getId(), !user.isForcePasswordChange());

                // Եթե մեյլը դատարկ է, վերցնում ենք User-ից
                if (doc.getEmail() == null || doc.getEmail().isEmpty()) {
                    doc.setEmail(user.getEmail());
                }

                // Ապահովագրում ենք նաև հեռախոսահամարը, եթե այն ևս պակասում է
                if (doc.getPhone() == null || doc.getPhone().isEmpty()) {
                    doc.setPhone(user.getPhone());
                }
            } else {
                loginStatuses.put(doc.getId(), false);
            }
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("newDoctor", new DoctorRegistrationDTO());
        model.addAttribute("services", clinicServiceRepository.findAll());
        model.addAttribute("loginStatuses", loginStatuses);

        return "admin/doctors";
    }
    private String saveProfilePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            // Վերցնում ենք բացարձակ ճանապարհը
            Path uploadPath = Paths.get("uploads/doctors").toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Անունը սարքում ենք յուրահատուկ՝ բացատները ջնջելով
            String originalName = file.getOriginalFilename().replaceAll("\\s+", "_");
            String fileName = System.currentTimeMillis() + "_" + originalName;
            Path filePath = uploadPath.resolve(fileName);

            // Պահպանում ենք ֆայլը (եթե կա նույն անունով, վրայից կգրի)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/doctors/" + fileName;
        } catch (IOException e) {
            System.out.println("❌ ՍԽԱԼ ՆԿԱՐԸ ՊԱՀՊԱՆԵԼԻՍ: " + e.getMessage());
            return null;
        }
    }

    @PostMapping("/save")
    public String saveDoctor(@ModelAttribute DoctorRegistrationDTO dto, RedirectAttributes redirectAttributes) {
        // Գեներացնում ենք 8-նիշանոց պատահական կոդ (նախկին UUID-ի փոխարեն)
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMsg", "ՍԽԱԼ. «" + dto.getEmail() + "» հասցեով արդեն կա գրանցված օգտատեր: Խնդրում ենք օգտագործել այլ մեյլ:");
            return "redirect:/admin/doctors";
        }
        String tempPassword = generateAlphanumericCode(8);
        String spec = (dto.getSpecialization() != null) ? dto.getSpecialization().toLowerCase().trim() : "";
        String role = spec.contains("լաբ") || spec.contains("lab") ? "ROLE_LAB_WORKER" : "ROLE_DOCTOR";
        String salutation = role.equals("ROLE_LAB_WORKER") ? "" : "Dr. ";

        User newDoctorUser = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .role(role)
                .enabled(true)
                .forcePasswordChange(true)
                .build();
        User savedUser = userRepository.save(newDoctorUser);

        String photoUrl = saveProfilePhoto(dto.getPhotoFile());

        Doctor newDoctorProfile = Doctor.builder()
                .userId(savedUser.getId())
                .departmentId(dto.getDepartmentId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .patronymic(dto.getPatronymic())
                .email(dto.getEmail())
                .description(dto.getDescription())
                .photoUrl(photoUrl)
                .phone(dto.getPhone())
                .serviceIds(dto.getServiceIds()) // Բազայից ընտրված ծառայությունների ցանկ
                .specialization(dto.getSpecialization())
                .experienceYears(dto.getExperienceYears())
                .education(dto.getEducation())
                .languagesSpoken(dto.getLanguagesSpoken())
                .active(true)
                .build();
        doctorRepository.save(newDoctorProfile);

        // Ուղարկում ենք 8-նիշանոց ժամանակավոր կոդը
        String subject = "Բարի գալուստ Համակարգ - Ձեր մուտքային տվյալները";
        String body = "Բարև " + salutation + dto.getLastName() + ",\n\n" +
                "Ձեր հաշիվը հաջողությամբ ստեղծվել է:\n\n" +
                "Էլ. հասցե: " + dto.getEmail() + "\n" +
                "Մուտքի 8-նիշանոց կոդ: " + tempPassword + "\n\n" +
                "Խնդրում ենք մուտք գործել: Առաջին մուտքի ժամանակ համակարգը կպահանջի ստեղծել նոր անձնական գաղտնաբառ:";

        boolean isSent = emailService.sendSimpleMessage(dto.getEmail(), subject, body);

        if (isSent) {
            redirectAttributes.addFlashAttribute("successMsg", "Բժիշկը հաջողությամբ գրանցվեց, և նամակն ուղարկվեց:");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "ՈՒՇԱԴՐՈՒԹՅՈՒՆ. Բժիշկը գրանցվեց, ԲԱՅՑ նամակը չուղարկվեց: Հնարավոր է՝ էլ. հասցեն գոյություն չունի:");
        }
        return "redirect:/admin/doctors";
    }
    @PostMapping("/toggle/{id}")
    public String toggleDoctor(@PathVariable String id) {
        doctorRepository.findById(id).ifPresent(doc -> {
            doc.setActive(!doc.isActive());
            doctorRepository.save(doc);
        });
        return "redirect:/admin/doctors";
    }

    @GetMapping("/edit/{id}")
    public String editDoctorPage(@PathVariable String id, Model model) {
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null) return "redirect:/admin/doctors";

        // Քանի որ մեյլը User-ի մեջ է հիմնականում, ապահովության համար քաշում ենք նաև այնտեղից
        User user = userRepository.findById(doctor.getUserId()).orElse(null);
        if (user != null && (doctor.getEmail() == null || doctor.getEmail().isEmpty())) {
            doctor.setEmail(user.getEmail());
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("departments", departmentRepository.findByActiveTrueOrderByNameAsc());

        // Ավելացված է, որպեսզի խմբագրելիս ծառայությունները երևան
        model.addAttribute("services", clinicServiceRepository.findAll());

        return "admin/doctor-edit";
    }

    // Պահպանում է բժշկի թարմացված տվյալները
    @PostMapping("/update/{id}")
    public String updateDoctor(@PathVariable String id, @ModelAttribute DoctorRegistrationDTO updatedInfo, RedirectAttributes redirectAttributes) {
        Doctor doct = doctorRepository.findById(id).orElse(null);
        if (doct == null) return "redirect:/admin/doctors";

        doct.setFirstName(updatedInfo.getFirstName());
        doct.setLastName(updatedInfo.getLastName());
        doct.setPatronymic(updatedInfo.getPatronymic());
        doct.setSpecialization(updatedInfo.getSpecialization());
        doct.setExperienceYears(updatedInfo.getExperienceYears());
        doct.setDepartmentId(updatedInfo.getDepartmentId());
        doct.setDescription(updatedInfo.getDescription());
        doct.setPhone(updatedInfo.getPhone());
        doct.setServiceIds(updatedInfo.getServiceIds());
        doct.setEducation(updatedInfo.getEducation());
        doct.setLanguagesSpoken(updatedInfo.getLanguagesSpoken());

        if (updatedInfo.getPhotoFile() != null && !updatedInfo.getPhotoFile().isEmpty()) {
            doct.setPhotoUrl(saveProfilePhoto(updatedInfo.getPhotoFile()));
        }
        doctorRepository.save(doct);

        User user = userRepository.findById(doct.getUserId()).orElse(null);
        if (user != null) {
            boolean emailChanged = !user.getEmail().equals(updatedInfo.getEmail());

            user.setFirstName(updatedInfo.getFirstName());
            user.setLastName(updatedInfo.getLastName());
            user.setPhone(updatedInfo.getPhone());

            if (emailChanged) {
                var existingUserWithNewEmail = userRepository.findByEmail(updatedInfo.getEmail());
                if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("errorMsg", "ՍԽԱԼ. Չհաջողվեց փոխել մեյլը, քանի որ «" + updatedInfo.getEmail() + "» հասցեն արդեն զբաղված է այլ օգտատիրոջ կողմից:");
                    return "redirect:/admin/doctors"; // Կամ redirect:/admin/doctors/edit/ + id
                }
                user.setEmail(updatedInfo.getEmail());
                // Գեներացնում ենք նոր 8-նիշանոց կոդ
                String newVerificationCode = generateAlphanumericCode(8);
                user.setPasswordHash(passwordEncoder.encode(newVerificationCode));
                user.setForcePasswordChange(true);
                userRepository.save(user);

                String body = "Հարգելի Բժիշկ,\n\n" +
                        "Ձեր էլ. հասցեն համակարգում թարմացվել է ադմինիստրատորի կողմից:\n" +
                        "Անվտանգության նկատառումներից ելնելով՝ Ձեր հին գաղտնաբառը զրոյացվել է:\n\n" +
                        "Ձեր ՆՈՐ մուտքի 8-նիշանոց ժամանակավոր գաղտնաբառը: " + newVerificationCode + "\n\n" +
                        "Խնդրում ենք մուտք գործել և սահմանել նոր անձնական գաղտնաբառ:";

                // Ուղարկում ենք և ստուգում արդյունքը
                boolean isSent = emailService.sendSimpleMessage(updatedInfo.getEmail(), "Կարևոր. Մուտքային տվյալների թարմացում", body);

                if (isSent) {
                    redirectAttributes.addFlashAttribute("successMsg", "Տվյալները թարմացվեցին, նոր կոդը հաջողությամբ ուղարկվել է բժշկին:");
                } else {
                    redirectAttributes.addFlashAttribute("errorMsg", "ՈՒՇԱԴՐՈՒԹՅՈՒՆ. Տվյալները պահպանվեցին, ԲԱՅՑ նոր մեյլը սխալ է, նամակը տեղ չհասավ:");
                }
            } else {
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("successMsg", "Բժշկի տվյալները հաջողությամբ թարմացվեցին:");
            }
        }

        return "redirect:/admin/doctors";
    }

    private String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }



    @GetMapping("/schedule/{id}")
    public String manageDoctorSchedule(@PathVariable String id, Model model) {
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null) return "redirect:/admin/doctors";

        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(id);

        // Եթե բժիշկը նոր է և դեռ գրաֆիկ չունի, ավտոմատ ստեղծում ենք շաբլոն
        if (schedules.isEmpty()) {
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
            for (String day : days) {
                boolean isWorking = !day.equals("SATURDAY") && !day.equals("SUNDAY"); // Շաբաթ/Կիրակի հանգիստ
                schedules.add(DoctorSchedule.builder()
                        .doctorId(id)
                        .dayOfWeek(day)
                        .startTime(isWorking ? "09:00" : "")
                        .endTime(isWorking ? "18:00" : "")
                        .isWorkingDay(isWorking)
                        .build());
            }
            doctorScheduleRepository.saveAll(schedules);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("schedules", schedules);
        return "admin/doctor-schedule";
    }

    @PostMapping("/schedule/{id}")
    public String saveDoctorSchedule(@PathVariable String id,
                                     @RequestParam Map<String, String> params,
                                     RedirectAttributes redirectAttributes) {

        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null) return "redirect:/admin/doctors";

        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(id);
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

        for (String day : days) {
            // Գտնում ենք տվյալ օրվա գրաֆիկը բազայից կամ ստեղծում ենք նորը
            DoctorSchedule dailySchedule = schedules.stream()
                    .filter(s -> s.getDayOfWeek().equals(day))
                    .findFirst()
                    .orElse(new DoctorSchedule());

            dailySchedule.setDoctorId(id);
            dailySchedule.setDayOfWeek(day);

            // Checkbox-ները ուղարկվում են միայն այն դեպքում, եթե նշված են (checked)
            boolean isWorking = params.containsKey("isWorking_" + day);
            dailySchedule.setWorkingDay(isWorking);

            if (isWorking) {
                dailySchedule.setStartTime(params.get("startTime_" + day));
                dailySchedule.setEndTime(params.get("endTime_" + day));

                boolean hasBreak = params.containsKey("hasBreak_" + day);
                dailySchedule.setHasBreak(hasBreak);

                if (hasBreak) {
                    dailySchedule.setBreakStartTime(params.get("breakStartTime_" + day));
                    dailySchedule.setBreakEndTime(params.get("breakEndTime_" + day));
                } else {
                    dailySchedule.setBreakStartTime(null);
                    dailySchedule.setBreakEndTime(null);
                }
            } else {
                // Եթե տվյալ օրը չի աշխատում, մաքրում ենք ժամերը
                dailySchedule.setStartTime("");
                dailySchedule.setEndTime("");
                dailySchedule.setHasBreak(false);
            }

            doctorScheduleRepository.save(dailySchedule);
        }

        redirectAttributes.addFlashAttribute("successMsg", "Բժշկի գրաֆիկը հաջողությամբ թարմացվեց:");
        return "redirect:/admin/doctors";
    }


}
