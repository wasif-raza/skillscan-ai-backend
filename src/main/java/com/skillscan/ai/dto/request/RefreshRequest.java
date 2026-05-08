package com.skillscan.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }


}