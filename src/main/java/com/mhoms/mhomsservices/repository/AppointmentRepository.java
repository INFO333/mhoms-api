package com.mhoms.mhomsservices.repository;

import com.mhoms.mhomsservices.model.Appointment;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ═══════════════════════════════════════════════════════════════
    // PAGINATION METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all appointments with pagination
     * Usage: GET /appointments?page=0&size=10&sort=appointmentDate,desc
     */
    Page<Appointment> findAll(Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // DUPLICATE PREVENTION (EXISTING)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check if doctor already has an appointment at given time
     */
    boolean existsByDoctorAndAppointmentDate(Doctor doctor, LocalDateTime appointmentDate);

    /**
     * Alternative check using doctor ID
     */
    boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDateTime appointmentDate);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY STATUS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find appointments by status
     * Usage: GET /appointments?status=BOOKED
     */
    Page<Appointment> findByStatusIgnoreCase(String status, Pageable pageable);

    /**
     * Find appointments by status (list)
     */
    List<Appointment> findByStatusIgnoreCase(String status);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY PATIENT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find all appointments for a patient
     * Usage: GET /appointments?patientId=1
     */
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    /**
     * Find appointments for a patient (list)
     */
    List<Appointment> findByPatient(Patient patient);

    /**
     * Find appointments for patient by status
     */
    Page<Appointment> findByPatientIdAndStatusIgnoreCase(Long patientId, String status, Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY DOCTOR
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find all appointments for a doctor
     * Usage: GET /appointments?doctorId=1
     */
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    /**
     * Find appointments for a doctor (list)
     */
    List<Appointment> findByDoctor(Doctor doctor);

    /**
     * Find appointments for doctor by status
     */
    Page<Appointment> findByDoctorIdAndStatusIgnoreCase(Long doctorId, String status, Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY DATE RANGE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find appointments in date range
     * Usage: GET /appointments?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59
     */
    Page<Appointment> findByAppointmentDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find appointments after a specific date
     */
    Page<Appointment> findByAppointmentDateAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find appointments before a specific date
     */
    Page<Appointment> findByAppointmentDateBefore(LocalDateTime date, Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // TODAY'S APPOINTMENTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find today's appointments
     */
    // ⭐ FIXED: Use CAST instead of DATE()
    @Query("SELECT a FROM Appointment a WHERE CAST(a.appointmentDate AS date) = CURRENT_DATE ORDER BY a.appointmentDate")
    List<Appointment> findTodaysAppointments();

    /**
     * Find today's appointments for a doctor
     */
    // ⭐ FIXED: Use CAST instead of DATE()
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND CAST(a.appointmentDate AS date) = CURRENT_DATE ORDER BY a.appointmentDate")
    List<Appointment> findTodaysAppointmentsByDoctor(@Param("doctorId") Long doctorId);

    // ═══════════════════════════════════════════════════════════════
    // UPCOMING APPOINTMENTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find upcoming appointments (future)
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate > CURRENT_TIMESTAMP AND a.status = 'BOOKED' ORDER BY a.appointmentDate")
    Page<Appointment> findUpcomingAppointments(Pageable pageable);

    /**
     * Find upcoming appointments for a patient
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate > CURRENT_TIMESTAMP AND a.status = 'BOOKED' ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") Long patientId);

    /**
     * Find upcoming appointments for a doctor
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate > CURRENT_TIMESTAMP AND a.status = 'BOOKED' ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingAppointmentsByDoctor(@Param("doctorId") Long doctorId);

    // ═══════════════════════════════════════════════════════════════
    // ADVANCED SEARCH
    // ═══════════════════════════════════════════════════════════════

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
            "(:status IS NULL OR LOWER(a.status) = LOWER(:status)) AND " +
            "(:startDate IS NULL OR a.appointmentDate >= :startDate) AND " +
            "(:endDate IS NULL OR a.appointmentDate <= :endDate)")
    Page<Appointment> searchAppointments(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Count appointments by status
     */
    long countByStatusIgnoreCase(String status);

    /**
     * Count appointments for a doctor
     */
    long countByDoctorId(Long doctorId);

    /**
     * Count appointments for a patient
     */
    long countByPatientId(Long patientId);

    /**
     * Count today's appointments
     */
    // ⭐ FIXED: Use CAST instead of DATE()
    @Query("SELECT COUNT(a) FROM Appointment a WHERE CAST(a.appointmentDate AS date) = CURRENT_DATE")
    long countTodaysAppointments();
}