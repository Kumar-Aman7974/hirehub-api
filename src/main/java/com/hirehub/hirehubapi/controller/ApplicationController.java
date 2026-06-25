package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.*;
import com.hirehub.hirehubapi.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/application")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "APIs for managing job applications")
public class ApplicationController {

    private final ApplicationService  applicationService;

    @Operation(
            summary = "Apply for a job",
            description = "Submit an application for a specific job. Only job seekers can apply"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Application submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Already applied or job not active",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "success": false,
                                        "message": "You have already applied for this job",
                                        "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Only job seekers can apply")
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @Valid @RequestBody ApplicationRequest request)
    {
        ApplicationResponse application = applicationService.applyToJob(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", application));

    }

    @Operation(
            summary = "Get my applications",
            description = "Retrieves all applications submitted by the current job seeker."
    )
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt")  String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection)
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Page<ApplicationResponse> applications = applicationService.getMyApplications(pageable);

        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", applications));
    }


    @Operation(
            summary = "Get Job applications",
            description = "Retrieves all  job applications submitted by the current job seeker." +
                    " Only Employer and admin can access"
    )
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getJobApplications(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<ApplicationResponse> applications = applicationService.getJobApplications(jobId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", applications));
    }

    @Operation(
            summary = "Get my applications by id",
            description = "Retrieves specific applications submitted by id"
    )
     // Access: JOB_SEEKER (own), EMPLOYER (job owner), ADMIN
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable Long applicationId) {
        ApplicationResponse application = applicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application retrieved successfully", application));
    }

    @Operation(
            summary = "Update application status",
            description = "Updates the status of an application. Only the job owner or admin can update."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid status transition")
    })
    /**
     * Update application status (Employer/Admin only)
     * PUT /api/applications/{applicationId}/status
     * Access: EMPLOYER (job owner), ADMIN
     */
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdate statusUpdate) {
        ApplicationResponse application = applicationService.updateApplicationStatus(applicationId, statusUpdate);
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", application));
    }

    @Operation(
            summary = "Withdraw application",
            description = "Withdraws a pending application. Only the job seeker can withdraw their own application."
    )
    /**
     * Withdraw application (Job Seeker only)
     * DELETE /api/applications/{applicationId}/withdraw
     * Access: JOB_SEEKER (own application)
     */
    @PutMapping("/{applicationId}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(
            @PathVariable Long applicationId) {
        applicationService.withdrawApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", null));
    }

    @Operation(
            summary = "Get my application statistics for a job",
            description = "Retrieves  applications statistics submitted by job id. Only job owner and admin can access"
    )
    /**
     * Get application statistics for a job
     * GET /api/applications/job/{jobId}/statistics
     * Access: EMPLOYER (job owner), ADMIN
     */
    @GetMapping("/job/{jobId}/statistics")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationStatistics>> getApplicationStatistics(
            @PathVariable Long jobId) {
        ApplicationStatistics statistics = applicationService.getApplicationStatistics(jobId);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }

    @Operation(
            summary = "Get all employer applications  for a job",
            description = "Retrieves  all applications . Only job owner and admin can access"
    )
    @GetMapping("/employer/all")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAllEmployerApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<ApplicationResponse> applications = applicationService.getAllEmployerApplications(pageable);
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", applications));
    }

}
