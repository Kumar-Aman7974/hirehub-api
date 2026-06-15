package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.UserResponse;
import com.hirehub.hirehubapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current authenticated user's profile
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        UserResponse userResponse = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userResponse));
    }
}

