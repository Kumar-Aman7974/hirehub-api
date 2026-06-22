# 🏢 HireHub - Job Portal Backend System

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.0-red.svg)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-yellow.svg)](https://jwt.io/)
[![JUnit](https://img.shields.io/badge/JUnit-5.0-green.svg)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.0-blue.svg)](https://site.mockito.org/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-success.svg)](https://swagger.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **A Production-Ready REST API for Job Recruitment Platform**

---

## 📋 **Overview**

HireHub is a **complete backend system** for a job recruitment platform that connects **Job Seekers** with **Employers**. Built with **Spring Boot 3**, this monolithic application handles user authentication, job postings, applications, email notifications, and secure file uploads.

### 🎯 **Key Features**

#### 🔐 **Authentication & Authorization**
- **JWT-based Authentication** with Access & Refresh Tokens
- **Role-Based Access Control (RBAC)** - 3 User Roles:
    - `JOB_SEEKER` - Browse jobs, apply, track applications
    - `EMPLOYER` - Post jobs, review applications, manage candidates
    - `ADMIN` - Full platform access, user management
- **Stateless Session Management** for horizontal scalability
- **Password Encryption** using BCrypt

#### 💼 **Job Management**
- Complete **CRUD Operations** for Job Postings
- **Advanced Search & Filtering**:
    - Keyword search (title + description)
    - Location filter
    - Job Type filter (FULL_TIME, PART_TIME, CONTRACT, etc.)
    - Salary range filter
- **Pagination & Sorting** for efficient data retrieval
- **View Count Tracking** for job popularity analytics
- **Automatic Job Expiration** via scheduled tasks

#### 📝 **Application Management**
- **End-to-End Application Workflow**:

PENDING → REVIEWED → INTERVIEWING → HIRED/REJECTED
- **Duplicate Prevention** using Composite Unique Constraints
- **Application Tracking** for both Job Seekers and Employers
- **Status Updates** with automatic email notifications
- **Withdrawal Functionality** for pending applications
- **Application Statistics** for employers (hire rate, review rate)

#### 📧 **Email Notifications**
- **Asynchronous Email Sending** (Non-blocking, Thread Pool)
- **Professional HTML Email Templates** (Thymeleaf)
- **Automatic Retry Logic** (3 attempts with delays)
- **Email Types**:
- Welcome email (user registration)
- Application confirmation (job seeker)
- New application alert (employer)
- Application status updates (job seeker)
- Job expiry reminders (employer)

#### 📁 **File Management**
- **Secure File Upload** for:
- Resumes (Job Seekers)
- Company Logos (Employers)
- Profile Pictures (All users)
- **File Validation**:
- MIME type verification (PDF, DOCX, JPEG, PNG)
- File size limits (5MB for resumes, 2MB for images)
- Extension matching (prevent spoofing)
- **UUID-based Filename Generation** (security & collision prevention)
- **Download Endpoints** with Access Control
- **Download Count Tracking**

#### 🧪 **Testing Strategy**
- **85%+ Test Coverage** with JUnit 5 & Mockito
- **Unit Tests** for Service Layer
- **Unit Tests** for Controller Layer (MockMvc)
- **Integration Tests** for Repository Layer (@DataJpaTest)
- **End-to-End Tests** for Complete User Flows

#### 📚 **API Documentation**
- **Swagger/OpenAPI 3.0** - Interactive Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`
- **Postman Collection** for API Testing
- **Complete Request/Response Examples**

#### 🛡️ **Security Features**
- **JWT** with 15-minute Access Tokens (7-day Refresh Tokens)
- **Method-Level Security** with `@PreAuthorize`
- **Global Exception Handling** with consistent error responses
- **Input Validation** with `@Valid` and custom validators
- **CORS Configuration** for frontend integration
- **Secure Headers** and **XSS Protection**

---

## 🛠️ **Tech Stack**

| Category | Technologies |
|----------|--------------|
| **Framework** | Spring Boot 3.1.5, Spring MVC |
| **Security** | Spring Security 6, JWT (jjwt 0.11.5), BCrypt |
| **Database** | MySQL 8.0, H2 (Testing), HikariCP |
| **ORM** | Spring Data JPA, Hibernate 6 |
| **Email** | Spring Mail, Jakarta Mail |
| **Templates** | Thymeleaf (Email HTML Templates) |
| **Validation** | Jakarta Validation (Hibernate Validator) |
| **Testing** | JUnit 5, Mockito 5, Spring Boot Test, MockMvc |
| **Documentation** | SpringDoc OpenAPI 3.0 (Swagger UI) |
| **Build Tool** | Maven 3.9+ |
| **Version Control** | Git, GitHub |
| **IDE** | IntelliJ IDEA, Eclipse |

### **Key Dependencies**

```xml
<dependencies>
  <!-- Spring Boot Starters -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-mail</artifactId>
  </dependency>
  
  <!-- JWT -->
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.11.5</version>
  </dependency>
  
  <!-- Database -->
  <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
  </dependency>
  
  <!-- Template Engine -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  
  <!-- Utilities -->
  <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
  </dependency>
  
  <!-- API Documentation -->
  <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.3.0</version>
  </dependency>
  
  <!-- Testing -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
  </dependency>
  <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-test</artifactId>
      <scope>test</scope>
  </dependency>
  <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>test</scope>
  </dependency>
</dependencies>

🚀 Quick Start Guide

Prerequisites
✅ Java 17 or higher
✅ MySQL 8.0
✅ Maven 3.9+
✅ Git
✅ IntelliJ IDEA / Eclipse (Optional)

Step 1: Clone the Repository
git clone https://github.com/YKumar-Aman7974/hirehub-api.git
cd hirehub-api

Step 2: Configure Database
-- Create database
CREATE DATABASE hirehub_db;
USE hirehub_db;

-- Create user (optional)
CREATE USER 'hirehub_user'@'localhost' IDENTIFIED BY 'Hirehub@2024';
GRANT ALL PRIVILEGES ON hirehub_db.* TO 'hirehub_user'@'localhost';
FLUSH PRIVILEGES;

Step 3: Configure Application Properties
# File: src/main/resources/application.properties

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hirehub_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=hirehub_user
spring.datasource.password=Hirehub@2024
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT Configuration
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
jwt.secret=your-secret-key-here

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
email.from.address=noreply@hirehub.com
email.from.name=HireHub Team

# File Upload
file.upload-dir=./uploads
file.max-size=10485760
file.resume-max-size=5242880
file.image-max-size=2097152
file.allowed-types=PDF,DOCX,JPEG,PNG

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

Step 4: Set Environment Variables
# Create .env file (or set in IDE)
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

Step 5: Build and Run
# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run

# OR run directly
java -jar target/hirehub-api.jar

Step 6: Access the Application
# API Base URL
http://localhost:8080

# Swagger UI (Interactive Documentation)
http://localhost:8080/swagger-ui.html

# OpenAPI JSON
http://localhost:8080/api-docs

# Health Check
http://localhost:8080/api/test/health

📚 API Documentation
Authentication Endpoints
Method	Endpoint	        Description	            Access
POST	/api/auth/register	Register new user	    Public
POST	/api/auth/login	    Login & get JWT	        Public
POST	/api/auth/refresh	Refresh access token	Authenticated
POST	/api/auth/logout	Logout user	            Authenticated

Register Example:
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "JOB_SEEKER"
}

Response:
{
    "success": true,
    "message": "User registered successfully",
    "data": {
        "id": 1,
        "email": "john@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "JOB_SEEKER",
        "createdAt": "2024-01-15T10:30:00"
    },
    "timestamp": "2024-01-15T10:30:00"
}

Login Example:
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123"
}
Response:
{
    "success": true,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "user": {
            "id": 1,
            "email": "john@example.com",
            "firstName": "John",
            "lastName": "Doe",
            "role": "JOB_SEEKER"
        },
        "expiresIn": 900
    },
    "timestamp": "2024-01-15T10:30:00"
}

