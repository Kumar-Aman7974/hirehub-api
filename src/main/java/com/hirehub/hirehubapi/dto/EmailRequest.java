package com.hirehub.hirehubapi.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EmailRequest {

    private String to;
    private String subject;
    private String templateName;  // Name of Thymeleaf template
    private Map<String, Object> variables;
    private boolean html = true;
    private String textContent;   // Fallback if template fails
}
