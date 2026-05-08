package com.skillscan.ai.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "accessToken")
public class RefreshResponse {

    private String accessToken;
}