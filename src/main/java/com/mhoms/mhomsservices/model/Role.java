package com.mhoms.mhomsservices.model;

/**
 * User roles for role-based access control
 * - ADMIN: Full system access
 * - DOCTOR: Can manage appointments and view patients
 * - PATIENT: Can view own appointments
 */
public enum Role {
    ADMIN,
    DOCTOR,
    PATIENT
}