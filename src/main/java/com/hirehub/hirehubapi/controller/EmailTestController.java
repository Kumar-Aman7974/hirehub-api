package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.model.Role;
import com.hirehub.hirehubapi.model.User;
import com.hirehub.hirehubapi.service.EmailService;
import com.hirehub.hirehubapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/test/email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/test-welcome")
    public ApiResponse<String> testWelcomeEmail(@RequestParam String email) {

        User user = new User();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.JOB_SEEKER);

        emailService.sendWelcomeEmail(user);
        return ApiResponse.success("Email service is healthy", null);
    }
}
