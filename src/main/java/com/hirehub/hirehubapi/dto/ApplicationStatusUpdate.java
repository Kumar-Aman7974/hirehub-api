package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationStatusUpdate {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private String additionalNotes;

    private String rejectionReason;

    private LocalDateTime interviewDate;

    // Validate that if status is REJECTED, rejection reason is provided
    public boolean isValidStatusTransition() {
        if (status == ApplicationStatus.REJECTED) {
            return rejectionReason != null && !rejectionReason.trim().isEmpty();
        }
        if (status == ApplicationStatus.INTERVIEWING) {
            return interviewDate != null;
        }
        return true;
    }
}
