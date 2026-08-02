package com.pradip.banksphere.service.user;

import com.pradip.banksphere.dto.request.RegisterRequest;
import com.pradip.banksphere.dto.response.RegisterResponse;

import javax.management.relation.RoleNotFoundException;

public interface UserService {
    RegisterResponse register(RegisterRequest registerRequest);
}
