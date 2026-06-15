

package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.AuthRequest;
import com.hirehub.hirehubapi.dto.AuthResponse;
import com.hirehub.hirehubapi.dto.RegisterRequest;
import com.hirehub.hirehubapi.dto.UserResponse;
import com.hirehub.hirehubapi.service.AuthService;
import com.hirehub.hirehubapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse registeredUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", registeredUser));
    }

    /**
     * Authenticate user and get JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestHeader("Authorization") String refreshToken) {
        // Extract token from Bearer header
        String token = refreshToken.substring(7);
        AuthResponse authResponse = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }

    /**
     * Logout user
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}