package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.exception.ResourceNotFoundException;
import com.mhoms.mhomsservices.model.Doctor;
import com.mhoms.mhomsservices.repository.DoctorRepository;
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
 * Unit tests for DoctorService
 * Tests doctor creation, retrieval, and management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Doctor Service Tests")
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
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
    @DisplayName("Should create doctor successfully")
    void testCreateDoctor_Success() {
        // Arrange
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        Doctor createdDoctor = doctorService.createDoctor(testDoctor);

        // Assert
        assertThat(createdDoctor).isNotNull();
        assertThat(createdDoctor.getId()).isEqualTo(1L);
        assertThat(createdDoctor.getName()).isEqualTo("Dr. Michael Chen");
        assertThat(createdDoctor.getSpecialization()).isEqualTo("Cardiology");
        assertThat(createdDoctor.getEmail()).isEqualTo("dr.chen@hospital.com");
        assertThat(createdDoctor.getActive()).isTrue();

        verify(doctorRepository, times(1)).save(testDoctor);
    }

    @Test
    @DisplayName("Should retrieve all doctors")
    void testGetAllDoctors_Success() {
        // Arrange
        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Lisa Anderson");
        doctor2.setSpecialization("Pediatrics");
        doctor2.setPhone("9123123123");
        doctor2.setEmail("dr.anderson@hospital.com");
        doctor2.setActive(true);

        List<Doctor> doctorList = Arrays.asList(testDoctor, doctor2);
        when(doctorRepository.findAll()).thenReturn(doctorList);

        // Act
        List<Doctor> result = doctorService.getAllDoctors();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Dr. Michael Chen");
        assertThat(result.get(0).getSpecialization()).isEqualTo("Cardiology");
        assertThat(result.get(1).getName()).isEqualTo("Dr. Lisa Anderson");
        assertThat(result.get(1).getSpecialization()).isEqualTo("Pediatrics");

        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve doctor by ID")
    void testGetDoctorById_Found() {
        // Arrange
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // Act
        Doctor foundDoctor = doctorService.getDoctorById(1L);

        // Assert
        assertThat(foundDoctor).isNotNull();
        assertThat(foundDoctor.getId()).isEqualTo(1L);
        assertThat(foundDoctor.getName()).isEqualTo("Dr. Michael Chen");
        assertThat(foundDoctor.getSpecialization()).isEqualTo("Cardiology");

        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when doctor not found")
    void testGetDoctorById_NotFound() {
        // Arrange
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> doctorService.getDoctorById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");

        verify(doctorRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should handle empty doctor list")
    void testGetAllDoctors_EmptyList() {
        // Arrange
        when(doctorRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Doctor> result = doctorService.getAllDoctors();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create inactive doctor")
    void testCreateDoctor_InactiveDoctor() {
        // Arrange
        testDoctor.setActive(false);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        Doctor createdDoctor = doctorService.createDoctor(testDoctor);

        // Assert
        assertThat(createdDoctor).isNotNull();
        assertThat(createdDoctor.getActive()).isFalse();

        verify(doctorRepository, times(1)).save(testDoctor);
    }

    @Test
    @DisplayName("Should create multiple doctors with different specializations")
    void testCreateDoctors_MultipleSpecializations() {
        // Arrange
        Doctor cardiologist = new Doctor();
        cardiologist.setSpecialization("Cardiology");

        Doctor pediatrician = new Doctor();
        pediatrician.setSpecialization("Pediatrics");

        Doctor neurologist = new Doctor();
        neurologist.setSpecialization("Neurology");

        when(doctorRepository.save(cardiologist)).thenReturn(cardiologist);
        when(doctorRepository.save(pediatrician)).thenReturn(pediatrician);
        when(doctorRepository.save(neurologist)).thenReturn(neurologist);

        // Act
        Doctor result1 = doctorService.createDoctor(cardiologist);
        Doctor result2 = doctorService.createDoctor(pediatrician);
        Doctor result3 = doctorService.createDoctor(neurologist);

        // Assert
        assertThat(result1.getSpecialization()).isEqualTo("Cardiology");
        assertThat(result2.getSpecialization()).isEqualTo("Pediatrics");
        assertThat(result3.getSpecialization()).isEqualTo("Neurology");

        verify(doctorRepository, times(3)).save(any(Doctor.class));
    }
}