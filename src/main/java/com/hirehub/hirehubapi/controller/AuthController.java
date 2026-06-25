

package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.AuthRequest;
import com.hirehub.hirehubapi.dto.AuthResponse;
import com.hirehub.hirehubapi.dto.RegisterRequest;
import com.hirehub.hirehubapi.dto.UserResponse;
import com.hirehub.hirehubapi.service.AuthService;
import com.hirehub.hirehubapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with specified role (JOB_SEEKER, EMPLOYER, or ADMIN). " +
                    "Employers must provide a company name."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = io.swagger.v3.oas.annotations.responses.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation failed or email already exists",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "success": false,
                                        "message": "Email already registered!",
                                        "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Company name required for employers",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "success": false,
                                        "message": "Company name is required for employers",
                                        "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """)))
    })

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse registeredUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", registeredUser));
    }

    @Operation(
            summary = "Authenticate user and get JWT token",
            description = "Login with email and password to receive JWT access token and refresh token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = io.swagger.v3.oas.annotations.responses.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "success": true,
                                        "message": "Login successful",
                                        "data": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                            "tokenType": "Bearer",
                                            "user": {
                                                "id": 1,
                                                "email": "john@example.com",
                                                "firstName": "John",
                                                "lastName": "Doe",
                                                "role": "JOB_SEEKER"
                                            },
                                            "expiresIn": 900
                                        },
                                        "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "success": false,
                                        "message": "Invalid email or password",
                                        "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """)))
    })


     // Authenticate user and get JWT token
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Get a new access token using a valid refresh token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Invalid or expired refresh token")
    })

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestHeader("Authorization") String refreshToken) {
        // Extract token from Bearer header
        String token = refreshToken.substring(7);
        AuthResponse authResponse = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }

    @Operation(
            summary = "Logout user",
            description = "Logs out the current user (client-side token invalidation)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}