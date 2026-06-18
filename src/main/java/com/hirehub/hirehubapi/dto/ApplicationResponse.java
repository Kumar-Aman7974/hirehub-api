package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {


    private Long id;
    private JobInfo job;
    private JobSeekerInfo jobSeeker;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeUrl;
    private String additionalNotes;
    private String rejectionReason;
    private LocalDateTime interviewDate;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    @Data
    @Builder
    public static class JobInfo {
        private Long id;
        private String title;
        private String location;
        private String companyName;
        private String companyLogo;
    }

    @Data
    @Builder
    public static class JobSeekerInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String resumeUrl;
    }
}
