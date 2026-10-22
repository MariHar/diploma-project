package com.example.diplomathesisfinalcollection.services;

import com.example.diplomathesisfinalcollection.clinicservice.ClinicService;
import com.example.diplomathesisfinalcollection.clinicservice.ClinicServiceRepository;
import com.example.diplomathesisfinalcollection.department.Department;
import com.example.diplomathesisfinalcollection.department.DepartmentRepository;
import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicDataService {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private ClinicServiceRepository clinicServiceRepository; // Սա քո ClinicService-ի repo-ն է

    public String getActiveClinicSnapshot() {
        StringBuilder snapshot = new StringBuilder("--- ԿԼԻՆԻԿԱՅԻ ԱԿՏԻՎ ՏՎՅԱԼՆԵՐ ---\n");

        List<Department> departments = departmentRepository.findAll().stream()
                .filter(Department::isActive).toList();

        for (Department dept : departments) {
            snapshot.append("ԲԱԺԱՆՄՈՒՆՔ: ").append(dept.getName()).append(" (ID: ").append(dept.getId()).append(")\n");

            // Գտնում ենք այս բաժանմունքի ակտիվ բժիշկներին
            List<Doctor> doctors = doctorRepository.findByDepartmentIdAndActiveTrue(dept.getId()).stream()
                    .filter(Doctor::isActive).toList();

            for (Doctor doc : doctors) {
                snapshot.append("  - ԲԺԻՇԿ: ").append(doc.getFirstName()).append(" ").append(doc.getLastName())
                        .append(" (").append(doc.getSpecialization()).append(")\n");
            }

            // Գտնում ենք այս բաժանմունքի ծառայությունները
            List<ClinicService> services = clinicServiceRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(dept.getId()).stream()
                    .filter(ClinicService::isActive).toList();

            for (ClinicService srv : services) {
                snapshot.append("  * ԾԱՌԱՅՈՒԹՅՈՒՆ: ").append(srv.getName())
                        .append(" (Գին: ").append(srv.getPrice()).append(" դրամ)\n");
            }
            snapshot.append("\n");
        }
        return snapshot.toString();
    }
}