package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Job posting request")
public class JobRequest {

    @Schema(description = "Job title", example = "Java backend Developer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @Schema(description = "job description", example = "We are looking for an experience java Developer", requiredMode =
    Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;

    @Schema(description = "Job requirements", example = "5+ years of Java experience, Spring Boot expertise",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Requirements are required")
    @Size(min = 20, max = 2000, message = "Requirements must be between 20 and 2000 characters")
    private String requirements;

    @Schema(description = "Job location", example = "New York, NY (Remote possible)",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Minimum salary", example = "120000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Minimum salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum salary", example = "160000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Maximum salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salaryMax;

    @Schema(description = "Job type", example = "FULL_TIME",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Job type is required")
    private JobType jobType;

    @Schema(description = "Number of openings", example = "3")
    @Min(value = 1, message = "Must have at least 1 opening")
    private Integer openings = 1;

    @Schema(description = "Experience required", example = "5+ years")
    private String experienceRequired;

    @Schema(description = "Required skills (comma-separated)", example = "Java, Spring Boot, MySQL")
    private String skills;

    @Schema(description = "Application deadline", example = "2026-06-23T23:59:59")
    private LocalDateTime applicationDeadline;

    // Custom validation to ensure salaryMin <= salaryMax
    @AssertTrue(message = "Minimum salary must be less than or equal to maximum salary")
    public boolean isSalaryValid() {
        if (salaryMin == null || salaryMax == null) return true;
        return salaryMin.compareTo(salaryMax) <= 0;
    }


}
