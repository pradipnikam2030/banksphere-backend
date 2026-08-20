package com.pradip.banksphere.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshResult {
    private String accessToken;
    private String refreshToken;
}
