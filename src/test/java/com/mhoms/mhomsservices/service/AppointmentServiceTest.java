package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.exception.ResourceNotFoundException;
import com.mhoms.mhomsservices.model.Appointment;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.repository.AppointmentRepository;
import com.mhoms.mhomsservices.repository.DoctorRepository;
import com.mhoms.mhomsservices.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentService
 * Tests business logic including duplicate prevention
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Appointment Service Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private LocalDateTime appointmentDate;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Emma Thompson");
        testPatient.setEmail("emma@example.com");

        // Setup test doctor
        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setName("Dr. Michael Chen");
        testDoctor.setSpecialization("Cardiology");

        // Setup test appointment
        appointmentDate = LocalDateTime.of(2025, 12, 27, 10, 0); // ← CORRECT!
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setAppointmentDate(appointmentDate);
        testAppointment.setStatus("BOOKED");
    }

    @Test
    @DisplayName("Should book appointment successfully")
    void testBookAppointment_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorAndAppointmentDate(testDoctor, appointmentDate))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        Appointment bookedAppointment = appointmentService.bookAppointment(1L, 1L, appointmentDate);

        // Assert
        assertThat(bookedAppointment).isNotNull();
        assertThat(bookedAppointment.getStatus()).isEqualTo("BOOKED");
        assertThat(bookedAppointment.getPatient().getName()).isEqualTo("Emma Thompson");
        assertThat(bookedAppointment.getDoctor().getName()).isEqualTo("Dr. Michael Chen");

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void testBookAppointment_PatientNotFound() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.bookAppointment(999L, 1L, appointmentDate))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when doctor not found")
    void testBookAppointment_DoctorNotFound() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.bookAppointment(1L, 999L, appointmentDate))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should prevent duplicate booking for same doctor and time")
    void testBookAppointment_DuplicatePrevention() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorAndAppointmentDate(testDoctor, appointmentDate))
                .thenReturn(true); // Appointment already exists!

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.bookAppointment(1L, 1L, appointmentDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Doctor already has an appointment at this time");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should retrieve all appointments")
    void testGetAllAppointments_Success() {
        // Arrange
        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setStatus("COMPLETED");

        List<Appointment> appointments = Arrays.asList(testAppointment, appointment2);
        when(appointmentRepository.findAll()).thenReturn(appointments);

        // Act
        List<Appointment> result = appointmentService.getAllAppointments();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo("BOOKED");
        assertThat(result.get(1).getStatus()).isEqualTo("COMPLETED");

        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update appointment status successfully")
    void testUpdateAppointmentStatus_Success() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(1L, "COMPLETED");

        // Assert
        assertThat(updatedAppointment).isNotNull();
        assertThat(updatedAppointment.getStatus()).isEqualTo("COMPLETED");

        verify(appointmentRepository, times(1)).save(testAppointment);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent appointment")
    void testUpdateAppointmentStatus_NotFound() {
        // Arrange
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(999L, "COMPLETED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should convert status to uppercase")
    void testUpdateAppointmentStatus_UppercaseConversion() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        appointmentService.updateAppointmentStatus(1L, "completed");

        // Assert
        assertThat(testAppointment.getStatus()).isEqualTo("COMPLETED");
    }
}