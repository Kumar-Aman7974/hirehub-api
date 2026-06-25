

package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.JobRequest;
import com.hirehub.hirehubapi.dto.JobResponse;
import com.hirehub.hirehubapi.dto.JobSearchRequest;
import com.hirehub.hirehubapi.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "job Management", description = "APIs for managing job postings")
@SecurityRequirement(name = "Bearer Authentication")
public class JobController {

    private final JobService jobService;

    @Operation(
            summary = "Create a new job posting",
            description = "Creates a new job posting. Only employers and admins can create jobs."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Job created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Access denied (not employer/admin)")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(@Valid @RequestBody JobRequest request) {
        JobResponse job = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted successfully", job));
    }

    @Operation(
            summary = "Get job by ID",
            description = "Retrieves a specific job by its ID. Also increments view count."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Job found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Job not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", job));
    }

    @Operation(
            summary = "Get all jobs with filters",
            description = "Retrieves paginated list of jobs with optional filters for keyword, " +
                    "location, job type, and salary range."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Jobs retrieved successfully")
    })
    /**
     * Get all jobs with filters and pagination
     * GET /api/jobs?page=0&size=10&keyword=java&location=NYC
     * Access: PUBLIC
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        JobSearchRequest searchRequest = new JobSearchRequest();
        searchRequest.setKeyword(keyword);
        searchRequest.setLocation(location);
        if (jobType != null) {
            searchRequest.setJobType(com.hirehub.hirehubapi.enums.JobType.valueOf(jobType));
        }
        if (minSalary != null) {
            searchRequest.setMinSalary(java.math.BigDecimal.valueOf(minSalary));
        }
        if (maxSalary != null) {
            searchRequest.setMaxSalary(java.math.BigDecimal.valueOf(maxSalary));
        }
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(Sort.Direction.fromString(sortDirection));

        Page<JobResponse> jobs = jobService.getAllJobs(searchRequest);
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", jobs));
    }


    @Operation(
            summary = "Get jobs posted by current employer",
            description = "Retrieves all job posted by the currently authenticated employer."
    )
    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getMyJobs(pageable);
        return ResponseEntity.ok(ApiResponse.success("Your jobs retrieved successfully", jobs));
    }


    @Operation(
            summary = "Update a job posting",
            description = "Updates an existing job posting. Only the job owner or admin can update."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request) {
        JobResponse job = jobService.updateJob(id, request);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", job));
    }

    @Operation(
            summary = "Delete a job posting",
            description = "Soft deletes a job posting. Only the job owner or admin can delete."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @Operation(
            summary = "Get job by id",
            description = "Get your job by using id like- 1,2, 3, 4, 5."
    )
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getJobsByEmployer(
            @PathVariable Long employerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getJobsByEmployer(employerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employer jobs retrieved successfully", jobs));
    }

    @Operation(
            summary = "Get job statistics for dashboard",
            description = "SStatistics retrieved successfully. Only the job owner or admin can do this."
    )
    /**
     * Get job statistics for dashboard
     * GET /api/jobs/statistics
     * Access: EMPLOYER, ADMIN
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobService.JobStatistics>> getJobStatistics() {
        JobService.JobStatistics statistics = jobService.getJobStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }
}