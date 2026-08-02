package com.pradip.banksphere.service.impl;

import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.RegisterResponse;
import com.pradip.banksphere.entity.role.Role;
import com.pradip.banksphere.entity.user.User;
import com.pradip.banksphere.enums.RoleType;
import com.pradip.banksphere.exception.EmailAlreadyExistsException;
import com.pradip.banksphere.exception.RoleNotFoundException;
import com.pradip.banksphere.repository.role.RoleRepository;
import com.pradip.banksphere.repository.user.UserRepository;
import com.pradip.banksphere.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {

        validateEmail(registerRequest);

        Role customerRole = getCustomerRole();

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = buildUser(registerRequest, customerRole, encodedPassword);

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("Registration Successful")
                .build();


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
        if (userRepository.existsByEmail(registerRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email Already Exists");
        }
    }
}
