

package com.hirehub.hirehubapi.service;

import com.hirehub.hirehubapi.dto.AuthResponse;
import com.hirehub.hirehubapi.dto.AuthRequest;
import com.hirehub.hirehubapi.dto.UserResponse;
import com.hirehub.hirehubapi.model.User;
import com.hirehub.hirehubapi.repository.UserRepository;
import com.hirehub.hirehubapi.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    /**
     * Authenticate user and generate tokens
     */
    public AuthResponse authenticateUser(AuthRequest request) {
        // 1. Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Set authentication in context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // 5. Build response
        UserResponse userResponse = userService.mapToUserResponse(user); // Add this method to UserService

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .expiresIn(900) // 15 minutes in seconds
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user);

        UserResponse userResponse = userService.mapToUserResponse(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .expiresIn(900)
                .build();
    }

    /**
     * Logout user (invalidate token on client side)
     */
    public void logout() {
        // For JWT, logout is client-side
        // Server just clears context
        SecurityContextHolder.clearContext();
        log.info("User logged out");
    }
}