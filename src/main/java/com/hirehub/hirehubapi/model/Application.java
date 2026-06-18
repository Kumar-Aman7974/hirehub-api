package com.hirehub.hirehubapi.model;

import com.hirehub.hirehubapi.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_job_seeker_application",
                        columnNames = {"job_id", "job_seeker_id"}
                )
        },
        indexes = {
                @Index(name = "idx_application_status", columnList = "status"),
                @Index(name = "idx_application_applied_at", columnList = "applied_at"),
                @Index(name = "idx_application_job_seeker", columnList = "job_seeker_id")
        })
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private User jobSeeker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;


    @NotBlank(message = "Cover letter is required")
    @Size(min = 50, max = 2000, message = "Cover letter must be between 50 and 2000 characters")
    @Column(length = 2000)
    private String coverLetter;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "additional_notes")
    private String additionalNotes; // Employer notes

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    // Helper method to update status
    public void updateStatus(ApplicationStatus newStatus) {
        if (this.status.isTerminal()) {
            throw new IllegalStateException("Cannot change status of a terminal application");
        }
        this.status = newStatus;
        if (newStatus == ApplicationStatus.REVIEWED) {
            this.reviewedAt = LocalDateTime.now();
        }
    }

    // Helper method to check if job seeker can withdraw
    public boolean canWithdraw() {
        return status == ApplicationStatus.PENDING ||
                status == ApplicationStatus.REVIEWED;
    }
}
