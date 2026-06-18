package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.*;
import com.hirehub.hirehubapi.service.ApplicationService;
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
public class ApplicationController {

    private final ApplicationService  applicationService;

    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @Valid @RequestBody ApplicationRequest request)
    {
        ApplicationResponse application = applicationService.applyToJob(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", application));

    }

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


     // Access: JOB_SEEKER (own), EMPLOYER (job owner), ADMIN

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable Long applicationId) {
        ApplicationResponse application = applicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application retrieved successfully", application));
    }

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
