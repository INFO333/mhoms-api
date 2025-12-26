package com.mhoms.mhomsservices.controller;

import com.mhoms.mhomsservices.model.Role;
import com.mhoms.mhomsservices.repository.UserRepository;
import com.mhoms.mhomsservices.service.AppointmentService;
import com.mhoms.mhomsservices.service.DoctorService;
import com.mhoms.mhomsservices.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "APIs for admin dashboard statistics")
public class DashboardController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public DashboardController(PatientService patientService, DoctorService doctorService,
                               AppointmentService appointmentService, UserRepository userRepository) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get complete dashboard statistics")
    @GetMapping("/stats")
    public DashboardStats getDashboardStats() {
        // Using Record constructor directly (NOT builder pattern)
        return new DashboardStats(
                // Patient stats
                patientService.countAll(),
                patientService.countByGender("Male"),
                patientService.countByGender("Female"),
                patientService.getPatientsCreatedToday().size(),
                // Doctor stats
                doctorService.countAll(),
                doctorService.countActive(),
                doctorService.countInactive(),
                // Appointment stats
                appointmentService.countAll(),
                appointmentService.countByStatus("BOOKED"),
                appointmentService.countByStatus("COMPLETED"),
                appointmentService.countByStatus("CANCELLED"),
                appointmentService.countTodaysAppointments(),
                // User stats
                userRepository.count(),
                userRepository.countByRole(Role.ADMIN),
                userRepository.countByRole(Role.DOCTOR),
                userRepository.countByRole(Role.PATIENT),
                // Metadata
                LocalDateTime.now()
        );
    }

    @Operation(summary = "Get quick summary for dashboard widgets")
    @GetMapping("/summary")
    public QuickSummary getQuickSummary() {
        return new QuickSummary(
                patientService.countAll(),
                doctorService.countActive(),
                appointmentService.countTodaysAppointments(),
                appointmentService.countByStatus("BOOKED")
        );
    }

    // Record classes for response (NO builder needed - use constructor)
    public record DashboardStats(
            long totalPatients,
            long malePatients,
            long femalePatients,
            long patientsCreatedToday,
            long totalDoctors,
            long activeDoctors,
            long inactiveDoctors,
            long totalAppointments,
            long bookedAppointments,
            long completedAppointments,
            long cancelledAppointments,
            long todaysAppointments,
            long totalUsers,
            long adminUsers,
            long doctorUsers,
            long patientUsers,
            LocalDateTime generatedAt
    ) {}

    public record QuickSummary(
            long totalPatients,
            long activeDoctors,
            long todaysAppointments,
            long pendingAppointments
    ) {}
}