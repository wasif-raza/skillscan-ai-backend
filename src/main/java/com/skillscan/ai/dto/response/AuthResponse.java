package com.skillscan.ai.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = {"accessToken","refreshToken"})
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
}