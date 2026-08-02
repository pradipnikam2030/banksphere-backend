package com.pradip.banksphere.controller.auth;

import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.RegisterResponse;
import com.pradip.banksphere.repository.user.UserRepository;
import com.pradip.banksphere.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public RegisterResponse RegisterRequest(@Valid @RequestBody RegisterRequest registerRequest){
        return userService.register(registerRequest);

    }
}
