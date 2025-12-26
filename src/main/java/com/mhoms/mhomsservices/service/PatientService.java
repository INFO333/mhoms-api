package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.exception.ResourceNotFoundException;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create a new patient
     * Validates for duplicate email and phone
     */
    public Patient createPatient(Patient patient) {
        // Check for duplicate email
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new IllegalStateException(
                    "Patient with email '" + patient.getEmail() + "' already exists"
            );
        }

        // Check for duplicate phone
        if (patientRepository.existsByPhone(patient.getPhone())) {
            throw new IllegalStateException(
                    "Patient with phone '" + patient.getPhone() + "' already exists"
            );
        }

        return patientRepository.save(patient);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ OPERATIONS (WITHOUT PAGINATION - BACKWARD COMPATIBLE)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all patients (no pagination)
     * Backward compatible with existing code
     */
    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Get patient by ID
     */
    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + id
                ));
    }

    // ═══════════════════════════════════════════════════════════════
    // READ OPERATIONS (WITH PAGINATION - NEW)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all patients with pagination
     * Usage: GET /patients/page?page=0&size=10&sort=name,asc
     */
    @Transactional(readOnly = true)
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    /**
     * Search patients by name
     * Usage: GET /patients/search?name=john&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Patient> searchByName(String name, Pageable pageable) {
        return patientRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Search patients by gender
     * Usage: GET /patients/search?gender=male&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Patient> searchByGender(String gender, Pageable pageable) {
        return patientRepository.findByGenderIgnoreCase(gender, pageable);
    }

    /**
     * Search patients by age range
     * Usage: GET /patients/search?minAge=18&maxAge=65&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<Patient> searchByAgeRange(Integer minAge, Integer maxAge, Pageable pageable) {
        return patientRepository.findByAgeBetween(minAge, maxAge, pageable);
    }

    /**
     * Advanced search with multiple criteria
     * All parameters are optional
     */
    @Transactional(readOnly = true)
    public Page<Patient> searchPatients(
            String name,
            String gender,
            Integer minAge,
            Integer maxAge,
            Pageable pageable) {
        return patientRepository.searchPatients(name, gender, minAge, maxAge, pageable);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Update patient details
     */
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);

        // Check for duplicate email (if changed)
        if (!patient.getEmail().equals(patientDetails.getEmail())
                && patientRepository.existsByEmail(patientDetails.getEmail())) {
            throw new IllegalStateException(
                    "Patient with email '" + patientDetails.getEmail() + "' already exists"
            );
        }

        // Check for duplicate phone (if changed)
        if (!patient.getPhone().equals(patientDetails.getPhone())
                && patientRepository.existsByPhone(patientDetails.getPhone())) {
            throw new IllegalStateException(
                    "Patient with phone '" + patientDetails.getPhone() + "' already exists"
            );
        }

        // Update fields
        patient.setName(patientDetails.getName());
        patient.setAge(patientDetails.getAge());
        patient.setGender(patientDetails.getGender());
        patient.setPhone(patientDetails.getPhone());
        patient.setEmail(patientDetails.getEmail());

        return patientRepository.save(patient);
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Delete patient by ID
     */
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Count total patients
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return patientRepository.count();
    }

    /**
     * Count patients by gender
     */
    @Transactional(readOnly = true)
    public long countByGender(String gender) {
        return patientRepository.countByGenderIgnoreCase(gender);
    }

    /**
     * Get patients created today
     */
    @Transactional(readOnly = true)
    public List<Patient> getPatientsCreatedToday() {
        return patientRepository.findPatientsCreatedToday();
    }
}