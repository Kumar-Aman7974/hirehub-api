package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.JobStatus;
import com.hirehub.hirehubapi.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private JobType jobType;
    private JobStatus status;
    private Integer openings;
    private String experienceRequired;
    private String skills;
    private LocalDateTime applicationDeadline;
    private EmployerInfo employer;
    private Integer viewCount;
    private Integer applicationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    @Data
    @Builder
    public static class EmployerInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String companyName;
    }
}
