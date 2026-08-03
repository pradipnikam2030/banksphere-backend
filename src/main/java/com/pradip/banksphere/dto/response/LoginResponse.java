package com.pradip.banksphere.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private Long userId;

    private String username;

    private String email;

    private String message;
}
