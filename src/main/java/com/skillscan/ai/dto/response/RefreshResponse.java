package com.skillscan.ai.dto.response;

import lombok.*;

@Data
@Builder
@ToString(exclude = "accessToken")
public class RefreshResponse {

    private String accessToken;

    private String refreshToken;
}
