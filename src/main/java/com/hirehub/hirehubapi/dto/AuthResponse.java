package com.hirehub.hirehubapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private  String refreshToken;
    private String tokenType = "Bearer";
    private UserResponse user;

}
