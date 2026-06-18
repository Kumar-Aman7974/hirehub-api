package com.hirehub.hirehubapi.repository;

import com.hirehub.hirehubapi.enums.ApplicationStatus;
import com.hirehub.hirehubapi.model.Application;
import com.hirehub.hirehubapi.model.Job;
import com.hirehub.hirehubapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {


    // Find application by job and job seeker (for duplicate check)
    Optional<Application> findByJobAndJobSeeker(Job job, User jobSeeker);

    boolean existsByJobAndJobSeeker(Job job, User jobSeeker);


    Page<Application> findByJobSeeker(User jobSeeker, Pageable pageable);

    List<Application> findByJobSeeker(User jobSeeker);

    // Find applications by job (for employer)
    Page<Application> findByJob(Job job, Pageable pageable);

    // Find applications by job and status
    Page<Application> findByJobAndStatus(Job job, ApplicationStatus status, Pageable pageable);

    // Find pending applications for a job (for employer)
    @Query("SELECT a FROM Application a WHERE a.job = :job AND a.status = 'PENDING' ORDER BY a.appliedAt ASC")
    List<Application> findPendingApplicationsByJob(@Param("job") Job job);

    // Count applications by job and status
    long countByJobAndStatus(Job job, ApplicationStatus status);

    // Count total applications for a job
    long countByJob(Job job);

    // Get application statistics by job
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job = :job GROUP BY a.status")
    List<Object[]> countApplicationsByStatusForJob(@Param("job") Job job);

    // Find applications that have been pending for more than X days
    @Query("SELECT a FROM Application a WHERE a.status = 'PENDING' AND a.appliedAt < :cutoffDate")
    List<Application> findStalePendingApplications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    // Get all applications for an employer (across all their jobs)
    @Query("SELECT a FROM Application a WHERE a.job.employer = :employer")
    Page<Application> findByEmployer(@Param("employer") User employer, Pageable pageable);

    // Count applications for employer by status
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.employer = :employer GROUP BY a.status")
    List<Object[]> countApplicationsByStatusForEmployer(@Param("employer") User employer);

    // Find applications by job seeker and status
    Page<Application> findByJobSeekerAndStatus(User jobSeeker, ApplicationStatus status, Pageable pageable);

    // Soft delete applications for a job (when job is deleted)
    @Modifying
    @Transactional
    @Query("UPDATE Application a SET a.isActive = false WHERE a.job = :job")
    void softDeleteApplicationsByJob(@Param("job") Job job);

    // Soft delete applications by job seeker
    @Modifying
    @Transactional
    @Query("UPDATE Application a SET a.isActive = false WHERE a.jobSeeker = :jobSeeker")
    void softDeleteApplicationsByJobSeeker(@Param("jobSeeker") User jobSeeker);

    // Find recent applications for a job (for notifications)
    @Query("SELECT a FROM Application a WHERE a.job = :job AND a.appliedAt > :since")
    List<Application> findRecentApplicationsByJob(@Param("job") Job job,
                                                  @Param("since") java.time.LocalDateTime since);

    // Get application count trends (daily)
    @Query("SELECT DATE(a.appliedAt), COUNT(a) FROM Application a WHERE a.job = :job GROUP BY DATE(a.appliedAt)")
    List<Object[]> getApplicationTrends(@Param("job") Job job);
}

