package com.management.controller;

import com.management.service.RoleService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role creation and assignment")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create new role (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role created"),
            @ApiResponse(responseCode = "409", description = "Role already exists")
    })
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createRole(@RequestParam String name) {

        roleService.createRole(name);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Role created successfully");
    }

    @Operation(summary = "Assign role to user (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role assigned"),
            @ApiResponse(responseCode = "404", description = "User or role not found"),
            @ApiResponse(responseCode = "409", description = "Role already assigned")
    })
    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignRole(
            @PathVariable Long userId,
            @RequestParam String roleName) {

        roleService.assignRole(userId, roleName);

        return ResponseEntity.ok("Role assigned successfully");
    }
}