

package com.hirehub.hirehubapi.service;

import com.hirehub.hirehubapi.dto.EmailRequest;
import com.hirehub.hirehubapi.enums.ApplicationStatus;
import com.hirehub.hirehubapi.model.Job;
import com.hirehub.hirehubapi.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.hirehub.hirehubapi.enums.ApplicationStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // Make mail and template engine optional so app can start without mail configuration
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    // Provide defaults to avoid startup failure if properties are missing
    @Value("${email.from.address:no-reply@hirehub.local}")
    private String fromAddress;

    @Value("${email.from.name:HireHub}")
    private String fromName;

    @Value("${email.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${email.retry.delay:5000}")
    private long retryDelay;

    /**
     * Send email asynchronously
     * This method returns immediately, email sending happens in background
     */
    @Async("emailExecutor")
    public void sendEmail(EmailRequest emailRequest) {
        try {
            if (mailSender == null) {
                log.warn("JavaMailSender not configured. Skipping email to {}", emailRequest.getTo());
                return;
            }
            sendEmailWithRetry(emailRequest, 0);
        } catch (Exception e) {
            log.error("Failed to send email to: {} even after retries", emailRequest.getTo(), e);
        }
    }

    /**
     * Send email with retry logic
     */
    private void sendEmailWithRetry(EmailRequest emailRequest, int attempt) {
        try {
            // Build email content
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            // Check if HTML template is provided and template engine is available
            boolean canUseTemplate = emailRequest.isHtml() && emailRequest.getTemplateName() != null && templateEngine != null;

            if (canUseTemplate) {
                // Render Thymeleaf template
                Context context = new Context();
                context.setVariables(emailRequest.getVariables());
                String htmlContent = templateEngine.process(emailRequest.getTemplateName(), context);
                // Ensure we don't pass null into MimeMessageHelper
                if (htmlContent == null) htmlContent = "";
                helper.setText(htmlContent, true);
            } else {
                // Fallback to plain text. If caller didn't provide textContent, build a simple fallback body
                String text = emailRequest.getTextContent();
                if (text == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(emailRequest.getSubject() != null ? emailRequest.getSubject() : "Notification from HireHub");
                    Map<String, Object> vars = emailRequest.getVariables();
                    if (vars != null && !vars.isEmpty()) {
                        sb.append("\n\n");
                        vars.forEach((k, v) -> sb.append(k).append(": ").append(String.valueOf(v)).append("\n"));
                    } else {
                        sb.append("\n\nNo content available.");
                    }
                    text = sb.toString();
                }
                // use plain text fallback
                helper.setText(text, false);
            }

            // Send email
            mailSender.send(mimeMessage);
            log.info(" Email sent successfully to: {} - Subject: {}",
                    emailRequest.getTo(), emailRequest.getSubject());

        } catch (Exception e) {
            // Retry if within max attempts
            if (attempt < maxRetryAttempts) {
                log.warn("Email send failed (attempt {} of {}) to: {}. Retrying...",
                        attempt + 1, maxRetryAttempts, emailRequest.getTo());

                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ignored) {}

                sendEmailWithRetry(emailRequest, attempt + 1);
            } else {
                log.error("Email send failed after {} attempts to: {}",
                        maxRetryAttempts, emailRequest.getTo(), e);
                throw new RuntimeException("Email sending failed", e);
            }
        }
    }

    // ========== SPECIFIC EMAIL METHODS ==========

    /**
     * Send welcome email to new user
     */
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName() + " " + user.getLastName());
        variables.put("email", user.getEmail());
        variables.put("role", user.getRole().toString());
        variables.put("loginUrl", "http://localhost:8080/api/auth/login");
        variables.put("currentYear", LocalDateTime.now().getYear());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject("Welcome to HireHub! 🎉")
                .templateName("email/welcome-email")
                .variables(variables)
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send application confirmation to job seeker
     */
    public void sendApplicationConfirmation(String jobSeekerEmail, String jobTitle, String companyName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("jobSeekerName", jobSeekerEmail.split("@")[0]);
        variables.put("jobTitle", jobTitle);
        variables.put("companyName", companyName);
        variables.put("status", "PENDING");
        variables.put("dashboardUrl", "http://localhost:8080/api/applications/my-applications");
        variables.put("appliedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        EmailRequest emailRequest = EmailRequest.builder()
                .to(jobSeekerEmail)
                .subject("Application Submitted Successfully - " + jobTitle)
                .templateName("email/application-confirmation")
                .variables(variables)
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send new application notification to employer
     */
    public void sendNewApplicationNotification(String employerEmail, String applicantName,
                                               String jobTitle, String coverLetterExcerpt) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("employerName", employerEmail.split("@")[0]);
        variables.put("applicantName", applicantName);
        variables.put("jobTitle", jobTitle);
        variables.put("coverLetterExcerpt", coverLetterExcerpt);
        variables.put("applicationsUrl", "http://localhost:8080/api/applications/job/1");
        variables.put("appliedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        EmailRequest emailRequest = EmailRequest.builder()
                .to(employerEmail)
                .subject("New Application: " + applicantName + " applied to " + jobTitle)
                .templateName("email/new-application-notification")
                .variables(variables)
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send application status update to job seeker
     */
    public void sendApplicationStatusUpdate(String jobSeekerEmail, String jobTitle,
                                            ApplicationStatus status, String companyName,
                                            String notes, String interviewDate) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("jobSeekerName", jobSeekerEmail.split("@")[0]);
        variables.put("jobTitle", jobTitle);
        variables.put("companyName", companyName);
        variables.put("status", status.toString());
        variables.put("statusColor", getStatusColor(status));
        variables.put("statusMessage", getStatusMessage(status));
        variables.put("notes", notes);
        variables.put("interviewDate", interviewDate);
        variables.put("dashboardUrl", "http://localhost:8080/api/applications/my-applications");
        variables.put("updatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        String subject = getStatusUpdateSubject(status, jobTitle);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(jobSeekerEmail)
                .subject(subject)
                .templateName("email/application-status-update")
                .variables(variables)
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send job expiry reminder to employer
     */
    public void sendJobExpiryReminder(String employerEmail, Job job, int daysUntilExpiry) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("employerName", job.getEmployer().getFirstName());
        variables.put("jobTitle", job.getTitle());
        variables.put("daysUntilExpiry", daysUntilExpiry);
        variables.put("deadline", job.getApplicationDeadline());
        variables.put("applicationCount", job.getApplicationCount());
        variables.put("jobUrl", "http://localhost:8080/api/jobs/" + job.getId());
        variables.put("extendDeadlineUrl", "http://localhost:8080/api/jobs/" + job.getId() + "/extend");

        EmailRequest emailRequest = EmailRequest.builder()
                .to(employerEmail)
                .subject("Job Expiry Reminder: " + job.getTitle() + " expires in " + daysUntilExpiry + " days")
                .templateName("email/job-expiry-reminder")
                .variables(variables)
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("resetLink", "http://localhost:8080/api/auth/reset-password?token=" + resetToken);
        variables.put("expiryTime", "1 hour");

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Password Reset Request")
                .templateName("email/password-reset")
                .variables(variables)
                .build();

        sendEmail(emailRequest);
    }

    // ========== HELPER METHODS ==========

    private String getStatusColor(ApplicationStatus status) {
        switch (status) {
            case PENDING: return "#F59E0B"; // Yellow
            case REVIEWED: return "#3B82F6"; // Blue
            case INTERVIEWING: return "#8B5CF6"; // Purple
            case HIRED: return "#10B981"; // Green
            case REJECTED: return "#EF4444"; // Red
            case WITHDRAWN: return "#6B7280"; // Gray
            default: return "#000000";
        }
    }

    private String getStatusMessage(ApplicationStatus status) {
        switch (status) {
            case PENDING: return "Your application is waiting for review";
            case REVIEWED: return "Your application has been reviewed";
            case INTERVIEWING: return "Congratulations! You've been selected for an interview";
            case HIRED: return "Congratulations! You've been hired!";
            case REJECTED: return "We appreciate your interest, but unfortunately...";
            case WITHDRAWN: return "You have withdrawn this application";
            default: return "Status updated";
        }
    }

    private String getStatusUpdateSubject(ApplicationStatus status, String jobTitle) {
        switch (status) {
            case HIRED: return "Congratulations! Hired for " + jobTitle;
            case INTERVIEWING: return "Interview Scheduled for " + jobTitle;
            case REJECTED: return "Application Update for " + jobTitle;
            default: return "Application Status Updated - " + jobTitle;
        }
    }
}