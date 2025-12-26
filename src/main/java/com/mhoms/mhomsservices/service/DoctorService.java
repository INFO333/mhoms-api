package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.exception.ResourceNotFoundException;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create a new doctor
     * Validates for duplicate email and phone
     */
    public Doctor createDoctor(Doctor doctor) {
        // Check for duplicate email
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new IllegalStateException(
                    "Doctor with email '" + doctor.getEmail() + "' already exists"
            );
        }

        // Check for duplicate phone
        if (doctorRepository.existsByPhone(doctor.getPhone())) {
            throw new IllegalStateException(
                    "Doctor with phone '" + doctor.getPhone() + "' already exists"
            );
        }

        // Set default active status if not provided
        if (doctor.getActive() == null) {
            doctor.setActive(true);
        }

        return doctorRepository.save(doctor);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ OPERATIONS (WITHOUT PAGINATION - BACKWARD COMPATIBLE)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all doctors (no pagination)
     * Backward compatible with existing code
     */
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Get doctor by ID
     */
    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + id
                ));
    }

    // ═══════════════════════════════════════════════════════════════
    // READ OPERATIONS (WITH PAGINATION - NEW)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all doctors with pagination
     * Usage: GET /doctors/page?page=0&size=10&sort=name,asc
     */
    @Transactional(readOnly = true)
    public Page<Doctor> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    /**
     * Search doctors by name
     * Usage: GET /doctors/search?name=sarah&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Doctor> searchByName(String name, Pageable pageable) {
        return doctorRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Search doctors by specialization
     * Usage: GET /doctors/search?specialization=cardio&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Doctor> searchBySpecialization(String specialization, Pageable pageable) {
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization, pageable);
    }

    /**
     * Search doctors by active status
     * Usage: GET /doctors/search?active=true&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Doctor> searchByActiveStatus(Boolean active, Pageable pageable) {
        return doctorRepository.findByActive(active, pageable);
    }

    /**
     * Advanced search with multiple criteria
     * All parameters are optional
     */
    @Transactional(readOnly = true)
    public Page<Doctor> searchDoctors(
            String name,
            String specialization,
            Boolean active,
            Pageable pageable) {
        return doctorRepository.searchDoctors(name, specialization, active, pageable);
    }

    /**
     * Get all unique specializations
     */
    @Transactional(readOnly = true)
    public List<String> getAllSpecializations() {
        return doctorRepository.findAllSpecializations();
    }

    /**
     * Get all active doctors (no pagination)
     */
    @Transactional(readOnly = true)
    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByActiveTrue();
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Update doctor details
     */
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);

        // Check for duplicate email (if changed)
        if (!doctor.getEmail().equals(doctorDetails.getEmail())
                && doctorRepository.existsByEmail(doctorDetails.getEmail())) {
            throw new IllegalStateException(
                    "Doctor with email '" + doctorDetails.getEmail() + "' already exists"
            );
        }

        // Check for duplicate phone (if changed)
        if (!doctor.getPhone().equals(doctorDetails.getPhone())
                && doctorRepository.existsByPhone(doctorDetails.getPhone())) {
            throw new IllegalStateException(
                    "Doctor with phone '" + doctorDetails.getPhone() + "' already exists"
            );
        }

        // Update fields
        doctor.setName(doctorDetails.getName());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setPhone(doctorDetails.getPhone());
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setActive(doctorDetails.getActive());

        return doctorRepository.save(doctor);
    }

    /**
     * Toggle doctor active status
     */
    public Doctor toggleActiveStatus(Long id) {
        Doctor doctor = getDoctorById(id);
        doctor.setActive(!doctor.getActive());
        return doctorRepository.save(doctor);
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Delete doctor by ID
     */
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Count total doctors
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return doctorRepository.count();
    }

    /**
     * Count active doctors
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return doctorRepository.countByActiveTrue();
    }

    /**
     * Count inactive doctors
     */
    @Transactional(readOnly = true)
    public long countInactive() {
        return doctorRepository.countByActiveFalse();
    }

    /**
     * Count doctors by specialization
     */
    @Transactional(readOnly = true)
    public long countBySpecialization(String specialization) {
        return doctorRepository.countBySpecializationIgnoreCase(specialization);
    }
}