package com.pradip.banksphere.service.impl;

import com.pradip.banksphere.dto.request.LoginRequest;
import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.LoginResult;
import com.pradip.banksphere.dto.response.RefreshResult;
import com.pradip.banksphere.dto.response.RegisterResponse;
import com.pradip.banksphere.entity.refresh.RefreshToken;
import com.pradip.banksphere.entity.role.Role;
import com.pradip.banksphere.entity.user.User;
import com.pradip.banksphere.enums.RoleType;
import com.pradip.banksphere.exception.EmailAlreadyExistsException;
import com.pradip.banksphere.exception.InvalidCredentialsException;
import com.pradip.banksphere.exception.RoleNotFoundException;
import com.pradip.banksphere.repository.role.RoleRepository;
import com.pradip.banksphere.repository.user.UserRepository;
import com.pradip.banksphere.security.CustomUserDetails;
import com.pradip.banksphere.service.jwt.JwtService;
import com.pradip.banksphere.service.refresh.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;
    @Test
    public void login_shouldReturnLoginResult_whenCredentialsAreValid() {

        // Arrange

        LoginRequest loginRequest = LoginRequest.builder()
                .email("radha@gmail.com")
                .password("password123")
                .build();

        Role role = Role.builder()
                .roleName(RoleType.CUSTOMER)
                .build();

        User user = User.builder()
                .userId(6L)
                .username("radha@gmail.com")
                .email("radha@gmail.com")
                .password("encoded-password")
                .role(role)
                .enabled(true)
                .build();

        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        doReturn(userDetails.getAuthorities())
                .when(authentication)
                .getAuthorities();

        when(jwtService.generateToken(
                eq("6"),
                any()
        )).thenReturn("access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn("refresh-token");


        // Act

        LoginResult result = userService.login(loginRequest);


        // Assert

        assertNotNull(result);

        assertNotNull(result.getLoginResponse());

        assertEquals(
                6L,
                result.getLoginResponse().getUserId()
        );

        assertEquals(
                "radha@gmail.com",
                result.getLoginResponse().getEmail()
        );

        assertEquals(
                "access-token",
                result.getLoginResponse().getToken()
        );

        assertEquals(
                "refresh-token",
                result.getRefreshToken()
        );

        assertEquals(
                "Login Successful",
                result.getLoginResponse().getMessage()
        );
    }


    @Test
    void register_shouldCreateUser_whenRequestIsValid() {

        // Arrange

        when(userRepository.existsByEmail("radha@gmail.com"))
                .thenReturn(false);

        Role customerRole = Role.builder()
                .roleName(RoleType.CUSTOMER)
                .build();

        when(roleRepository.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("radha@gmail.com")
                .username("radha")
                .password("password123")
                .build();


        // Act

        RegisterResponse result =
                userService.register(registerRequest);


        // Assert

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(
                "radha@gmail.com",
                savedUser.getEmail()
        );

        assertEquals(
                "radha",
                savedUser.getUsername()
        );

        assertEquals(
                "encoded-password",
                savedUser.getPassword()
        );

        assertEquals(
                RoleType.CUSTOMER,
                savedUser.getRole().getRoleName()
        );

        assertTrue(savedUser.getEnabled());

        assertNotNull(result);

        assertEquals(
                "radha@gmail.com",
                result.getEmail()
        );

        assertEquals(
                "radha",
                result.getUsername()
        );

        assertEquals(
                "Registration Successful",
                result.getMessage()
        );
    }
    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("radha@gmail.com")
                .username("radha")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("radha@gmail.com"))
                .thenReturn(true);

        // Act + Assert
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.register(registerRequest)
        );

        assertEquals(
                "Email Already Exists",
                exception.getMessage()
        );
    }

    @Test
    void register_shouldThrowException_whenCustomerRoleDoesNotExist() {

        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("radha@gmail.com")
                .username("radha")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("radha@gmail.com"))
                .thenReturn(false);

        when(roleRepository.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.empty());

        // Act + Assert
        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> userService.register(registerRequest)
        );

        assertEquals(
                "Role does not exists",
                exception.getMessage()
        );
    }
    @Test
    void refreshAccessToken_shouldGenerateNewTokens_whenRefreshTokenIsValid() {

        // Arrange

        String rawRefreshToken = "old-refresh-token";
        Role role = Role.builder()
                .roleName(RoleType.CUSTOMER)
                .build();

        User user = User.builder()
                .userId(6L)
                .username("radha")
                .email("radha@gmail.com")
                .password("encoded-password")
                .role(role)
                .enabled(true)
                .build();

        RefreshToken oldRefreshToken = RefreshToken.builder()
                .id(1L)
                .tokenHash("hashed-old-token")
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenService.validateRefreshToken(rawRefreshToken))
                .thenReturn(oldRefreshToken);

        when(jwtService.generateToken(
                eq("6"),
                any()
        )).thenReturn("new-access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn("new-refresh-token");


        // Act

        RefreshResult result =
                userService.refreshAccessToken(rawRefreshToken);


        // Assert

        assertNotNull(result);

        assertEquals(
                "new-access-token",
                result.getAccessToken()
        );

        assertEquals(
                "new-refresh-token",
                result.getRefreshToken()
        );

        verify(refreshTokenService)
                .revokeRefreshToken(oldRefreshToken);

        verify(refreshTokenService)
                .createRefreshToken(user);
    }
    @Test
    void refreshAccessToken_shouldThrowException_whenRefreshTokenIsInvalid() {

        // Arrange
        String rawRefreshToken = "invalid-refresh-token";

        when(refreshTokenService.validateRefreshToken(rawRefreshToken))
                .thenThrow(new InvalidCredentialsException("Invalid token"));

        // Act + Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.refreshAccessToken(rawRefreshToken)
        );

        assertEquals(
                "Invalid token",
                exception.getMessage()
        );

        // Make sure token rotation never happened
        verify(refreshTokenService, never())
                .revokeRefreshToken(any());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }
    @Test
    void logOut_shouldRevokeRefreshToken() {

        // Arrange

        String rawRefreshToken = "refresh-token";

        RefreshToken refreshToken = new RefreshToken();

        when(refreshTokenService.findByRawToken(rawRefreshToken))
                .thenReturn(refreshToken);


        // Act

        userService.logOut(rawRefreshToken);


        // Assert

        verify(refreshTokenService)
                .findByRawToken(rawRefreshToken);

        verify(refreshTokenService)
                .revokeRefreshToken(refreshToken);
    }
}
