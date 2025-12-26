# 🏥 MHOMS - Medical Hospital Office Management System

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-green?style=for-the-badge&logo=render)](https://mhoms-api.onrender.com/swagger-ui.html)
[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

A production-ready **REST API** for managing hospital operations including patient records, doctor profiles, and appointment scheduling with **JWT authentication** and **role-based access control**.

## 🌐 Live Demo

> **🔗 API URL:** [https://mhoms-api.onrender.com](https://mhoms-api.onrender.com)
>
> **📖 Swagger UI:** [https://mhoms-api.onrender.com/swagger-ui.html](https://mhoms-api.onrender.com/swagger-ui.html)

---

## ✨ Features

### 🔐 Authentication & Security
- **JWT Authentication** with Access & Refresh Tokens
- **Role-Based Access Control** (ADMIN, DOCTOR, PATIENT)
- **BCrypt Password Encryption**
- **Secure Endpoint Protection**

### 👥 User Management
- User Registration with Role Assignment
- Secure Login with Token Generation
- Token Refresh Mechanism

### 🏥 Patient Management
- Complete CRUD Operations
- Search by Name, Gender, Age Range
- Pagination & Sorting Support
- Duplicate Email/Phone Prevention

### 👨‍⚕️ Doctor Management
- Doctor Profile Management
- Specialization-based Search
- Active/Inactive Status Toggle
- List All Specializations

### 📅 Appointment Management
- Book, Reschedule, Cancel Appointments
- Prevent Double Booking
- Today's & Upcoming Appointments
- Status Tracking (BOOKED, COMPLETED, CANCELLED)

### 📊 Dashboard & Statistics
- Real-time System Statistics
- Patient, Doctor, Appointment Counts
- Admin Dashboard API

### 🛠️ Advanced Features
- **Pagination** - Handle large datasets efficiently
- **Sorting** - Sort by any field (asc/desc)
- **Search & Filter** - Multi-criteria search
- **Soft Delete** - Cancel appointments without data loss
- **Validation** - Comprehensive input validation
- **Error Handling** - User-friendly error messages

---

## 🏗️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 17** | Programming Language |
| **Spring Boot 3.2** | Backend Framework |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Database ORM |
| **PostgreSQL** | Relational Database |
| **JWT (jjwt 0.12.3)** | Token-based Authentication |
| **Swagger/OpenAPI 3** | API Documentation |
| **JUnit 5 & Mockito** | Testing Framework |
| **Gradle** | Build Tool |
| **Docker** | Containerization |
| **Render** | Cloud Deployment |

---

## 📁 Project Structure

```
mhoms-api/
├── src/main/java/com/mhoms/mhomsservices/
│   ├── config/                 # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── CorsConfig.java
│   ├── controller/             # REST Controllers
│   │   ├── AuthController.java
│   │   ├── PatientController.java
│   │   ├── DoctorController.java
│   │   ├── AppointmentController.java
│   │   └── DashboardController.java
│   ├── dto/                    # Data Transfer Objects
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── AuthResponse.java
│   │   └── PageResponse.java
│   ├── exception/              # Exception Handling
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── model/                  # Entity Classes
│   │   ├── User.java
│   │   ├── Patient.java
│   │   ├── Doctor.java
│   │   ├── Appointment.java
│   │   └── Role.java
│   ├── repository/             # Data Repositories
│   │   ├── UserRepository.java
│   │   ├── PatientRepository.java
│   │   ├── DoctorRepository.java
│   │   └── AppointmentRepository.java
│   ├── security/               # Security Components
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── CustomAccessDeniedHandler.java
│   │   └── CustomAuthenticationEntryPoint.java
│   ├── service/                # Business Logic
│   │   ├── AuthService.java
│   │   ├── PatientService.java
│   │   ├── DoctorService.java
│   │   └── AppointmentService.java
│   └── MhoMsApplication.java   # Main Application
├── src/test/                   # Test Classes (72 Tests)
├── Dockerfile                  # Docker Configuration
├── build.gradle                # Gradle Build File
└── README.md                   # This File
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **PostgreSQL 14+**
- **Gradle 8.5+** (or use wrapper)
- **Git**

### Local Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Jashwith/mhoms-api.git
   cd mhoms-api
   ```

2. **Create PostgreSQL Database**
   ```sql
   CREATE DATABASE mhoms_db;
   ```

3. **Configure Environment**

   Update `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/mhoms_db
       username: your_username
       password: your_password
   ```

4. **Build & Run**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

5. **Access the API**
    - API: http://localhost:8080
    - Swagger UI: http://localhost:8080/swagger-ui.html

---

## 🔑 API Authentication Flow

### 1. Register a User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@hospital.com",
  "password": "admin123",
  "fullName": "Admin User",
  "role": "ADMIN"
}
```

### 2. Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

### 3. Use Token in Requests
```http
GET /patients
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 📚 API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/auth/register` | Register new user | Public |
| POST | `/auth/login` | Login & get tokens | Public |
| POST | `/auth/refresh` | Refresh access token | Public |

### Patients
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/patients` | Get all patients | ADMIN, DOCTOR |
| GET | `/patients/{id}` | Get patient by ID | ADMIN, DOCTOR |
| GET | `/patients/page` | Get paginated patients | ADMIN, DOCTOR |
| GET | `/patients/search` | Search patients | ADMIN, DOCTOR |
| GET | `/patients/stats` | Get patient statistics | ADMIN, DOCTOR |
| POST | `/patients` | Create patient | ADMIN |
| PUT | `/patients/{id}` | Update patient | ADMIN |
| DELETE | `/patients/{id}` | Delete patient | ADMIN |

### Doctors
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/doctors` | Get all doctors | ALL |
| GET | `/doctors/{id}` | Get doctor by ID | ALL |
| GET | `/doctors/page` | Get paginated doctors | ALL |
| GET | `/doctors/search` | Search doctors | ALL |
| GET | `/doctors/specializations` | List specializations | ALL |
| GET | `/doctors/active` | Get active doctors | ALL |
| GET | `/doctors/stats` | Get doctor statistics | ALL |
| POST | `/doctors` | Create doctor | ADMIN |
| PUT | `/doctors/{id}` | Update doctor | ADMIN |
| PATCH | `/doctors/{id}/toggle-status` | Toggle active status | ADMIN |
| DELETE | `/doctors/{id}` | Delete doctor | ADMIN |

### Appointments
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/appointments` | Get all appointments | ALL |
| GET | `/appointments/{id}` | Get appointment by ID | ALL |
| GET | `/appointments/page` | Get paginated appointments | ALL |
| GET | `/appointments/search` | Search appointments | ALL |
| GET | `/appointments/today` | Get today's appointments | ALL |
| GET | `/appointments/upcoming` | Get upcoming appointments | ALL |
| GET | `/appointments/stats` | Get appointment statistics | ALL |
| POST | `/appointments` | Book appointment | ADMIN, PATIENT |
| PUT | `/appointments/{id}/status` | Update status | ADMIN, DOCTOR |
| PUT | `/appointments/{id}/reschedule` | Reschedule | ADMIN, DOCTOR |
| PUT | `/appointments/{id}/cancel` | Cancel appointment | ALL |
| DELETE | `/appointments/{id}` | Delete appointment | ADMIN |

### Dashboard
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/dashboard/stats` | Complete statistics | ADMIN |
| GET | `/dashboard/summary` | Quick summary | ADMIN |

---

## 🔒 Role-Based Access Control

| Resource | ADMIN | DOCTOR | PATIENT |
|----------|-------|--------|---------|
| Create Patient | ✅ | ❌ | ❌ |
| View Patients | ✅ | ✅ | ❌ |
| Create Doctor | ✅ | ❌ | ❌ |
| View Doctors | ✅ | ✅ | ✅ |
| Book Appointment | ✅ | ❌ | ✅ |
| View Appointments | ✅ | ✅ | ✅ |
| Update Appointment Status | ✅ | ✅ | ❌ |
| Access Dashboard | ✅ | ❌ | ❌ |

---

## 🧪 Testing

The project includes **72 comprehensive tests**:

```bash
# Run all tests
./gradlew test

# View test report
open build/reports/tests/test/index.html
```

### Test Coverage
- ✅ Unit Tests (Service Layer)
- ✅ Integration Tests (Controller Layer)
- ✅ Security Tests (JWT Authentication)
- ✅ Repository Tests

---

## 🐳 Docker Deployment

```bash
# Build Docker image
docker build -t mhoms-api .

# Run container
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/mhoms \
  -e DATABASE_USERNAME=user \
  -e DATABASE_PASSWORD=pass \
  -e JWT_SECRET=your-secret-key \
  mhoms-api
```

---

## 📊 Sample API Responses

### Paginated Response
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

### Dashboard Statistics
```json
{
  "totalPatients": 150,
  "malePatients": 80,
  "femalePatients": 70,
  "totalDoctors": 25,
  "activeDoctors": 22,
  "totalAppointments": 500,
  "bookedAppointments": 45,
  "completedAppointments": 440,
  "cancelledAppointments": 15,
  "todaysAppointments": 12,
  "generatedAt": "2025-12-26T10:30:00"
}
```

### Error Response
```json
{
  "timestamp": "2025-12-26T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with id: 999",
  "path": "/patients/999"
}
```

---

## 🛣️ Roadmap

- [x] Core CRUD Operations
- [x] JWT Authentication
- [x] Role-Based Access Control
- [x] Pagination & Sorting
- [x] Search & Filter
- [x] Swagger Documentation
- [x] Unit & Integration Tests
- [x] Cloud Deployment
- [ ] Email Notifications
- [ ] Appointment Reminders
- [ ] Reports & Analytics
- [ ] React Frontend

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Jashwith**

- GitHub: [@Jashwith](https://github.com/INFO333/)
- Email: infotube45@gmail.com

---

## 🙏 Acknowledgments

- Spring Boot Documentation
- JWT.io for JWT resources
- Render for free hosting
- Swagger for API documentation

---

<p align="center">
  <b>⭐ If you found this project helpful, please give it a star! ⭐</b>
</p>

<p align="center">
  Made with ❤️ using Spring Boot
</p>