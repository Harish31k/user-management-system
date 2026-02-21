package com.management.service;

import com.management.entity.Role;
import com.management.entity.User;
import com.management.exception.DuplicateRoleException;
import com.management.exception.RoleAlreadyAssignedException;
import com.management.exception.RoleNotFoundException;
import com.management.exception.UserNotFoundException;
import com.management.repository.RoleRepository;
import com.management.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // Create new role

    public void createRole(String roleName) {

        String fullRoleName = roleName.startsWith("ROLE_")
                ? roleName
                : "ROLE_" + roleName;

        if (roleRepository.findByName(fullRoleName).isPresent()) {
            throw new DuplicateRoleException("Role already exists: " + fullRoleName);
        }

        Role role = Role.builder()
                .name(fullRoleName)
                .build();

        roleRepository.save(role);

    }

    //Assign role to user

    public void assignRole(Long userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + userId));

        String fullRoleName = roleName.startsWith("ROLE_")
                ? roleName
                : "ROLE_" + roleName;

        Role role = roleRepository.findByName(fullRoleName)
                .orElseThrow(() ->
                        new RoleNotFoundException("Role not found: " + fullRoleName));

        if (user.getRoles().contains(role)) {
            throw new RoleAlreadyAssignedException(
                    "User already has role: " + fullRoleName);
        }

        user.getRoles().add(role);
        userRepository.save(user);

        //  Audit Log for Role Assignment
        auditService.log("ROLE_ASSIGNED:" + fullRoleName, userId);
    }
}