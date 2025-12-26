package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.exception.ResourceNotFoundException;
import com.mhoms.mhomsservices.model.Appointment;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.repository.AppointmentRepository;
import com.mhoms.mhomsservices.repository.DoctorRepository;
import com.mhoms.mhomsservices.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Book a new appointment
     * Validates patient, doctor existence and prevents double booking
     */
    public Appointment bookAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate) {
        // Validate patient exists
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId
                ));

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId
                ));

        // Check if doctor is active
        if (!doctor.getActive()) {
            throw new IllegalStateException(
                    "Doctor '" + doctor.getName() + "' is not currently available for appointments"
            );
        }

        // Prevent double booking - check if doctor already has appointment at this time
        if (appointmentRepository.existsByDoctorAndAppointmentDate(doctor, appointmentDate)) {
            throw new IllegalStateException(
                    "Doctor already has an appointment at this time - Please choose a different time slot"
            );
        }

        // Create and save appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStatus("BOOKED");

        return appointmentRepository.save(appointment);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ OPERATIONS (WITHOUT PAGINATION - BACKWARD COMPATIBLE)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all appointments (no pagination)
     * Backward compatible with existing code
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * Get appointment by ID
     */
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + id
                ));
    }

    // ═══════════════════════════════════════════════════════════════
    // READ OPERATIONS (WITH PAGINATION - NEW)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all appointments with pagination
     * Usage: GET /appointments/page?page=0&size=10&sort=appointmentDate,desc
     */
    @Transactional(readOnly = true)
    public Page<Appointment> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    /**
     * Search appointments by status
     * Usage: GET /appointments/search?status=BOOKED&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Appointment> searchByStatus(String status, Pageable pageable) {
        return appointmentRepository.findByStatusIgnoreCase(status.toUpperCase(), pageable);
    }

    /**
     * Search appointments by patient
     * Usage: GET /appointments/search?patientId=1&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Appointment> searchByPatient(Long patientId, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    /**
     * Search appointments by doctor
     * Usage: GET /appointments/search?doctorId=1&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Appointment> searchByDoctor(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }

    /**
     * Search appointments by date range
     * Usage: GET /appointments/search?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59
     */
    @Transactional(readOnly = true)
    public Page<Appointment> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return appointmentRepository.findByAppointmentDateBetween(startDate, endDate, pageable);
    }

    /**
     * Advanced search with multiple criteria
     * All parameters are optional
     */
    @Transactional(readOnly = true)
    public Page<Appointment> searchAppointments(
            Long patientId,
            Long doctorId,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return appointmentRepository.searchAppointments(
                patientId, doctorId, status, startDate, endDate, pageable
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // SPECIAL QUERIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get today's appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointments() {
        return appointmentRepository.findTodaysAppointments();
    }

    /**
     * Get today's appointments for a specific doctor
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findTodaysAppointmentsByDoctor(doctorId);
    }

    /**
     * Get upcoming appointments with pagination
     */
    @Transactional(readOnly = true)
    public Page<Appointment> getUpcomingAppointments(Pageable pageable) {
        return appointmentRepository.findUpcomingAppointments(pageable);
    }

    /**
     * Get upcoming appointments for a patient
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findUpcomingAppointmentsByPatient(patientId);
    }

    /**
     * Get upcoming appointments for a doctor
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findUpcomingAppointmentsByDoctor(doctorId);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Update appointment status
     * Valid statuses: BOOKED, COMPLETED, CANCELLED
     */
    public Appointment updateAppointmentStatus(Long id, String status) {
        Appointment appointment = getAppointmentById(id);

        // Validate status
        String normalizedStatus = status.toUpperCase();
        if (!isValidStatus(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status: " + status + ". Valid values are: BOOKED, COMPLETED, CANCELLED"
            );
        }

        appointment.setStatus(normalizedStatus);
        return appointmentRepository.save(appointment);
    }

    /**
     * Reschedule appointment
     */
    public Appointment rescheduleAppointment(Long id, LocalDateTime newDate) {
        Appointment appointment = getAppointmentById(id);

        // Check if new time is available for the doctor
        if (appointmentRepository.existsByDoctorAndAppointmentDate(
                appointment.getDoctor(), newDate)) {
            throw new IllegalStateException(
                    "Doctor already has an appointment at this time"
            );
        }

        appointment.setAppointmentDate(newDate);
        return appointmentRepository.save(appointment);
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cancel appointment (soft delete - just changes status)
     */
    public Appointment cancelAppointment(Long id) {
        return updateAppointmentStatus(id, "CANCELLED");
    }

    /**
     * Delete appointment permanently
     */
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Count total appointments
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return appointmentRepository.count();
    }

    /**
     * Count appointments by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return appointmentRepository.countByStatusIgnoreCase(status);
    }

    /**
     * Count today's appointments
     */
    @Transactional(readOnly = true)
    public long countTodaysAppointments() {
        return appointmentRepository.countTodaysAppointments();
    }

    /**
     * Count appointments for a doctor
     */
    @Transactional(readOnly = true)
    public long countByDoctor(Long doctorId) {
        return appointmentRepository.countByDoctorId(doctorId);
    }

    /**
     * Count appointments for a patient
     */
    @Transactional(readOnly = true)
    public long countByPatient(Long patientId) {
        return appointmentRepository.countByPatientId(patientId);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    private boolean isValidStatus(String status) {
        return status.equals("BOOKED") ||
                status.equals("COMPLETED") ||
                status.equals("CANCELLED");
    }
}