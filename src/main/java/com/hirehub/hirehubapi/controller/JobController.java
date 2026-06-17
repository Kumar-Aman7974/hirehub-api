

package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.JobRequest;
import com.hirehub.hirehubapi.dto.JobResponse;
import com.hirehub.hirehubapi.dto.JobSearchRequest;
import com.hirehub.hirehubapi.service.JobService;
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
public class JobController {

    private final JobService jobService;

    /**
     * Create new job posting
     * POST /api/jobs
     * Access: EMPLOYER, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(@Valid @RequestBody JobRequest request) {
        JobResponse job = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted successfully", job));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", job));
    }

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


    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getMyJobs(pageable);
        return ResponseEntity.ok(ApiResponse.success("Your jobs retrieved successfully", jobs));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request) {
        JobResponse job = jobService.updateJob(id, request);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", job));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }


    @GetMapping("/employer/{employerId}")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getJobsByEmployer(
            @PathVariable Long employerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getJobsByEmployer(employerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employer jobs retrieved successfully", jobs));
    }

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