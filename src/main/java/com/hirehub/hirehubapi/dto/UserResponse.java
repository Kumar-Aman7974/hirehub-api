package com.hirehub.hirehubapi.dto;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Data;
import com.hirehub.hirehubapi.model.Role;


import java.time.LocalDateTime;

@Builder
@Data
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private Role role;
    private String companyName;
    private LocalDateTime createdAt;



}