Job Management Endpoints

Method	    Endpoint	        Description	                    Access
POST	    /api/jobs	        Create job posting	            EMPLOYER, ADMIN
GET	        /api/jobs/{id}	    Get job by ID	                Public
GET	        /api/jobs	        Get all jobs (search/filter)	Public
GET	        /api/jobs/my-jobs	My job postings	                EMPLOYER, ADMIN
PUT	        /api/jobs/{id}	    Update job	                    EMPLOYER (owner), ADMIN
DELETE	    /api/jobs/{id}	    Delete job	                    EMPLOYER (owner), ADMIN
GET	        /api/jobs/statistics Job statistics	                EMPLOYER, ADMIN


Create Job Example:
POST http://localhost:8080/api/jobs
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
    "title": "Senior Java Developer",
    "description": "We are looking for an experienced Java developer...",
    "requirements": "5+ years of Java experience, Spring Boot expertise",
    "location": "New York, NY",
    "salaryMin": 120000,
    "salaryMax": 160000,
    "jobType": "FULL_TIME",
    "openings": 3,
    "experienceRequired": "5+ years",
    "skills": "Java, Spring Boot, MySQL, REST APIs",
    "applicationDeadline": "2024-12-31T23:59:59"
}

Search Jobs Example:
GET http://localhost:8080/api/jobs?keyword=Java&location=New%20York&jobType=FULL_TIME&page=0&size=10

