package com.hirehub.hirehubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotBlank(message = "Cover letter is required")
    @Size(min = 50, max = 2000, message = "Cover letter must be between 50 and 2000 characters")
    private String coverLetter;

    private String resumeUrl; // Could be uploaded file URL
}
