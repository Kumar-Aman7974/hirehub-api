package com.hirehub.hirehubapi.service;


import com.hirehub.hirehubapi.enums.JobStatus;
import com.hirehub.hirehubapi.model.Job;
import com.hirehub.hirehubapi.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final JobRepository jobRepository;
    private final EmailService emailService;

    /**
     * Send job expiry reminders daily at 9 AM
     * Reminds employers when jobs are expiring in 7, 3, and 1 days
     */
    @Scheduled(cron = "0 0 9 * * ?")  // 9 AM daily
    public void sendJobExpiryReminders() {
        log.info("Starting job expiry reminder check...");

        LocalDateTime now = LocalDateTime.now();
        List<Job> activeJobs = jobRepository.findByStatus(JobStatus.ACTIVE);

        int remindersSent = 0;
        for (Job job : activeJobs) {
            if (job.getApplicationDeadline() == null) continue;

            long daysUntilExpiry = ChronoUnit.DAYS.between(now, job.getApplicationDeadline());

            // Send reminders at 7, 3, and 1 day before expiry
            if (daysUntilExpiry == 7 || daysUntilExpiry == 3 || daysUntilExpiry == 1) {
                try {
                    emailService.sendJobExpiryReminder(
                            job.getEmployer().getEmail(),
                            job,
                            (int) daysUntilExpiry
                    );
                    remindersSent++;
                    log.info("Sent expiry reminder for job: {} ({} days remaining)",
                            job.getTitle(), daysUntilExpiry);
                } catch (Exception e) {
                    log.error("Failed to send expiry reminder for job: {}", job.getTitle(), e);
                }
            }

            // If job expired, mark as EXPIRED
            if (daysUntilExpiry < 0 && job.getStatus() == JobStatus.ACTIVE) {
                job.setStatus(JobStatus.EXPIRED);
                jobRepository.save(job);
                log.info("Job expired automatically: {}", job.getTitle());
            }
        }

        log.info("Job expiry reminder check complete. Sent {} reminders.", remindersSent);
    }

    /**
     * Send weekly digest to job seekers (Sunday at 10 AM)
     * This would be expanded to send personalized recommendations
     */
    @Scheduled(cron = "0 0 10 * * SUN")  // Sunday 10 AM
    public void sendWeeklyDigest() {
        log.info("Starting weekly digest email...");
        // TODO: Implement weekly digest with job recommendations
        // This would query popular jobs and send to all job seekers
        log.info("Weekly digest email complete.");
    }
}
