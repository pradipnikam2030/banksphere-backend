package com.pradip.banksphere.service.impl;

import com.pradip.banksphere.dto.request.LoginRequest;
import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.LoginResponse;
import com.pradip.banksphere.dto.response.RegisterResponse;
import com.pradip.banksphere.entity.role.Role;
import com.pradip.banksphere.entity.user.User;
import com.pradip.banksphere.enums.RoleType;
import com.pradip.banksphere.exception.AccountIsNotEnabledException;
import com.pradip.banksphere.exception.EmailAlreadyExistsException;
import com.pradip.banksphere.exception.InvalidCredentialsException;
import com.pradip.banksphere.exception.RoleNotFoundException;
import com.pradip.banksphere.repository.role.RoleRepository;
import com.pradip.banksphere.repository.user.UserRepository;
import com.pradip.banksphere.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


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
    public LoginResponse login(LoginRequest loginRequest) {
        User user = findUserOrThrow(loginRequest.getEmail());

        validatePassword(loginRequest.getPassword(), user.getPassword());

        validateAccount(user);

        updateLastLogin(user);

        return buildLoginResponse(user);

    }

    private void validatePassword(String rawPassword, String userPassword) {
        if (!passwordEncoder.matches(rawPassword, userPassword)) {
            throw new InvalidCredentialsException("Invalid Email or Password");
        }
    }

    private User findUserOrThrow(String email) {
       return userRepository.findByEmail(email).orElseThrow(() ->
                new InvalidCredentialsException("Invalid Email or Password"));

    }

    private LoginResponse buildLoginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .message("Successfully Login")
                .build();
    }

    private void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
    }

    private void validateAccount(User user) {
        if (!user.getEnabled()) {
            throw new AccountIsNotEnabledException("Account is not Enabled");
        }
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
