package com.management.controller;

import com.management.dto.response.UserProfileResponse;
import com.management.service.UserService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current authenticated user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {

        String email = principal.getUsername();

        return ResponseEntity.ok(
                userService.getCurrentUserProfile(email)
        );
    }
}