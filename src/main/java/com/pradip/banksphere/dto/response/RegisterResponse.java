package com.pradip.banksphere.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RegisterResponse {
    private Long userId;
    private String name;
    private String email;
    private String username;
    private String message;
}
