package com.pradip.banksphere.repository.role;

import com.pradip.banksphere.entity.role.Role;
import com.pradip.banksphere.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
   Optional <Role> findByRoleName(RoleType role);
}
