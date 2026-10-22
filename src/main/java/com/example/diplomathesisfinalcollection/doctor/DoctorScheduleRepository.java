package com.example.diplomathesisfinalcollection.doctor;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DoctorScheduleRepository extends MongoRepository<DoctorSchedule, String> {
    // Գտնել կոնկրետ բժշկի գրաֆիկը ամբողջ շաբաթվա համար
    List<DoctorSchedule> findByDoctorId(String doctorId);

    // Գտնել կոնկրետ բժշկի գրաֆիկը կոնկրետ օրվա համար
    DoctorSchedule findByDoctorIdAndDayOfWeek(String doctorId, String dayOfWeek);
}