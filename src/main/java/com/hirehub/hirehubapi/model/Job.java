package com.hirehub.hirehubapi.model;

import com.hirehub.hirehubapi.enums.JobStatus;
import com.hirehub.hirehubapi.enums.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_title", columnList = "title"),
        @Index(name = "idx_location", columnList = "location"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})

public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 500 characters")
    @Column(length = 5000, nullable = false)
    private String description;

    @NotBlank(message = "Requirements are required")
    @Size(min = 20, max = 2000, message = "Requirements must be between 20 and 2000 characters")
    @Column(length = 2000)
    private String requirements;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Salary range is required")
    private BigDecimal salaryMin;

    @NotNull(message = "Salary range is required")
    private BigDecimal salaryMax;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.ACTIVE;

    @Min(value = 1, message = "Must have at least 1 opening")
    private Integer openings = 1;

    @Column(name = "experience_required")
    private String experienceRequired;

    private String skills; // Comma-separated skills

    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;


    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "application_count")
    private Integer applicationCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    // relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Application> applications = new HashSet<>();

    public  void incrementViewCount() {

        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void incrementApplicationCount() {
        this.applicationCount = (this.applicationCount == null ? 0 : this.applicationCount) + 1;
    }


    // Check if job is expired
    @PrePersist
    @PreUpdate
    public void checkExpiration() {
        if (applicationDeadline != null && applicationDeadline.isBefore(LocalDateTime.now())) {
            this.status = JobStatus.EXPIRED;
        }
    }

}
