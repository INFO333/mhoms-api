package com.mhoms.mhomsservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for PatientController
 * Tests HTTP endpoints with security
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Patient Controller Integration Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Emma Thompson");
        testPatient.setAge(32);
        testPatient.setGender("Female");
        testPatient.setPhone("9876543210");
        testPatient.setEmail("emma@example.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should create patient successfully")
    void testCreatePatient_AsAdmin_Success() throws Exception {
        // Arrange
        when(patientService.createPatient(any(Patient.class))).thenReturn(testPatient);

        // Act & Assert
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Emma Thompson"))
                .andExpect(jsonPath("$.email").value("emma@example.com"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should not be able to create patient - 403")
    void testCreatePatient_AsPatient_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN can create patients")));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should not be able to create patient - 403")
    void testCreatePatient_AsDoctor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN can create patients")));
    }

    @Test
    @DisplayName("Unauthenticated user should not access patient endpoints - 401")
    void testCreatePatient_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication required")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should validate patient data - missing name")
    void testCreatePatient_ValidationError_MissingName() throws Exception {
        // Arrange
        testPatient.setName(""); // Invalid!

        // Act & Assert
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should validate patient data - invalid email")
    void testCreatePatient_ValidationError_InvalidEmail() throws Exception {
        // Arrange
        testPatient.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should get all patients successfully")
    void testGetAllPatients_AsAdmin_Success() throws Exception {
        // Arrange
        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("David Martinez");

        List<Patient> patients = Arrays.asList(testPatient, patient2);
        when(patientService.getAllPatients()).thenReturn(patients);

        // Act & Assert
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Emma Thompson"))
                .andExpect(jsonPath("$[1].name").value("David Martinez"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should get all patients successfully")
    void testGetAllPatients_AsDoctor_Success() throws Exception {
        // Arrange
        when(patientService.getAllPatients()).thenReturn(Arrays.asList(testPatient));

        // Act & Assert
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should not get all patients - 403")
    void testGetAllPatients_AsPatient_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/patients"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN and DOCTOR can view patients")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get patient by ID successfully")
    void testGetPatientById_Success() throws Exception {
        // Arrange
        when(patientService.getPatientById(1L)).thenReturn(testPatient);

        // Act & Assert
        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Emma Thompson"));
    }
}