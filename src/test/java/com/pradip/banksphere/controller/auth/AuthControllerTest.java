package com.pradip.banksphere.controller.auth;

import com.pradip.banksphere.dto.request.LoginRequest;
import com.pradip.banksphere.dto.response.LoginResponse;
import com.pradip.banksphere.dto.response.LoginResult;
import com.pradip.banksphere.dto.response.RefreshResult;
import com.pradip.banksphere.service.user.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.allOf;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {

        // Arrange
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(6L)
                .username("radha")
                .email("radha@gmail.com")
                .token("access-token")
                .message("Login Successful")
                .build();

        LoginResult loginResult = LoginResult.builder()
                .loginResponse(loginResponse)
                .refreshToken("refresh-token")
                .build();

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(loginResult);

        String requestBody = """
            {
                "email": "radha@gmail.com",
                "password": "password123"
            }
            """;

        // Act + Assert
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(6))
                .andExpect(jsonPath("$.username").value("radha"))
                .andExpect(jsonPath("$.email").value("radha@gmail.com"))
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.message").value("Login Successful"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                       containsString("refresh_token=refresh-token")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        allOf(
                                containsString("refresh_token=refresh-token"),
                                containsString("Path=/api/v1/auth"),
                                containsString("HttpOnly")
                        )
                ));
    }
    @Test
    void login_shouldReturn400_whenRequestIsInvalid() throws Exception {

        String requestBody = """
            {
                "email": "",
                "password": ""
            }
            """;

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .login(any(LoginRequest.class));
    }

    @Test
    void refresh_shouldReturnNewAccessToken_whenRefreshTokenIsValid()
            throws Exception {

        // Arrange

        RefreshResult refreshResult = RefreshResult.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(userService.refreshAccessToken("old-refresh-token"))
                .thenReturn(refreshResult);


        // Act + Assert

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .cookie(
                                        new Cookie(
                                                "refresh_token",
                                                "old-refresh-token"
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        allOf(
                                containsString("refresh_token=new-refresh-token"),
                                containsString("Path=/api/v1/auth"),
                                containsString("HttpOnly"),
                                containsString("SameSite=Strict")
                        )
                ));

        verify(userService)
                .refreshAccessToken("old-refresh-token");
    }
    @Test
    void logOut_shouldRevokeRefreshTokenAndDeleteCookie()
            throws Exception {

        // Act + Assert

        mockMvc.perform(
                        post("/api/v1/auth/logOut")
                                .cookie(
                                        new Cookie(
                                                "refresh_token",
                                                "refresh-token"
                                        )
                                )
                )
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        allOf(
                                containsString("refresh_token="),
                                containsString("Max-Age=0"),
                                containsString("Path=/api/v1/auth"),
                                containsString("HttpOnly"),
                                containsString("SameSite=Strict")
                        )
                ));

        // Verify

        verify(userService)
                .logOut("refresh-token");
    }
    @Test
    void refresh_shouldReturn400_whenRefreshTokenCookieIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .refreshAccessToken(any(String.class));
    }


}
