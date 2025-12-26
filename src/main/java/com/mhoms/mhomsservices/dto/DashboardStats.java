package com.mhoms.mhomsservices.dto;

import java.time.LocalDateTime;

/**
 * DTO for dashboard statistics
 * Provides overview of system data for admin dashboard
 */
public class DashboardStats {

    // Patient Statistics
    private long totalPatients;
    private long malePatients;
    private long femalePatients;
    private long patientsCreatedToday;

    // Doctor Statistics
    private long totalDoctors;
    private long activeDoctors;
    private long inactiveDoctors;

    // Appointment Statistics
    private long totalAppointments;
    private long bookedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long todaysAppointments;
    private long upcomingAppointments;

    // User Statistics
    private long totalUsers;
    private long adminUsers;
    private long doctorUsers;
    private long patientUsers;

    // Metadata
    private LocalDateTime generatedAt;

    // Default constructor
    public DashboardStats() {
        this.generatedAt = LocalDateTime.now();
    }

    // Builder pattern for easy construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DashboardStats stats = new DashboardStats();

        public Builder totalPatients(long count) {
            stats.totalPatients = count;
            return this;
        }

        public Builder malePatients(long count) {
            stats.malePatients = count;
            return this;
        }

        public Builder femalePatients(long count) {
            stats.femalePatients = count;
            return this;
        }

        public Builder patientsCreatedToday(long count) {
            stats.patientsCreatedToday = count;
            return this;
        }

        public Builder totalDoctors(long count) {
            stats.totalDoctors = count;
            return this;
        }

        public Builder activeDoctors(long count) {
            stats.activeDoctors = count;
            return this;
        }

        public Builder inactiveDoctors(long count) {
            stats.inactiveDoctors = count;
            return this;
        }

        public Builder totalAppointments(long count) {
            stats.totalAppointments = count;
            return this;
        }

        public Builder bookedAppointments(long count) {
            stats.bookedAppointments = count;
            return this;
        }

        public Builder completedAppointments(long count) {
            stats.completedAppointments = count;
            return this;
        }

        public Builder cancelledAppointments(long count) {
            stats.cancelledAppointments = count;
            return this;
        }

        public Builder todaysAppointments(long count) {
            stats.todaysAppointments = count;
            return this;
        }

        public Builder upcomingAppointments(long count) {
            stats.upcomingAppointments = count;
            return this;
        }

        public Builder totalUsers(long count) {
            stats.totalUsers = count;
            return this;
        }

        public Builder adminUsers(long count) {
            stats.adminUsers = count;
            return this;
        }

        public Builder doctorUsers(long count) {
            stats.doctorUsers = count;
            return this;
        }

        public Builder patientUsers(long count) {
            stats.patientUsers = count;
            return this;
        }

        public DashboardStats build() {
            return stats;
        }
    }

    // Getters and Setters
    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getMalePatients() {
        return malePatients;
    }

    public void setMalePatients(long malePatients) {
        this.malePatients = malePatients;
    }

    public long getFemalePatients() {
        return femalePatients;
    }

    public void setFemalePatients(long femalePatients) {
        this.femalePatients = femalePatients;
    }

    public long getPatientsCreatedToday() {
        return patientsCreatedToday;
    }

    public void setPatientsCreatedToday(long patientsCreatedToday) {
        this.patientsCreatedToday = patientsCreatedToday;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getActiveDoctors() {
        return activeDoctors;
    }

    public void setActiveDoctors(long activeDoctors) {
        this.activeDoctors = activeDoctors;
    }

    public long getInactiveDoctors() {
        return inactiveDoctors;
    }

    public void setInactiveDoctors(long inactiveDoctors) {
        this.inactiveDoctors = inactiveDoctors;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getBookedAppointments() {
        return bookedAppointments;
    }

    public void setBookedAppointments(long bookedAppointments) {
        this.bookedAppointments = bookedAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public long getTodaysAppointments() {
        return todaysAppointments;
    }

    public void setTodaysAppointments(long todaysAppointments) {
        this.todaysAppointments = todaysAppointments;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(long adminUsers) {
        this.adminUsers = adminUsers;
    }

    public long getDoctorUsers() {
        return doctorUsers;
    }

    public void setDoctorUsers(long doctorUsers) {
        this.doctorUsers = doctorUsers;
    }

    public long getPatientUsers() {
        return patientUsers;
    }

    public void setPatientUsers(long patientUsers) {
        this.patientUsers = patientUsers;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}