package com.mhoms.mhomsservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhoms.mhomsservices.dto.AppointmentStatusRequest;
import com.mhoms.mhomsservices.model.Appointment;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.service.AppointmentService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AppointmentController
 * Tests appointment booking, retrieval, and status updates
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Appointment Controller Integration Tests")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private Patient testPatient;
    private Doctor testDoctor;
    private LocalDateTime appointmentDate;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Emma Thompson");

        // Setup test doctor
        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setName("Dr. Michael Chen");
        testDoctor.setSpecialization("Cardiology");

        // Setup test appointment
        appointmentDate = LocalDateTime.of(2025, 12, 27, 10, 0);
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setAppointmentDate(appointmentDate);
        testAppointment.setStatus("BOOKED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should book appointment successfully")
    void testBookAppointment_AsAdmin_Success() throws Exception {
        // Arrange
        when(appointmentService.bookAppointment(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(testAppointment);

        // Act & Assert
        mockMvc.perform(post("/appointments")
                        .param("patientId", "1")
                        .param("doctorId", "1")
                        .param("appointmentDate", "2025-12-27T10:00:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andExpect(jsonPath("$.patient.name").value("Emma Thompson"))
                .andExpect(jsonPath("$.doctor.name").value("Dr. Michael Chen"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should book appointment successfully")
    void testBookAppointment_AsPatient_Success() throws Exception {
        // Arrange
        when(appointmentService.bookAppointment(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(testAppointment);

        // Act & Assert
        mockMvc.perform(post("/appointments")
                        .param("patientId", "1")
                        .param("doctorId", "1")
                        .param("appointmentDate", "2025-12-27T10:00:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should not be able to book appointment - 403")
    void testBookAppointment_AsDoctor_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/appointments")
                        .param("patientId", "1")
                        .param("doctorId", "1")
                        .param("appointmentDate", "2025-12-27T10:00:00"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN and PATIENT can book appointments")));
    }

    @Test
    @DisplayName("Unauthenticated user should not book appointment - 401")
    void testBookAppointment_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/appointments")
                        .param("patientId", "1")
                        .param("doctorId", "1")
                        .param("appointmentDate", "2025-12-27T10:00:00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication required")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all appointments successfully")
    void testGetAllAppointments_Success() throws Exception {
        // Arrange
        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setStatus("COMPLETED");

        List<Appointment> appointments = Arrays.asList(testAppointment, appointment2);
        when(appointmentService.getAllAppointments()).thenReturn(appointments);

        // Act & Assert
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("BOOKED"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should get all appointments")
    void testGetAllAppointments_AsDoctor_Success() throws Exception {
        // Arrange
        when(appointmentService.getAllAppointments()).thenReturn(Arrays.asList(testAppointment));

        // Act & Assert
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should get all appointments")
    void testGetAllAppointments_AsPatient_Success() throws Exception {
        // Arrange
        when(appointmentService.getAllAppointments()).thenReturn(Arrays.asList(testAppointment));

        // Act & Assert
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN should update appointment status successfully")
    void testUpdateAppointmentStatus_AsAdmin_Success() throws Exception {
        // Arrange
        testAppointment.setStatus("COMPLETED");
        AppointmentStatusRequest request = new AppointmentStatusRequest();
        request.setStatus("COMPLETED");

        when(appointmentService.updateAppointmentStatus(1L, "COMPLETED"))
                .thenReturn(testAppointment);

        // Act & Assert
        mockMvc.perform(put("/appointments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DOCTOR should update appointment status successfully")
    void testUpdateAppointmentStatus_AsDoctor_Success() throws Exception {
        // Arrange
        testAppointment.setStatus("COMPLETED");
        AppointmentStatusRequest request = new AppointmentStatusRequest();
        request.setStatus("COMPLETED");

        when(appointmentService.updateAppointmentStatus(1L, "COMPLETED"))
                .thenReturn(testAppointment);

        // Act & Assert
        mockMvc.perform(put("/appointments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("PATIENT should not update appointment status - 403")
    void testUpdateAppointmentStatus_AsPatient_Forbidden() throws Exception {
        // Arrange
        AppointmentStatusRequest request = new AppointmentStatusRequest();
        request.setStatus("COMPLETED");

        // Act & Assert
        mockMvc.perform(put("/appointments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only ADMIN and DOCTOR can update appointment status")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update appointment status to CANCELLED")
    void testUpdateAppointmentStatus_ToCancelled() throws Exception {
        // Arrange
        testAppointment.setStatus("CANCELLED");
        AppointmentStatusRequest request = new AppointmentStatusRequest();
        request.setStatus("CANCELLED");

        when(appointmentService.updateAppointmentStatus(1L, "CANCELLED"))
                .thenReturn(testAppointment);

        // Act & Assert
        mockMvc.perform(put("/appointments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle empty appointment list")
    void testGetAllAppointments_EmptyList() throws Exception {
        // Arrange
        when(appointmentService.getAllAppointments()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}