Application Management Endpoints

Method	  Endpoint	                        Description	            Access
POST	  /api/applications/apply	        Apply for job	        JOB_SEEKER
GET	      /api/applications/my-applications	My applications	        JOB_SEEKER
GET	      /api/applications/job/{jobId}	    Job applications	    EMPLOYER (owner)
GET	      /api/applications/{id}	        Application details	    Owner, Employer, Admin
PUT	      /api/applications/{id}/status	    Update status	        EMPLOYER (owner), ADMIN
PUT	      /api/applications/{id}/withdraw	Withdraw application	JOB_SEEKER
GET	      /api/applications/job/{id}/statistics	Application stats	EMPLOYER (owner), ADMIN


Apply for Job Example:

POST http://localhost:8080/api/applications/apply
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
    "jobId": 1,
    "coverLetter": "I am very interested in this position...",
    "resumeUrl": "/api/files/download/abc-123-uuid"
}

Update Application Status Example:
PUT http://localhost:8080/api/applications/1/status
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
    "status": "INTERVIEWING",
    "additionalNotes": "Great candidate! Moving to interview round.",
    "interviewDate": "2024-02-15T14:00:00"
}

File Management Endpoints
Method	    Endpoint	                        Description	            Access
POST	    /api/files/upload/resume	        Upload resume	        JOB_SEEKER
POST	    /api/files/upload/profile-picture	Upload profile picture	Authenticated
POST	    /api/files/upload/company-logo	    Upload company logo	    EMPLOYER, ADMIN
GET	        /api/files/download/{fileId}	    Download file	        Authenticated (owner)
DELETE	    /api/files/{fileId}	                Delete file	            Authenticated (owner)
GET	        /api/files/category/{category}	    Get files by category	Authenticated


User Management Endpoints
Method	    Endpoint	        Description	        Access
GET	        /api/users/profile	Get current user	Authenticated
GET	        /api/users/{id}	    Get user by ID	    Authenticated
GET	        /api/users/all	    Get all users	    ADMIN
DELETE	    /api/users/{id}	    Delete user	        ADMIN

🧪 Testing

Run All Tests
mvn test

Generate Test Coverage Report
mvn clean test
mvn jacoco:report

# Open report:
# target/site/jacoco/index.html


Test Coverage: 85%+
Module	            Coverage	Status
Service Layer	    85%+	✅
Controller Layer	80%+	✅
Repository Layer	75%+	✅
Overall	            82%+	✅


Testing Tools Used:

JUnit 5 - Test framework
Mockito 5 - Mocking framework
MockMvc - Controller testing
@DataJpaTest - Repository testing
@WebMvcTest - Controller slice testing
@SpringBootTest - Integration testing


