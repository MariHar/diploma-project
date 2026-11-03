package com.example.diplomathesisfinalcollection.appointment; // Հարմարեցրու քո փաթեթին

import com.example.diplomathesisfinalcollection.doctor.Doctor;
import com.example.diplomathesisfinalcollection.doctor.DoctorRepository;
import com.example.diplomathesisfinalcollection.services.EmailService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentReminderService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    // Թեստային ռեժիմ՝ ֆունկցիան աշխատում է ամեն րոպե (cron-ը՝ "0 * * * * *")
    @Scheduled(cron = "0 * * * * *")
    public void send24HourReminders() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Ժամանակի շեղում՝ +8 կամ -8 ժամ, կախված բազայի ժամային գոտու սխալից
        // Եթե նախորդ անգամ նամակում ժամը 8 ժամ ԱՌԱՋ էր (օրինակ 14:00-ի փոխարեն 22:00), դիր plusHours(8)
        // Եթե 8 ժամ ՀԵՏ էր, դիր minusHours(8)
        LocalDateTime targetStart = now.plusHours(24).minusHours(8); // ՓՈԽԻՐ ՍԱ ԸՍՏ ՔՈ ԴԵՊՔԻ
        LocalDateTime targetEnd = targetStart.plusMinutes(2); // 2 րոպե պատուհան՝ բաց չթողնելու համար

        // Վրիպազերծման տեղեկատվություն. ցույց է տալիս բազայում փնտրվող ժամանակահատվածը
        System.out.println("[CRON] Փնտրում եմ այցեր բազայում: Սկիզբ - " + targetStart + " | Ավարտ - " + targetEnd);

        List<Appointment> upcomingAppointments = appointmentRepository
                .findByDateBetweenAndStatusAndWasNotifiedFalse(targetStart, targetEnd, "CONFIRMED");

        if (upcomingAppointments.isEmpty()) {
            System.out.println("❌ Այս րոպեին համապատասխանող այց չգտնվեց բազայում:");
        }

        for (Appointment appt : upcomingAppointments) {
            User patient = userRepository.findById(appt.getPatientId()).orElse(null);
            Doctor doctor = doctorRepository.findById(appt.getDoctorId()).orElse(null);

            if (patient != null && doctor != null && patient.getEmail() != null) {

                // Վերադարձնում ենք ճիշտ ժամը նամակում ցուցադրելու համար.
                // նշանը հակառակ է վերևի շեղմանը (+8 ընդդեմ -8)
                LocalDateTime correctDisplayTime = appt.getDate().plusHours(8);

                String subject = "⏰ Reminder: Upcoming Appointment Tomorrow";
                String body = String.format("Hello %s,\nReminder for your appointment tomorrow with Dr. %s at %s.",
                        patient.getFirstName(),
                        doctor.getLastName(),
                        correctDisplayTime.format(DateTimeFormatter.ofPattern("h:mm a")));

                emailService.sendSimpleMessage(patient.getEmail(), subject, body);

                appt.setWasNotified(true);
                appointmentRepository.save(appt);
                System.out.println("✅ Հիշեցումը հաջողությամբ ուղարկվեց: " + patient.getEmail());
            }
        }
    }
}