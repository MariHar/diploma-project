package com.example.diplomathesisfinalcollection.services;

import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.laboratory.Indicator;
import com.example.diplomathesisfinalcollection.laboratory.IndicatorRepository;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final DepartmentRepository departmentRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final IndicatorRepository indicatorRepository;

    public void importData(MultipartFile file) throws IOException {
        try(Workbook workbook = new XSSFWorkbook(file.getInputStream())){

        // Կոդերի և իրական ID-ների կապը պահելու համար (Map-եր)
        Map<String, String> deptCodeToId = new HashMap<>();
        Map<String, String> srvCodeToId = new HashMap<>();

        // --- ՔԱՅԼ 1: ԲԱԺԱՆՄՈՒՆՔՆԵՐ (DEPARTMENTS) ---
        Sheet deptSheet = workbook.getSheet("Departments");
        if (deptSheet != null) {
            for (int i = 1; i <= deptSheet.getLastRowNum(); i++) {
                Row row = deptSheet.getRow(i);
                if (row == null) continue;
                String deptCode = getStr(row, 0);
// ՍՏՈՒԳՈՒՄ. Եթե կա, վերցնում ենք ID-ն ու Map-ում պահելով անցնում առաջ
                Optional<Department> existingDept = departmentRepository.findByCode(deptCode);
                if (existingDept.isPresent()) {
                    deptCodeToId.put(deptCode, existingDept.get().getId());
                    System.out.println("Բաժանմունքը արդեն կա բազայում, վերցնում ենք հին ID-ն: " + deptCode);
                    continue;
                }

                Department dept = Department.builder()
                        .code(getStr(row, 0))
                        .name(getStr(row, 1)) // Default (AM)
                        .description(getStr(row, 4)) // Default (AM)
                        .location(getStr(row, 7))
                        .contactPhone(getStr(row, 8))
                        .images(new ArrayList<>()) // Initialize list
                        .build();

                // Լցնում ենք թարգմանությունները
//                dept.getNameTranslations().put("ru", getStr(row, 2));
//                dept.getNameTranslations().put("en", getStr(row, 3));
//                dept.getDescTranslations().put("ru", getStr(row, 5));
//                dept.getDescTranslations().put("en", getStr(row, 6));

                String imageUrlsRaw = getStr(row, 9);
                if (!imageUrlsRaw.isEmpty()) {
                    String[] urls = imageUrlsRaw.split(",");
                    for (String url : urls) {
                        // Since these are external URLs from Excel, we set them as the URL
                        // In a real scenario, you'd save the Cloudinary Public ID here
                        Department.DepartmentImage img = new Department.DepartmentImage();
                        img.setPublicId("external_url"); // Placeholder
                        img.setUrl(url.trim());
                        img.setActive(true);
                        dept.getImages().add(img);
                    }
                }
                Department saved = departmentRepository.save(dept);
                deptCodeToId.put(dept.getCode(), saved.getId());
            }
        }

        // --- ՔԱՅԼ 2: ԾԱՌԱՅՈՒԹՅՈՒՆՆԵՐ (SERVICES) ---
        Sheet srvSheet = workbook.getSheet("Services");
        if (srvSheet != null) {
            for (int i = 1; i <= srvSheet.getLastRowNum(); i++) {
                Row row = srvSheet.getRow(i);
                if (row == null) continue;

                String srvCode = getStr(row, 0);
                Optional<ClinicService> existingSrv = clinicServiceRepository.findByCode(srvCode);
                if (existingSrv.isPresent()) {
                    srvCodeToId.put(srvCode, existingSrv.get().getId());
                    System.out.println("Ծառայությունը արդեն կա բազայում, վերցնում ենք հին ID-ն: " + srvCode);
                    continue;
                }

                String deptCode = getStr(row, 1);
                ClinicService service = ClinicService.builder()
                        .code(getStr(row, 0))
                        .departmentId(deptCodeToId.get(deptCode)) // ԿԱՊԸ
                        .name(getStr(row, 2))
                        .description(getStr(row, 5))
                        .preparationRules(getStr(row, 8))
                        .price(getDouble(row, 11))
                        .durationMinutes((int) getDouble(row, 12))
                        .build();

//                service.getNameTranslations().put("ru", getStr(row, 3));
//                service.getNameTranslations().put("en", getStr(row, 4));
//                service.getDescTranslations().put("ru", getStr(row, 6));
//                service.getDescTranslations().put("en", getStr(row, 7));
//                service.getPrepTranslations().put("ru", getStr(row, 9));
//                service.getPrepTranslations().put("en", getStr(row, 10));

                ClinicService saved = clinicServiceRepository.save(service);
                srvCodeToId.put(service.getCode(), saved.getId());
            }
        }

        // --- ՔԱՅԼ 3: ԲԺԻՇԿՆԵՐ (DOCTORS) ---
        Sheet docSheet = workbook.getSheet("Doctors");
        if (docSheet != null) {
            for (int i = 1; i <= docSheet.getLastRowNum(); i++) {
                Row row = docSheet.getRow(i);
                if (row == null) continue;

                String email = getStr(row, 0);
                if (userRepository.existsByEmail(email)) {
                    System.out.println("Բժիշկը արդեն կա բազայում, բաց ենք թողնում: " + email);
                    continue;
                }
                String tempPassword = generateAlphanumericCode(8);

                // 1. Ստեղծում ենք User (Login account)
                User user = User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(tempPassword))
                        .firstName(getStr(row, 1))
                        .lastName(getStr(row, 4))
                        .phone(getStr(row, 21))
                        .role("ROLE_DOCTOR")
                        .enabled(true)
                        .forcePasswordChange(true)
                        .build();
                User savedUser = userRepository.save(user);

                // 2. Ծառայությունների կոդերը դարձնում ենք իրական ID-ներ
                String srvCodesRaw = getStr(row, 11);
                List<String> realServiceIds = Arrays.stream(srvCodesRaw.split(","))
                        .map(String::trim)
                        .map(srvCodeToId::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // 3. Ստեղծում ենք Doctor պրոֆիլը
                Doctor doctor = Doctor.builder()
                        .userId(savedUser.getId())
                        .departmentId(deptCodeToId.get(getStr(row, 10)))
                        .firstName(getStr(row, 1))
                        .lastName(getStr(row, 4))
                        .patronymic(getStr(row, 7))
                        .email(email)
                        .phone(getStr(row, 21))
                        .roomNumber(getStr(row, 22))
                        .experienceYears((int) getDouble(row, 23))
                        .specialization(getStr(row, 12))
                        .description(getStr(row, 15))
                        .education(getStr(row, 18))
                        .serviceIds(realServiceIds)
                        .languagesSpoken(Arrays.asList(getStr(row, 24).split(",")))
                        .build();

                // Լցնում ենք թարգմանությունները
//                doctor.getFirstNameTrans().put("ru", getStr(row, 2));
//                doctor.getFirstNameTrans().put("en", getStr(row, 3));
//                doctor.getLastNameTrans().put("ru", getStr(row, 5));
//                doctor.getLastNameTrans().put("en", getStr(row, 6));
//                doctor.getPatronymicTrans().put("ru", getStr(row, 8));
//                doctor.getPatronymicTrans().put("en", getStr(row, 9));
//                doctor.getSpecTrans().put("ru", getStr(row, 13));
//                doctor.getSpecTrans().put("en", getStr(row, 14));
//                doctor.getDescTrans().put("ru", getStr(row, 16));
//                doctor.getDescTrans().put("en", getStr(row, 17));
//                doctor.getEduTrans().put("ru", getStr(row, 19));
//                doctor.getEduTrans().put("en", getStr(row, 20));

                // Ավելացնում ենք Email-ի ուղարկումը
                String subject = "Գրանցում կլինիկայի համակարգում / Clinic Registration";
                String body = String.format("""
        Հարգելի բժիշկ %s %s,
        
        Դուք հաջողությամբ գրանցվել եք կլինիկայի համակարգում:
        Ձեր մուտքային տվյալներն են.
        
        Մուտքանուն (Email): %s
        Գաղտնաբառ (Password): %s
        
        Խնդրում ենք մուտք գործելուց հետո փոխել Ձեր գաղտնաբառը:
        """, doctor.getFirstName(), doctor.getLastName(), email, tempPassword);

// Ուղարկում ենք ֆոնային ռեժիմում, որպեսզի իմպորտը չկախի
                new Thread(() -> {
                    try {
                        emailService.sendSimpleMessage(email, subject, body);
                        System.out.println("Email sent to imported doctor: " + email);
                    } catch (Exception e) {
                        System.out.println("Failed to send email to " + email + ": " + e.getMessage());
                    }
                }).start();
                doctorRepository.save(doctor);
            }
        }
        workbook.close();
    }
    }

    // --- ՕԳՆՈՂ ՄԵԹՈԴՆԵՐ ---

    private String getStr(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            // Հեռացնում ենք .0-ն, եթե թիվը ամբողջական է (օր. սենյակի համարը)
            double val = cell.getNumericCellValue();
            if (val == (long) val) return String.format("%d", (long) val);
            return String.valueOf(val);
        }
        return cell.toString().trim();
    }

    private double getDouble(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return 0.0;
        return cell.getNumericCellValue();
    }

    private String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public int importIndicators(MultipartFile file) throws IOException {
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // Վերցնում ենք Excel-ի առաջին էջը
            Sheet sheet = workbook.getSheetAt(0);
            List<Indicator> indicatorsToSave = new ArrayList<>();

            // Սկսում ենք 1-ին տողից, որ վերնագրերը բաց թողնենք
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String testName = getStr(row, 0);
                String rawKey = getStr(row, 1);
                String displayName = getStr(row, 2);

                // Եթե կրիտիկական դաշտերը դատարկ են, բաց ենք թողնում
                if (testName.isEmpty() || rawKey.isEmpty() || displayName.isEmpty()) {
                    continue;
                }

                // Մաքրում ենք բանալին ադմինի հնարավոր սխալներից
                String safeKey = rawKey.trim().toLowerCase().replaceAll("\\s+", "");

                // ՍՏՈՒԳՈՒՄ. Որպեսզի նույն բանալիով նորմա չկրկնվի բազայում
                Optional<Indicator> existing = indicatorRepository.findAll().stream()
                        .filter(ind -> safeKey.equals(ind.getIndicatorKey()) && testName.equals(ind.getTestName()))
                        .findFirst();

                Indicator indicator;
                if (existing.isPresent()) {
                    indicator = existing.get(); // Եթե կա, վերցնում ենք հինը՝ թարմացնելու համար
                } else {
                    indicator = new Indicator();
                    indicator.setIndicatorKey(safeKey);
                    indicator.setActive(true);
                }

                // Լցնում ենք տվյալները Excel-ից
                indicator.setTestName(testName);
                indicator.setDisplayName(displayName);
                indicator.setUnit(getStr(row, 3));

                // Ստուգում ենք Min / Max թվերը (եթե դատարկ չեն)
                Cell minCell = row.getCell(4);
                Cell maxCell = row.getCell(5);

                if (minCell != null && minCell.getCellType() == CellType.NUMERIC) {
                    indicator.setNormMin(getDouble(row, 4));
                } else {
                    indicator.setNormMin(null); // Տեքստային անալիզների համար
                }

                if (maxCell != null && maxCell.getCellType() == CellType.NUMERIC) {
                    indicator.setNormMax(getDouble(row, 5));
                } else {
                    indicator.setNormMax(null);
                }

                indicatorsToSave.add(indicator);
                count++;
            }

            if (!indicatorsToSave.isEmpty()) {
                indicatorRepository.saveAll(indicatorsToSave);
            }
        }
        return count;
    }
}