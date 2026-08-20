package com.pradip.banksphere.service.impl;

import com.pradip.banksphere.dto.request.LoginRequest;
import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.LoginResponse;
import com.pradip.banksphere.dto.response.LoginResult;
import com.pradip.banksphere.dto.response.RefreshResult;
import com.pradip.banksphere.dto.response.RegisterResponse;
import com.pradip.banksphere.entity.refresh.RefreshToken;
import com.pradip.banksphere.entity.role.Role;
import com.pradip.banksphere.entity.user.User;
import com.pradip.banksphere.enums.RoleType;
import com.pradip.banksphere.exception.EmailAlreadyExistsException;
import com.pradip.banksphere.exception.RoleNotFoundException;
import com.pradip.banksphere.repository.role.RoleRepository;
import com.pradip.banksphere.repository.user.UserRepository;
import com.pradip.banksphere.security.CustomUserDetails;
import com.pradip.banksphere.service.jwt.JwtService;
import com.pradip.banksphere.service.refresh.RefreshTokenService;
import com.pradip.banksphere.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {

        validateEmail(registerRequest);

        Role customerRole = getCustomerRole();

        String encodedPassword = encodePassword(registerRequest.getPassword());

        User user = buildUser(registerRequest, customerRole, encodedPassword);

        User savedUser = userRepository.save(user);

        return buildResponse(savedUser);


    }

    @Override
    @Transactional
    public LoginResult login(LoginRequest loginRequest) {


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );



        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();
        user.setLastLogin(LocalDateTime.now());
        String accessToken = jwtService.generateToken(String.valueOf(user.getUserId()), authentication.getAuthorities());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        LoginResponse loginResponse =  buildLoginResponse(user, accessToken);
        return LoginResult.builder()
                .loginResponse(loginResponse)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public RefreshResult refreshAccessToken(String rawRefreshToken) {
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);
        User user = oldRefreshToken.getUser();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(String.valueOf(user.getUserId()),
                customUserDetails.getAuthorities());

        refreshTokenService.revokeRefreshToken(oldRefreshToken);
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        return RefreshResult.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logOut(String rawRefreshToken) {

        RefreshToken refreshToken = refreshTokenService.findByRawToken(rawRefreshToken);

        refreshTokenService.revokeRefreshToken(refreshToken);

    }

    private static LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .message("Login Successful")
                .build();
    }


    private static RegisterResponse buildResponse(User savedUser) {
        return RegisterResponse.builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("Registration Successful")
                .build();
    }

    private String encodePassword(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }

    private static User buildUser(RegisterRequest registerRequest, Role customerRole, String encodedPassword) {
        return User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .role(customerRole)
                .enabled(true)
                .password(encodedPassword)
                .build();
    }

    private Role getCustomerRole() {
        return roleRepository.findByRoleName(RoleType.CUSTOMER)
                .orElseThrow(() -> new RoleNotFoundException("Role does not exists"));
    }

    private void validateEmail(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email Already Exists");
        }
    }
}
