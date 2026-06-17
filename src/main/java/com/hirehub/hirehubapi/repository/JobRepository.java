package com.hirehub.hirehubapi.repository;

import com.hirehub.hirehubapi.enums.JobStatus;
import com.hirehub.hirehubapi.enums.JobType;
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


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository  extends JpaRepository<Job, Long> {


    Page<Job> findByEmployer(User employer, Pageable pageable);
    // Find active jobs
    Page<Job> findByStatusAndIsActiveTrue(JobStatus status, Pageable pageable);

    // Find jobs by status
    List<Job> findByStatus(JobStatus status);

    // JPQL Advanced search with multiple filters
    @Query("SELECT j FROM Job j WHERE " +
            "j.isActive = true AND " +
            "j.status = 'ACTIVE' AND " +
            "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:jobType IS NULL OR j.jobType = :jobType) AND " +
            "(:minSalary IS NULL OR j.salaryMax >= :minSalary) AND " +
            "(:maxSalary IS NULL OR j.salaryMin <= :maxSalary)")

    Page<Job> searchJobs(@Param("keyword") String keyword,
                         @Param("location") String location,
                         @Param("jobType") JobType jobType,
                         @Param("minSalary") BigDecimal minSalary,
                         @Param("maxSalary") BigDecimal maxSalary,
                         Pageable pageable);

    // Find jobs expiration soon(next 7 days)
    @Query("SELECT j FROM Job j WHERE j.applicationDeadline BETWEEN :start AND :end AND j.status = 'ACTIVE'")
    List<Job> findExpirationJobs(@Param("start")LocalDateTime start, @Param("end") LocalDateTime end);


    // Update job status based on deadline
    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.status = 'EXPIRED' WHERE j.applicationDeadline < :now AND j.status = 'ACTIVE'")
    int expirePastDeadlineJobs(@Param("now") LocalDateTime now);

    // Get job statistics by employer
    @Query("SELECT COUNT(j) FROM Job j WHERE j.employer = :employer AND j.status = :status")
    long countByEmployerAndStatus(@Param("employer") User employer, @Param("status") JobStatus status);

    // Find jobs by skill (for recommendations)
    @Query("SELECT j FROM Job j WHERE j.skills LIKE CONCAT('%', :skill, '%') AND j.status = 'ACTIVE'")
    Page<Job> findBySkill(@Param("skill") String skill, Pageable pageable);

    // Get top viewed jobs
    Page<Job> findAllByOrderByViewCountDesc(Pageable pageable);

    // Check if employer owns the job
    boolean existsByIdAndEmployer(Long id, User employer);

    // Soft delete job
    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.isActive = false WHERE j.id = :jobId")
    void softDeleteJob(@Param("jobId") Long jobId);

}
