package com.pradip.banksphere.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResult {
    private LoginResponse loginResponse;
    private String refreshToken;

}