📁 Project Structure
hirehub-api/
├── src/
│   ├── main/
│   │   ├── java/com/hirehub/hirehubapi/
│   │   │   ├── HirehubApiApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── AsyncConfig.java
│   │   │   │   └── FileStorageConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── JobController.java
│   │   │   │   ├── ApplicationController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── FileController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── JobService.java
│   │   │   │   ├── ApplicationService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── FileService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── JobRepository.java
│   │   │   │   ├── ApplicationRepository.java
│   │   │   │   └── FileMetadataRepository.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Job.java
│   │   │   │   ├── Application.java
│   │   │   │   ├── FileMetadata.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── JobType.java
│   │   │   │   ├── JobStatus.java
│   │   │   │   ├── ApplicationStatus.java
│   │   │   │   ├── FileCategory.java
│   │   │   │   └── FileType.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── UserResponse.java
│   │   │   │   ├── JobRequest.java
│   │   │   │   ├── JobResponse.java
│   │   │   │   ├── JobSearchRequest.java
│   │   │   │   ├── ApplicationRequest.java
│   │   │   │   ├── ApplicationResponse.java
│   │   │   │   ├── ApplicationStatusUpdate.java
│   │   │   │   ├── ApplicationStatistics.java
│   │   │   │   ├── FileUploadRequest.java
│   │   │   │   ├── FileUploadResponse.java
│   │   │   │   ├── FileMetadataResponse.java
│   │   │   │   └── ApiResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   ├── FileStorageException.java
│   │   │   │   ├── FileSizeExceededException.java
│   │   │   │   └── UnsupportedFileTypeException.java
│   │   │   └── utils/
│   │   │       └── JwtUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/email/
│   │           ├── base-email.html
│   │           ├── welcome-email.html
│   │           ├── application-confirmation.html
│   │           ├── new-application-notification.html
│   │           ├── application-status-update.html
│   │           └── job-expiry-reminder.html
│   └── test/
│       └── java/com/hirehub/hirehubapi/
│           ├── service/
│           │   ├── UserServiceTest.java
│           │   ├── JobServiceTest.java
│           │   └── ApplicationServiceTest.java
│           ├── controller/
│           │   ├── AuthControllerTest.java
│           │   ├── JobControllerTest.java
│           │   └── ApplicationControllerTest.java
│           └── repository/
│               ├── UserRepositoryTest.java
│               ├── JobRepositoryTest.java
│               └── ApplicationRepositoryTest.java
├── uploads/ (auto-generated)
├── .gitignore
├── pom.xml
└── README.md


📊 Database Schema
-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role ENUM('JOB_SEEKER', 'EMPLOYER', 'ADMIN') NOT NULL,
    company_name VARCHAR(255),
    resume_url VARCHAR(500),
    profile_picture_url VARCHAR(500),
    company_logo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Jobs Table
CREATE TABLE jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    location VARCHAR(255) NOT NULL,
    salary_min DECIMAL(15,2) NOT NULL,
    salary_max DECIMAL(15,2) NOT NULL,
    job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'FREELANCE', 'INTERNSHIP', 'REMOTE', 'HYBRID') NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'FILLED', 'DRAFT') DEFAULT 'ACTIVE',
    openings INT DEFAULT 1,
    experience_required VARCHAR(100),
    skills TEXT,
    application_deadline TIMESTAMP,
    employer_id BIGINT NOT NULL,
    view_count INT DEFAULT 0,
    application_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES users(id),
    INDEX idx_title (title),
    INDEX idx_location (location),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Applications Table
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    job_seeker_id BIGINT NOT NULL,
    status ENUM('PENDING', 'REVIEWED', 'INTERVIEWING', 'HIRED', 'REJECTED', 'WITHDRAWN', 'EXPIRED') DEFAULT 'PENDING',
    cover_letter TEXT NOT NULL,
    resume_url VARCHAR(500),
    additional_notes TEXT,
    rejection_reason TEXT,
    interview_date TIMESTAMP,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (job_seeker_id) REFERENCES users(id),
    UNIQUE KEY unique_job_seeker_application (job_id, job_seeker_id),
    INDEX idx_application_status (status),
    INDEX idx_application_applied_at (applied_at),
    INDEX idx_application_job_seeker (job_seeker_id)
);

