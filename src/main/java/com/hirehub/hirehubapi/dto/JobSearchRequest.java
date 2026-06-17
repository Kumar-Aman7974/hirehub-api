package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.JobType;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

@Data
public class JobSearchRequest {

    private String keyword;        // Search in title and description
    private String location;
    private JobType jobType;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String experienceRequired;
    private String skills;

    // Pagination
    private Integer page = 0;
    private Integer size = 10;

    // Sorting
    private String sortBy = "createdAt";
    private Sort.Direction sortDirection = Sort.Direction.DESC;


}
