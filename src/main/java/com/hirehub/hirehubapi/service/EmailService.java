package com.hirehub.hirehubapi.service;


import com.hirehub.hirehubapi.enums.ApplicationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void sendApplicationConfirmation(String email, String jobTitle) {
        log.info("Sending application confirmation to: {} for job: {}", email, jobTitle);


    }

    public  void sendNewApplicationNotification(String employerEmail, String applicationName, String jobTitle) {
        log.info("Sending new application notification to: {} from: {} for job: {}", employerEmail,
                applicationName, jobTitle);
    }

    public void sendApplicationStatusUpdate(String email, String jobTitle,
                                            ApplicationStatus status, String rejectionReason) {
        log.info(" Sending status update to: {} for job: {} - Status: {}",
                email, jobTitle, status);
        if (status == ApplicationStatus.REJECTED) {
            log.info("Rejection reason: {}", rejectionReason);
        }

    }

    public void sendJobExpiryNotification(String employerEmail, String jobTitle) {
        log.info(" Sending job expiry notification to: {} for job: {}", employerEmail, jobTitle);

    }
}