-- File Metadata Table
CREATE TABLE file_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(255) UNIQUE NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_extension VARCHAR(20),
    category ENUM('RESUME', 'COMPANY_LOGO', 'PROFILE_PICTURE', 'COVER_LETTER', 'ATTACHMENT') NOT NULL,
    file_type ENUM('JPEG', 'PNG', 'GIF', 'WEBP', 'PDF', 'DOCX', 'DOC', 'TXT', 'RTF', 'ZIP', 'RAR'),
    owner_id BIGINT NOT NULL,
    related_entity_id BIGINT,
    is_public BOOLEAN DEFAULT FALSE,
    download_count INT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_owner (owner_id),
    INDEX idx_file_category (category),
    INDEX idx_file_created_at (created_at)
);

🎯 API Response Format

Success Response:
{
    "success": true,
    "message": "Operation successful",
    "data": { ... },
    "timestamp": "2024-01-15T10:30:00"
}
Error Response:
{
    "success": false,
    "message": "Error description",
    "timestamp": "2024-01-15T10:30:00"
}

Validation Error Response:
{
    "success": false,
    "message": "Validation failed",
    "data": {
        "email": "Email is required",
        "password": "Password must be at least 6 characters"
    },
    "timestamp": "2024-01-15T10:30:00"
}


HTTP Status Codes Used:
200 OK - Successful GET/PUT/POST requests
201 CREATED - Resource created successfully
400 BAD REQUEST - Validation failed
401 UNAUTHORIZED - Invalid/Expired token
403 FORBIDDEN - Insufficient permissions
404 NOT FOUND - Resource not found
500 INTERNAL SERVER ERROR - Server error


🔧 Configuration Reference

Application Properties
server.port=8080
server.error.include-stacktrace=never

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hirehub_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# File Upload
file.upload-dir=./uploads
file.max-size=10485760
file.resume-max-size=5242880
file.image-max-size=2097152
file.allowed-types=PDF,DOCX,JPEG,PNG

# Logging
logging.level.com.hirehub=DEBUG
logging.level.org.springframework.security=DEBUG

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true


📈 Screenshots
Swagger UI Documentation
https://via.placeholder.com/800x400?text=Swagger+UI+Screenshot

API Response Example
https://via.placeholder.com/800x400?text=API+Response+Screenshot

Postman Collection
https://via.placeholder.com/800x400?text=Postman+Collection+Screenshot

Email Template
https://via.placeholder.com/800x400?text=Email+Template+Screenshot

🧪 Test Results
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.hirehub.hirehubapi.service.UserServiceTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0

Running com.hirehub.hirehubapi.service.JobServiceTest
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0

Running com.hirehub.hirehubapi.service.ApplicationServiceTest
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0

Running com.hirehub.hirehubapi.controller.AuthControllerTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

Running com.hirehub.hirehubapi.controller.JobControllerTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0

Running com.hirehub.hirehubapi.controller.ApplicationControllerTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

Running com.hirehub.hirehubapi.repository.UserRepositoryTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 57, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS

Coverage Report
Package	            Class %	Method %	Line %
Service Layer	    100%	95%	        88%
Controller Layer	100%	92%	        82%
Repository Layer	100%	90%	        78%
Overall	            100%	92%	        85%

🤝 Contributing
This is a learning project, but suggestions are welcome!

Fork the repository
Create your feature branch (git checkout -b feature/AmazingFeature)
Commit your changes (git commit -m 'Add some AmazingFeature')
Push to the branch (git push origin feature/AmazingFeature)
Open a Pull Request

📝 License
This project is licensed under the MIT License - see the LICENSE file for details.

👨‍💻 Author
Aman Kumar

Email: amanbth7974@gmail.com
GitHub: https://github.com/Kumar-Aman7974
LinkedIn: https://www.linkedin.com/in/aman-kumar-8b8416360/

🙏 Acknowledgments
Spring Boot Documentation
JWT.io
Baeldung for excellent Spring tutorials
SpringDoc OpenAPI

⭐ Show Your Support
If this project helped you, please give it a ⭐ on GitHub

Built with ❤️ using Spring Boot
