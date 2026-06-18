package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApplicationStatistics {

    private Long jobId;
    private String jobTitle;
    private long totalApplications;
    private Map<ApplicationStatus, Long> statusBreakdown;
    private long pendingCount;
    private long reviewedCount;
    private long interviewingCount;
    private long hiredCount;
    private long rejectedCount;
    private double hireRate; // hired / total * 100
    private double reviewRate; // reviewed / total * 100
}
