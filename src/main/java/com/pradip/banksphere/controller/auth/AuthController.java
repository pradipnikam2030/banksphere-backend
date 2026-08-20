package com.pradip.banksphere.controller.auth;

import com.pradip.banksphere.dto.request.LoginRequest;
import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.LoginResponse;
import com.pradip.banksphere.dto.response.LoginResult;
import com.pradip.banksphere.dto.response.RefreshResult;
import com.pradip.banksphere.dto.response.RegisterResponse;
import com.pradip.banksphere.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public RegisterResponse registerRequest(@Valid @RequestBody RegisterRequest registerRequest){
        return userService.register(registerRequest);

    }

    @PostMapping("/login")
    public LoginResponse loginUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        LoginResult loginResult = userService.login(loginRequest);

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(
                                "refresh_token",
                                loginResult.getRefreshToken()
                        )
                        .httpOnly(true)
                        .secure(false)// true in production with HTTPS
                        .path("/api/v1/auth")
                        .maxAge(Duration.ofDays(7))
                        .sameSite("Strict")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        return loginResult.getLoginResponse();
    }

    @PostMapping("/refresh")
    public RefreshResult refreshToken(
            @CookieValue("refresh_token") String rawRefreshToken,
            HttpServletResponse response) {

        RefreshResult refreshResult =
                userService.refreshAccessToken(rawRefreshToken);

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(
                                "refresh_token",
                                refreshResult.getRefreshToken()
                        )
                        .httpOnly(true)
                        .secure(false) // true in production with HTTPS
                        .path("/api/v1/auth")
                        .maxAge(Duration.ofDays(7))
                        .sameSite("Strict")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        return RefreshResult.builder()
                .accessToken(refreshResult.getAccessToken())
                .build();
    }

    @PostMapping("/logOut")
    public ResponseEntity<Void> logOut(@CookieValue("refresh_token") String refreshRawToken,
                                       HttpServletResponse response){

        userService.logOut(refreshRawToken);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .path("/api/v1/auth")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();

    }
}
