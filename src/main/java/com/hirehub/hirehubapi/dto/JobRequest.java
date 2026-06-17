package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.JobType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;

    @NotBlank(message = "Requirements are required")
    @Size(min = 20, max = 2000, message = "Requirements must be between 20 and 2000 characters")
    private String requirements;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Minimum salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salaryMin;

    @NotNull(message = "Maximum salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salaryMax;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @Min(value = 1, message = "Must have at least 1 opening")
    private Integer openings = 1;

    private String experienceRequired;

    private String skills;

    private LocalDateTime applicationDeadline;

    // Custom validation to ensure salaryMin <= salaryMax
    @AssertTrue(message = "Minimum salary must be less than or equal to maximum salary")
    public boolean isSalaryValid() {
        if (salaryMin == null || salaryMax == null) return true;
        return salaryMin.compareTo(salaryMax) <= 0;
    }


}
