

package com.hirehub.hirehubapi.service;

import com.hirehub.hirehubapi.dto.*;
import com.hirehub.hirehubapi.enums.ApplicationStatus;
import com.hirehub.hirehubapi.enums.JobStatus;
import com.hirehub.hirehubapi.exception.ResourceNotFoundException;
import com.hirehub.hirehubapi.exception.UnauthorizedException;
import com.hirehub.hirehubapi.model.*;
import com.hirehub.hirehubapi.repository.ApplicationRepository;
import com.hirehub.hirehubapi.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserService userService;

    @Autowired
    private EmailService emailService;

    /**
     * Apply for a job
     * Only JOB_SEEKER can apply
     */
    @Transactional
    public ApplicationResponse applyToJob(ApplicationRequest request) {
        User currentUser = userService.getCurrentUser();

        // Validate user role
        if (currentUser.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can apply for jobs");
        }

        // Get the job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));

        // Validate job is active and accepting applications
        if (!job.isActive() || job.getStatus() != JobStatus.ACTIVE) {
            throw new IllegalStateException("This job is no longer accepting applications");
        }

        // Check if deadline has passed
        if (job.getApplicationDeadline() != null &&
                job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Application deadline has passed");
        }

        // Check for duplicate application
        if (applicationRepository.existsByJobAndJobSeeker(job, currentUser)) {
            throw new IllegalStateException("You have already applied for this job");
        }

        // If resume file ID is provided, use it
        String resumeFileId = request.getResumeFileId();

        Application application = Application.builder()
                .job(job)
                .jobSeeker(currentUser)
                .coverLetter(request.getCoverLetter())
                .resumeFileId(resumeFileId)  // Store file reference
                .status(ApplicationStatus.PENDING)
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Update job application count
        job.incrementApplicationCount();
        jobRepository.save(job);

        log.info("New application submitted: {} for job: {} by: {}",
                savedApplication.getId(), job.getTitle(), currentUser.getEmail());

        // Send email notifications (async)
        try {
            // To job seeker
            emailService.sendApplicationConfirmation(
                    currentUser.getEmail(),
                    job.getTitle(),
                    job.getEmployer().getCompanyName()
            );

            // To employer
            emailService.sendNewApplicationNotification(
                    job.getEmployer().getEmail(),
                    currentUser.getFirstName() + " " + currentUser.getLastName(),
                    job.getTitle(),
                    request.getCoverLetter().length() > 100 ?
                            request.getCoverLetter().substring(0, 100) + "..." :
                            request.getCoverLetter()
            );
        } catch (Exception e) {
            log.warn("Failed to send email notifications: {}", e.getMessage());
        }

        return mapToResponse(savedApplication);
    }

    /**
     * Get all applications for current job seeker
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can view their applications");
        }

        Page<Application> applications = applicationRepository.findByJobSeeker(currentUser, pageable);
        return applications.map(this::mapToResponse);
    }

    /**
     * Get applications for a specific job (Employer view)
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getJobApplications(Long jobId, Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Check authorization
        if (!job.getEmployer().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to view applications for this job");
        }

        Page<Application> applications = applicationRepository.findByJob(job, pageable);
        return applications.map(this::mapToResponse);
    }

    /**
     * Get application details by ID
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        User currentUser = userService.getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        // Check authorization
        boolean isJobSeeker = application.getJobSeeker().getId().equals(currentUser.getId());
        boolean isEmployer = application.getJob().getEmployer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isJobSeeker && !isEmployer && !isAdmin) {
            throw new AccessDeniedException("You don't have permission to view this application");
        }

        return mapToResponse(application);
    }

    /**
     * Update application status (Employer/Admin only)
     */
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusUpdate statusUpdate) {
        User currentUser = userService.getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        // Check authorization
        boolean isEmployer = application.getJob().getEmployer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isEmployer && !isAdmin) {
            throw new AccessDeniedException("Only the employer or admin can update application status");
        }

        // Validate status transition
        if (!statusUpdate.isValidStatusTransition()) {
            throw new IllegalArgumentException("Invalid status transition: " + statusUpdate.getStatus());
        }

        // Don't allow changes to terminal statuses
        if (application.getStatus().isTerminal()) {
            throw new IllegalStateException("Cannot update a " + application.getStatus() + " application");
        }

        // Update status
        ApplicationStatus newStatus = statusUpdate.getStatus();
        application.updateStatus(newStatus);

        // Set additional fields based on status
        if (newStatus == ApplicationStatus.REJECTED) {
            application.setRejectionReason(statusUpdate.getRejectionReason());
        } else if (newStatus == ApplicationStatus.INTERVIEWING) {
            application.setInterviewDate(statusUpdate.getInterviewDate());
        } else if (newStatus == ApplicationStatus.REVIEWED) {
            application.setReviewedAt(LocalDateTime.now());
        }

        if (statusUpdate.getAdditionalNotes() != null) {
            application.setAdditionalNotes(statusUpdate.getAdditionalNotes());
        }

        Application updatedApplication = applicationRepository.save(application);

        log.info("Application {} status updated to: {} by: {}",
                applicationId, newStatus, currentUser.getEmail());

        // Send email notification to job seeker
        try {
            emailService.sendApplicationStatusUpdate(
                    application.getJobSeeker().getEmail(),
                    application.getJob().getTitle(),
                    newStatus,
                    application.getJob().getEmployer().getCompanyName(),
                    statusUpdate.getAdditionalNotes(),
                    statusUpdate.getInterviewDate() != null ?
                            statusUpdate.getInterviewDate().toString() : null
            );
        } catch (Exception e) {
            log.warn("Failed to send status update email: {}", e.getMessage());
        }

        return mapToResponse(updatedApplication);
    }

    /**
     * Withdraw application (Job Seeker only)
     */
    @Transactional
    public void withdrawApplication(Long applicationId) {
        User currentUser = userService.getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        // Check if user is the job seeker
        if (!application.getJobSeeker().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only withdraw your own applications");
        }

        if (!application.canWithdraw()) {
            throw new IllegalStateException("This application cannot be withdrawn in its current status");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        log.info("Application {} withdrawn by job seeker: {}", applicationId, currentUser.getEmail());
    }

    /**
     * Get application statistics for a job (Employer view)
     */
    @Transactional(readOnly = true)
    public ApplicationStatistics getApplicationStatistics(Long jobId) {
        User currentUser = userService.getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Check authorization
        if (!job.getEmployer().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to view statistics for this job");
        }

        long totalApplications = applicationRepository.countByJob(job);
        List<Object[]> statusCounts = applicationRepository.countApplicationsByStatusForJob(job);

        Map<ApplicationStatus, Long> statusBreakdown = new HashMap<>();
        for (Object[] row : statusCounts) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            statusBreakdown.put(status, count);
        }

        long pendingCount = statusBreakdown.getOrDefault(ApplicationStatus.PENDING, 0L);
        long reviewedCount = statusBreakdown.getOrDefault(ApplicationStatus.REVIEWED, 0L);
        long interviewingCount = statusBreakdown.getOrDefault(ApplicationStatus.INTERVIEWING, 0L);
        long hiredCount = statusBreakdown.getOrDefault(ApplicationStatus.HIRED, 0L);
        long rejectedCount = statusBreakdown.getOrDefault(ApplicationStatus.REJECTED, 0L);

        double hireRate = totalApplications > 0 ? (double) hiredCount / totalApplications * 100 : 0;
        double reviewRate = totalApplications > 0 ? (double) (reviewedCount + interviewingCount + hiredCount + rejectedCount) / totalApplications * 100 : 0;

        return ApplicationStatistics.builder()
                .jobId(jobId)
                .jobTitle(job.getTitle())
                .totalApplications(totalApplications)
                .statusBreakdown(statusBreakdown)
                .pendingCount(pendingCount)
                .reviewedCount(reviewedCount)
                .interviewingCount(interviewingCount)
                .hiredCount(hiredCount)
                .rejectedCount(rejectedCount)
                .hireRate(hireRate)
                .reviewRate(reviewRate)
                .build();
    }

    /**
     * Get all applications for employer (across all jobs)
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllEmployerApplications(Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers can view all applications");
        }

        Page<Application> applications = applicationRepository.findByEmployer(currentUser, pageable);
        return applications.map(this::mapToResponse);
    }

    // Helper Methods
    private ApplicationResponse mapToResponse(Application application) {
        // Job info
        ApplicationResponse.JobInfo jobInfo = ApplicationResponse.JobInfo.builder()
                .id(application.getJob().getId())
                .title(application.getJob().getTitle())
                .location(application.getJob().getLocation())
                .companyName(application.getJob().getEmployer().getCompanyName())
                .build();

        // Job seeker info
        ApplicationResponse.JobSeekerInfo jobSeekerInfo = ApplicationResponse.JobSeekerInfo.builder()
                .id(application.getJobSeeker().getId())
                .email(application.getJobSeeker().getEmail())
                .firstName(application.getJobSeeker().getFirstName())
                .lastName(application.getJobSeeker().getLastName())
                .phoneNumber(application.getJobSeeker().getPhoneNumber())
                .resumeUrl(application.getJobSeeker().getResumeUrl())
                .build();

        return ApplicationResponse.builder()
                .id(application.getId())
                .job(jobInfo)
                .jobSeeker(jobSeekerInfo)
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .additionalNotes(application.getAdditionalNotes())
                .rejectionReason(application.getRejectionReason())
                .interviewDate(application.getInterviewDate())
                .appliedAt(application.getAppliedAt())
                .reviewedAt(application.getReviewedAt())
                .updatedAt(application.getUpdatedAt())
                .isActive(application.isActive())
                .build();
    }
}