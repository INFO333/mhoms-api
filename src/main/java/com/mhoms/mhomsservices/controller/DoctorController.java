package com.mhoms.mhomsservices.controller;

import com.mhoms.mhomsservices.dto.PageResponse;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@Tag(
        name = "Doctor Management",
        description = "APIs for managing doctor profiles, specializations, and availability"
)
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Register a new doctor",
            description = "Adds a new doctor to the hospital system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid doctor data"),
            @ApiResponse(responseCode = "409", description = "Doctor with email/phone already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorService.createDoctor(doctor);
    }

    // ═══════════════════════════════════════════════════════════════
    // READ ENDPOINTS (BACKWARD COMPATIBLE)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get all doctors",
            description = "Retrieves all doctors without pagination"
    )
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @Operation(
            summary = "Get doctor by ID",
            description = "Retrieves detailed information about a specific doctor"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor found"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/{id}")
    public Doctor getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // PAGINATION ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get doctors with pagination",
            description = "Retrieves doctors with pagination support"
    )
    @GetMapping("/page")
    public PageResponse<Doctor> getDoctorsPage(
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
        Page<Doctor> doctorPage = doctorService.getAllDoctors(pageable);

        return PageResponse.of(doctorPage);
    }

    // ═══════════════════════════════════════════════════════════════
    // SEARCH ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Search doctors",
            description = "Advanced search with multiple optional criteria"
    )
    @GetMapping("/search")
    public PageResponse<Doctor> searchDoctors(
            @Parameter(description = "Search by name (partial match)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by specialization")
            @RequestParam(required = false) String specialization,

            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active,

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
        Page<Doctor> result = doctorService.searchDoctors(name, specialization, active, pageable);

        return PageResponse.of(result);
    }

    // ═══════════════════════════════════════════════════════════════
    // SPECIALIZATION ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get all specializations",
            description = "Returns list of all unique doctor specializations"
    )
    @GetMapping("/specializations")
    public List<String> getAllSpecializations() {
        return doctorService.getAllSpecializations();
    }

    @Operation(
            summary = "Get doctors by specialization",
            description = "Returns all doctors with a specific specialization"
    )
    @GetMapping("/specialization/{specialization}")
    public PageResponse<Doctor> getBySpecialization(
            @PathVariable String specialization,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> result = doctorService.searchBySpecialization(specialization, pageable);
        return PageResponse.of(result);
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTIVE STATUS ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get active doctors",
            description = "Returns all currently active doctors"
    )
    @GetMapping("/active")
    public List<Doctor> getActiveDoctors() {
        return doctorService.getActiveDoctors();
    }

    @Operation(
            summary = "Toggle doctor active status",
            description = "Activates or deactivates a doctor"
    )
    @PatchMapping("/{id}/toggle-status")
    public Doctor toggleActiveStatus(@PathVariable Long id) {
        return doctorService.toggleActiveStatus(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Update doctor",
            description = "Updates an existing doctor's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor updated successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "409", description = "Email/phone already in use")
    })
    @PutMapping("/{id}")
    public Doctor updateDoctor(
            @PathVariable Long id,
            @RequestBody Doctor doctor
    ) {
        return doctorService.updateDoctor(id, doctor);
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Delete doctor",
            description = "Permanently deletes a doctor record"
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS ENDPOINTS (NEW)
    // ═══════════════════════════════════════════════════════════════

    @Operation(
            summary = "Get doctor statistics",
            description = "Returns count statistics for doctors"
    )
    @GetMapping("/stats")
    public DoctorStats getDoctorStats() {
        return new DoctorStats(
                doctorService.countAll(),
                doctorService.countActive(),
                doctorService.countInactive(),
                doctorService.getAllSpecializations().size()
        );
    }

    // Inner class for stats response
    public record DoctorStats(
            long totalDoctors,
            long activeDoctors,
            long inactiveDoctors,
            int totalSpecializations
    ) {}
}