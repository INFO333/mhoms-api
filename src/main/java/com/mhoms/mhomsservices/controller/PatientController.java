package com.mhoms.mhomsservices.controller;

import com.mhoms.mhomsservices.dto.PageResponse;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@Tag(
        name = "Patient Management",
        description = "APIs for managing patient records including registration, retrieval, search, and pagination"
)
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Register a new patient",
            description = "Creates a new patient record with personal information and contact details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid patient data"),
            @ApiResponse(responseCode = "409", description = "Patient with email/phone already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient createPatient(@Valid @RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ ENDPOINTS (BACKWARD COMPATIBLE)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get all patients",
            description = "Retrieves all patients without pagination (for backward compatibility)"
    )
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @Operation(
            summary = "Get patient by ID",
            description = "Retrieves detailed information about a specific patient"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // PAGINATION ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get patients with pagination",
            description = "Retrieves patients with pagination support. Supports sorting by any field."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of patients retrieved successfully")
    })
    @GetMapping("/page")
    public PageResponse<Patient> getPatientsPage(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Patient> patientPage = patientService.getAllPatients(pageable);

        return PageResponse.of(patientPage);
    }

    // ═══════════════════════════════════════════════════════════════
    // SEARCH ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Search patients",
            description = "Advanced search with multiple optional criteria. All parameters are optional."
    )
    @GetMapping("/search")
    public PageResponse<Patient> searchPatients(
            @Parameter(description = "Search by name (partial match)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by gender (Male/Female)")
            @RequestParam(required = false) String gender,

            @Parameter(description = "Minimum age")
            @RequestParam(required = false) Integer minAge,

            @Parameter(description = "Maximum age")
            @RequestParam(required = false) Integer maxAge,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Patient> result = patientService.searchPatients(name, gender, minAge, maxAge, pageable);

        return PageResponse.of(result);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Update patient",
            description = "Updates an existing patient's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient updated successfully"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "409", description = "Email/phone already in use")
    })
    @PutMapping("/{id}")
    public Patient updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody Patient patient
    ) {
        return patientService.updatePatient(id, patient);
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Delete patient",
            description = "Permanently deletes a patient record"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get patient statistics",
            description = "Returns count statistics for patients"
    )
    @GetMapping("/stats")
    public PatientStats getPatientStats() {
        return new PatientStats(
                patientService.countAll(),
                patientService.countByGender("Male"),
                patientService.countByGender("Female"),
                patientService.getPatientsCreatedToday().size()
        );
    }

    // Inner class for stats response
    public record PatientStats(
            long totalPatients,
            long malePatients,
            long femalePatients,
            long patientsCreatedToday
    ) {}
}