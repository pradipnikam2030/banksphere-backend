package com.pradip.banksphere.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 8,max = 20)
    private String password;
}
