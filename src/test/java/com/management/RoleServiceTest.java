package com.management;

import com.management.entity.Role;
import com.management.entity.User;
import com.management.exception.*;
import com.management.repository.RoleRepository;
import com.management.repository.UserRepository;
import com.management.service.AuditService;
import com.management.service.RoleService;
import com.management.service.imp.AuditServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditServiceImpl auditService;

    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // CREATE ROLE SUCCESS
    // =========================
    @Test
    void createRole_ShouldSaveRole_WhenNotExists() {

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.empty());

        roleService.createRole("ADMIN");

        verify(roleRepository).save(any(Role.class));
    }

    // =========================
    // CREATE ROLE FAIL
    // =========================
    @Test
    void createRole_ShouldThrowException_WhenDuplicate() {

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(new Role()));

        assertThrows(DuplicateRoleException.class,
                () -> roleService.createRole("ADMIN"));
    }

    // =========================
    // ASSIGN ROLE SUCCESS
    // =========================
    @Test
    void assignRole_ShouldAssignRole_WhenValid() {

        User user = User.builder()
                .id(1L)
                .roles(new HashSet<>())
                .build();

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(role));

        roleService.assignRole(1L, "ADMIN");

        verify(userRepository).save(user);
        verify(auditService)
                .log("ROLE_ASSIGNED:ROLE_ADMIN", 1L);
    }

    // =========================
    // ASSIGN ROLE FAIL
    // =========================
    @Test
    void assignRole_ShouldThrowException_WhenAlreadyAssigned() {

        Role role = Role.builder().name("ROLE_ADMIN").build();

        User user = User.builder()
                .id(1L)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(role));

        assertThrows(RoleAlreadyAssignedException.class,
                () -> roleService.assignRole(1L, "ADMIN"));
    }
}