package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "User registration request")
public class RegisterRequest {

    @Schema(description = "User's email address", example = "john@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User's password(min 6 character", example = "password = 123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Schema(description = "User's email address",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "User's last name", examples = "Aman",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "User's phone number", example = " +91 800222XXXX")
    private String phoneNumber;

    @Schema(description = "User's role", examples = "JOB_SEEKER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is required")
    private Role role;

    @Schema(description = "Company name (required for EMPLOYER role)",
            example = "TechCorp Solutions")
    private String companyName; // Required if role is EMPLOYER
}
