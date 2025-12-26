package com.mhoms.mhomsservices.repository;

import com.mhoms.mhomsservices.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // ═══════════════════════════════════════════════════════════════
    // PAGINATION METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all patients with pagination
     * Usage: GET /patients?page=0&size=10&sort=name,asc
     */
    Page<Patient> findAll(Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // SEARCH METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Search patients by name (case-insensitive, partial match)
     * Usage: GET /patients/search?name=john
     */
    Page<Patient> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Search patients by email
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Check if email exists (for duplicate prevention)
     */
    boolean existsByEmail(String email);

    /**
     * Search patients by phone
     */
    Optional<Patient> findByPhone(String phone);

    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);

    /**
     * Search patients by gender
     */
    Page<Patient> findByGenderIgnoreCase(String gender, Pageable pageable);

    /**
     * Search patients by age range
     */
    Page<Patient> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable);

    // ═══════════════════════════════════════════════════════════════
    // CUSTOM QUERIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT p FROM Patient p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:gender IS NULL OR LOWER(p.gender) = LOWER(:gender)) AND " +
            "(:minAge IS NULL OR p.age >= :minAge) AND " +
            "(:maxAge IS NULL OR p.age <= :maxAge)")
    Page<Patient> searchPatients(
            @Param("name") String name,
            @Param("gender") String gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable
    );

    /**
     * Count patients by gender
     */
    long countByGenderIgnoreCase(String gender);

    /**
     * Get patients created today
     */
    // ⭐ FIXED: Use CAST instead of DATE()
    @Query("SELECT p FROM Patient p WHERE CAST(p.createdAt AS date) = CURRENT_DATE")
    List<Patient> findPatientsCreatedToday();
}