package com.mhoms.mhomsservices.repository;

import com.mhoms.mhomsservices.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // ═══════════════════════════════════════════════════════════════
    // PAGINATION METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all doctors with pagination
     * Usage: GET /doctors?page=0&size=10&sort=name,asc
     */
    Page<Doctor> findAll(Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY SPECIALIZATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find doctors by specialization (exact match, case-insensitive)
     * Usage: GET /doctors?specialization=Cardiology
     */
    Page<Doctor> findBySpecializationIgnoreCase(String specialization, Pageable pageable);

    /**
     * Find doctors by specialization (partial match)
     * Usage: GET /doctors/search?specialization=cardio
     */
    Page<Doctor> findBySpecializationContainingIgnoreCase(String specialization, Pageable pageable);

    /**
     * Get all unique specializations
     */
    @Query("SELECT DISTINCT d.specialization FROM Doctor d ORDER BY d.specialization")
    List<String> findAllSpecializations();

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY NAME
    // ═══════════════════════════════════════════════════════════════

    /**
     * Search doctors by name (partial match)
     */
    Page<Doctor> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH BY STATUS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find active/inactive doctors
     * Usage: GET /doctors?active=true
     */
    Page<Doctor> findByActive(Boolean active, Pageable pageable);

    /**
     * Find all active doctors (no pagination)
     */
    List<Doctor> findByActiveTrue();

    // ═══════════════════════════════════════════════════════════════
    // DUPLICATE PREVENTION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find doctor by email
     */
    Optional<Doctor> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find doctor by phone
     */
    Optional<Doctor> findByPhone(String phone);

    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);

    // ═══════════════════════════════════════════════════════════════
    // ADVANCED SEARCH
    // ═══════════════════════════════════════════════════════════════

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
            "(:active IS NULL OR d.active = :active)")
    Page<Doctor> searchDoctors(
            @Param("name") String name,
            @Param("specialization") String specialization,
            @Param("active") Boolean active,
            Pageable pageable
    );

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Count doctors by specialization
     */
    long countBySpecializationIgnoreCase(String specialization);

    /**
     * Count active doctors
     */
    long countByActiveTrue();

    /**
     * Count inactive doctors
     */
    long countByActiveFalse();
}