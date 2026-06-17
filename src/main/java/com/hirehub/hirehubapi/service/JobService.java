

package com.hirehub.hirehubapi.service;

import com.hirehub.hirehubapi.dto.JobRequest;
import com.hirehub.hirehubapi.dto.JobResponse;
import com.hirehub.hirehubapi.dto.JobSearchRequest;
import com.hirehub.hirehubapi.enums.JobStatus;
import com.hirehub.hirehubapi.exception.ResourceNotFoundException;
import com.hirehub.hirehubapi.model.Job;
import com.hirehub.hirehubapi.model.Role;
import com.hirehub.hirehubapi.model.User;
import com.hirehub.hirehubapi.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserService userService;


     // Create a new job posting
     // Only EMPLOYER or ADMIN can create jobs
    @Transactional
    public JobResponse createJob(JobRequest request) {

        User currentUser = userService.getCurrentUser();


        // Check if user is authorized to post jobs
        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers and admins can post jobs");
        }

        // For employers, ensure they have a company name
        if (currentUser.getRole() == Role.EMPLOYER && currentUser.getCompanyName() == null) {
            throw new IllegalStateException("Please update your profile with company name before posting jobs");
        }

        Job job = mapToEntity(request);
        job.setEmployer(currentUser);

        // Set default deadline if not provided (30 days from now)
        if (job.getApplicationDeadline() == null) {
            job.setApplicationDeadline(LocalDateTime.now().plusDays(30));
        }

        Job savedJob = jobRepository.save(job);
        log.info("New job posted: {} by employer: {}", savedJob.getTitle(), currentUser.getEmail());

        return mapToResponse(savedJob);
    }


    @Transactional
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        // Only increment view count for active jobs
        if (job.isActive() && job.getStatus() == JobStatus.ACTIVE) {
            job.incrementViewCount();
            jobRepository.save(job);
        }

        return mapToResponse(job);
    }


     // Get all jobs with pagination and filters
    @Transactional(readOnly = true)
    public Page<JobResponse> getAllJobs(JobSearchRequest searchRequest) {
        // Create pageable object
        Sort sort = Sort.by(searchRequest.getSortDirection(), searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // If search criteria provided, use search query
        if (hasSearchCriteria(searchRequest)) {
            Page<Job> jobs = jobRepository.searchJobs(
                    searchRequest.getKeyword(),
                    searchRequest.getLocation(),
                    searchRequest.getJobType(),
                    searchRequest.getMinSalary(),
                    searchRequest.getMaxSalary(),
                    pageable
            );
            return jobs.map(this::mapToResponse);
        }

        // Otherwise, get all active jobs
        Page<Job> jobs = jobRepository.findByStatusAndIsActiveTrue(JobStatus.ACTIVE, pageable);
        return jobs.map(this::mapToResponse);
    }

    //Get jobs posted by current employer or admin
    @Transactional(readOnly = true)
    public Page<JobResponse> getMyJobs(Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers can view their jobs");
        }

        Page<Job> jobs = jobRepository.findByEmployer(currentUser, pageable);
        return jobs.map(this::mapToResponse);
    }

    // Update job posting
     // Only the employer who posted it or admin can update
    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        User currentUser = userService.getCurrentUser();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        // Check authorization
        if (!job.getEmployer().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to update this job");
        }

        // Don't allow updating expired or filled jobs
        if (job.getStatus() == JobStatus.EXPIRED || job.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("Cannot update expired or filled jobs");
        }

        // Update fields
        updateJobFields(job, request);

        Job updatedJob = jobRepository.save(job);
        log.info("Job updated: {} by user: {}", updatedJob.getTitle(), currentUser.getEmail());

        return mapToResponse(updatedJob);
    }


    @Transactional
    public void deleteJob(Long id) {
        User currentUser = userService.getCurrentUser();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        // Check authorization
        if (!job.getEmployer().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to delete this job");
        }

        jobRepository.softDeleteJob(id);
        log.info("Job deleted: {} by user: {}", job.getTitle(), currentUser.getEmail());
    }


    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByEmployer(Long employerId, Pageable pageable) {
        User employer = userService.getUserByIdForInternal(employerId);
        Page<Job> jobs = jobRepository.findByEmployer(employer, pageable);
        return jobs.map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public JobStatistics getJobStatistics() {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers can view statistics");
        }

        long activeJobs = jobRepository.countByEmployerAndStatus(currentUser, JobStatus.ACTIVE);
        long expiredJobs = jobRepository.countByEmployerAndStatus(currentUser, JobStatus.EXPIRED);
        long failedJobs = jobRepository.countByEmployerAndStatus(currentUser, JobStatus.FAILED);
        long totalJobs = activeJobs + expiredJobs + failedJobs;

        return JobStatistics.builder()
                .totalJobs(totalJobs)
                .activeJobs(activeJobs)
                .expiredJobs(expiredJobs)
                .filledJobs(failedJobs)
                .build();
    }


    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expirePastDeadlineJobs() {
        int expiredCount = jobRepository.expirePastDeadlineJobs(LocalDateTime.now());
        if (expiredCount > 0) {
            log.info("Expired {} jobs that passed deadline", expiredCount);
        }
    }

    // Helper Methods
    private boolean hasSearchCriteria(JobSearchRequest request) {
        return request.getKeyword() != null ||
                request.getLocation() != null ||
                request.getJobType() != null ||
                request.getMinSalary() != null ||
                request.getMaxSalary() != null;
    }

    private Job mapToEntity(JobRequest request) {
        return Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .location(request.getLocation())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .jobType(request.getJobType())
                .status(JobStatus.ACTIVE)
                .openings(request.getOpenings())
                .experienceRequired(request.getExperienceRequired())
                .skills(request.getSkills())
                .applicationDeadline(request.getApplicationDeadline())
                .build();
    }

    private void updateJobFields(Job job, JobRequest request) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setJobType(request.getJobType());
        job.setOpenings(request.getOpenings());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSkills(request.getSkills());
        job.setApplicationDeadline(request.getApplicationDeadline());
    }

    private JobResponse mapToResponse(Job job) {
        // Create employer info
        JobResponse.EmployerInfo employerInfo = JobResponse.EmployerInfo.builder()
                .id(job.getEmployer().getId())
                .email(job.getEmployer().getEmail())
                .firstName(job.getEmployer().getFirstName())
                .lastName(job.getEmployer().getLastName())
                .companyName(job.getEmployer().getCompanyName())
                .build();

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .openings(job.getOpenings())
                .experienceRequired(job.getExperienceRequired())
                .skills(job.getSkills())
                .applicationDeadline(job.getApplicationDeadline())
                .employer(employerInfo)
                .viewCount(job.getViewCount())
                .applicationCount(job.getApplicationCount())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .isActive(job.isActive())
                .build();
    }

    // Inner class for statistics
    @lombok.Builder
    @lombok.Data
    public static class JobStatistics {
        private long totalJobs;
        private long activeJobs;
        private long expiredJobs;
        private long filledJobs;
    }
}