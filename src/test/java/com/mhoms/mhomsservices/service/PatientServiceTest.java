package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.exception.ResourceNotFoundException;
import com.mhoms.mhomsservices.model.Patient;
import com.mhoms.mhomsservices.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PatientService
 * Uses Mockito to mock repository dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Patient Service Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        // Setup test data before each test
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Emma Thompson");
        testPatient.setAge(32);
        testPatient.setGender("Female");
        testPatient.setPhone("9876543210");
        testPatient.setEmail("emma@example.com");
    }

    @Test
    @DisplayName("Should create patient successfully")
    void testCreatePatient_Success() {
        // Arrange (Given)
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act (When)
        Patient createdPatient = patientService.createPatient(testPatient);

        // Assert (Then)
        assertThat(createdPatient).isNotNull();
        assertThat(createdPatient.getId()).isEqualTo(1L);
        assertThat(createdPatient.getName()).isEqualTo("Emma Thompson");
        assertThat(createdPatient.getEmail()).isEqualTo("emma@example.com");

        verify(patientRepository, times(1)).save(testPatient);
    }

    @Test
    @DisplayName("Should retrieve all patients")
    void testGetAllPatients_Success() {
        // Arrange
        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("David Martinez");
        patient2.setAge(45);
        patient2.setGender("Male");
        patient2.setPhone("9123456789");
        patient2.setEmail("david@example.com");

        List<Patient> patientList = Arrays.asList(testPatient, patient2);
        when(patientRepository.findAll()).thenReturn(patientList);

        // Act
        List<Patient> result = patientService.getAllPatients();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Emma Thompson");
        assertThat(result.get(1).getName()).isEqualTo("David Martinez");

        verify(patientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve patient by ID")
    void testGetPatientById_Found() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        Patient foundPatient = patientService.getPatientById(1L);

        // Assert
        assertThat(foundPatient).isNotNull();
        assertThat(foundPatient.getId()).isEqualTo(1L);
        assertThat(foundPatient.getName()).isEqualTo("Emma Thompson");

        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void testGetPatientById_NotFound() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> patientService.getPatientById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found");

        verify(patientRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should handle empty patient list")
    void testGetAllPatients_EmptyList() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Patient> result = patientService.getAllPatients();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(patientRepository, times(1)).findAll();
    }
}