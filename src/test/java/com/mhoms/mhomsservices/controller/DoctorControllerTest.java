package com.mhoms.mhomsservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.service.DoctorService;
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
 * Integration tests for DoctorController
 * Tests HTTP endpoints with role-based security
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Doctor Controller Integration Tests")
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setName("Dr. Michael Chen");
        testDoctor.setSpecialization("Cardiology");
        testDoctor.setPhone("9988776655");
        testDoctor.setEmail("dr.chen@hospital.com");
        testDoctor.setActive(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should create doctor successfully")
    void testCreateDoctor_AsAdmin_Success() throws Exception {
        // Arrange
        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(testDoctor);

        // Act & Assert
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Dr. Michael Chen"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.email").value("dr.chen@hospital.com"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should not be able to create doctor - 403")
    void testCreateDoctor_AsDoctor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN can register doctors")));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should not be able to create doctor - 403")
    void testCreateDoctor_AsPatient_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN can register doctors")));
    }

    @Test
    @DisplayName("Unauthenticated user should not create doctor - 401")
    void testCreateDoctor_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication required")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should get all doctors successfully")
    void testGetAllDoctors_AsAdmin_Success() throws Exception {
        // Arrange
        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Lisa Anderson");
        doctor2.setSpecialization("Pediatrics");

        List<Doctor> doctors = Arrays.asList(testDoctor, doctor2);
        when(doctorService.getAllDoctors()).thenReturn(doctors);

        // Act & Assert
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Dr. Michael Chen"))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$[1].name").value("Dr. Lisa Anderson"))
                .andExpect(jsonPath("$[1].specialization").value("Pediatrics"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should get all doctors successfully")
    void testGetAllDoctors_AsDoctor_Success() throws Exception {
        // Arrange
        when(doctorService.getAllDoctors()).thenReturn(Arrays.asList(testDoctor));

        // Act & Assert
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Dr. Michael Chen"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should get all doctors successfully")
    void testGetAllDoctors_AsPatient_Success() throws Exception {
        // Arrange
        when(doctorService.getAllDoctors()).thenReturn(Arrays.asList(testDoctor));

        // Act & Assert
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get doctor by ID successfully")
    void testGetDoctorById_Success() throws Exception {
        // Arrange
        when(doctorService.getDoctorById(1L)).thenReturn(testDoctor);

        // Act & Assert
        mockMvc.perform(get("/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Dr. Michael Chen"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should get doctor by ID successfully")
    void testGetDoctorById_AsPatient_Success() throws Exception {
        // Arrange
        when(doctorService.getDoctorById(1L)).thenReturn(testDoctor);

        // Act & Assert
        mockMvc.perform(get("/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Michael Chen"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return null for non-existent doctor ID")
    void testGetDoctorById_NotFound() throws Exception {
        // Arrange
        when(doctorService.getDoctorById(999L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/doctors/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create inactive doctor")
    void testCreateDoctor_InactiveDoctor() throws Exception {
        // Arrange
        testDoctor.setActive(false);
        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(testDoctor);

        // Act & Assert
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(false));
    }
}