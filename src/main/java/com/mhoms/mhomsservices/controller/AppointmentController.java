package com.mhoms.mhomsservices.controller;

import com.mhoms.mhomsservices.dto.AppointmentStatusRequest;
import com.mhoms.mhomsservices.dto.PageResponse;
import com.mhoms.mhomsservices.model.Appointment;
import com.mhoms.mhomsservices.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@Tag(
        name = "Appointment Management",
        description = "APIs for scheduling, managing, and tracking patient appointments"
)
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Book a new appointment",
            description = "Schedules a new appointment between a patient and a doctor"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment booked successfully"),
            @ApiResponse(responseCode = "404", description = "Patient or Doctor not found"),
            @ApiResponse(responseCode = "409", description = "Doctor already has an appointment at this time")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment bookAppointment(
            @Parameter(description = "Patient ID", required = true, example = "1")
            @RequestParam Long patientId,

            @Parameter(description = "Doctor ID", required = true, example = "1")
            @RequestParam Long doctorId,

            @Parameter(description = "Appointment date/time", required = true, example = "2025-12-27T10:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime appointmentDate
    ) {
        return appointmentService.bookAppointment(patientId, doctorId, appointmentDate);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ ENDPOINTS (BACKWARD COMPATIBLE)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get all appointments",
            description = "Retrieves all appointments without pagination"
    )
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @Operation(
            summary = "Get appointment by ID",
            description = "Retrieves detailed information about a specific appointment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment found"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @GetMapping("/{id}")
    public Appointment getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // PAGINATION ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get appointments with pagination",
            description = "Retrieves appointments with pagination and sorting support"
    )
    @GetMapping("/page")
    public PageResponse<Appointment> getAppointmentsPage(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "appointmentDate")
            @RequestParam(defaultValue = "appointmentDate") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> appointmentPage = appointmentService.getAllAppointments(pageable);

        return PageResponse.of(appointmentPage);
    }

    // ═══════════════════════════════════════════════════════════════
    // SEARCH ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Search appointments",
            description = "Advanced search with multiple optional criteria"
    )
    @GetMapping("/search")
    public PageResponse<Appointment> searchAppointments(
            @Parameter(description = "Filter by patient ID")
            @RequestParam(required = false) Long patientId,

            @Parameter(description = "Filter by doctor ID")
            @RequestParam(required = false) Long doctorId,

            @Parameter(description = "Filter by status (BOOKED/COMPLETED/CANCELLED)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Start date for date range filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @Parameter(description = "End date for date range filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "appointmentDate")
            @RequestParam(defaultValue = "appointmentDate") String sortBy,

            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> result = appointmentService.searchAppointments(
                patientId, doctorId, status, startDate, endDate, pageable
        );

        return PageResponse.of(result);
    }

    // ═══════════════════════════════════════════════════════════════
    // TODAY'S APPOINTMENTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get today's appointments",
            description = "Returns all appointments scheduled for today"
    )
    @GetMapping("/today")
    public List<Appointment> getTodaysAppointments() {
        return appointmentService.getTodaysAppointments();
    }

    @Operation(
            summary = "Get today's appointments for a doctor",
            description = "Returns all appointments scheduled for today for a specific doctor"
    )
    @GetMapping("/today/doctor/{doctorId}")
    public List<Appointment> getTodaysAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentService.getTodaysAppointmentsByDoctor(doctorId);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPCOMING APPOINTMENTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get upcoming appointments",
            description = "Returns all future booked appointments with pagination"
    )
    @GetMapping("/upcoming")
    public PageResponse<Appointment> getUpcomingAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> result = appointmentService.getUpcomingAppointments(pageable);
        return PageResponse.of(result);
    }

    @Operation(
            summary = "Get upcoming appointments for a patient",
            description = "Returns all future booked appointments for a specific patient"
    )
    @GetMapping("/upcoming/patient/{patientId}")
    public List<Appointment> getUpcomingAppointmentsByPatient(@PathVariable Long patientId) {
        return appointmentService.getUpcomingAppointmentsByPatient(patientId);
    }

    @Operation(
            summary = "Get upcoming appointments for a doctor",
            description = "Returns all future booked appointments for a specific doctor"
    )
    @GetMapping("/upcoming/doctor/{doctorId}")
    public List<Appointment> getUpcomingAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentService.getUpcomingAppointmentsByDoctor(doctorId);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Update appointment status",
            description = "Updates the status of an existing appointment (BOOKED/COMPLETED/CANCELLED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    @PutMapping("/{id}/status")
    public Appointment updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusRequest request
    ) {
        return appointmentService.updateAppointmentStatus(id, request.getStatus());
    }

    @Operation(
            summary = "Reschedule appointment",
            description = "Changes the date/time of an existing appointment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment rescheduled successfully"),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "409", description = "Doctor not available at new time")
    })
    @PutMapping("/{id}/reschedule")
    public Appointment rescheduleAppointment(
            @PathVariable Long id,

            @Parameter(description = "New appointment date/time", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime newDate
    ) {
        return appointmentService.rescheduleAppointment(id, newDate);
    }

    @Operation(
            summary = "Cancel appointment",
            description = "Cancels an existing appointment (soft delete)"
    )
    @PutMapping("/{id}/cancel")
    public Appointment cancelAppointment(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Delete appointment",
            description = "Permanently deletes an appointment record"
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get appointment statistics",
            description = "Returns count statistics for appointments"
    )
    @GetMapping("/stats")
    public AppointmentStats getAppointmentStats() {
        return new AppointmentStats(
                appointmentService.countAll(),
                appointmentService.countByStatus("BOOKED"),
                appointmentService.countByStatus("COMPLETED"),
                appointmentService.countByStatus("CANCELLED"),
                appointmentService.countTodaysAppointments()
        );
    }

    // Inner class for stats response
    public record AppointmentStats(
            long totalAppointments,
            long bookedAppointments,
            long completedAppointments,
            long cancelledAppointments,
            long todaysAppointments
    ) {}
}