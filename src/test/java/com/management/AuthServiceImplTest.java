package com.management;

import com.management.dto.request.LoginRequest;
import com.management.dto.request.RegisterRequest;
import com.management.dto.response.AuthResponse;
import com.management.entity.Role;
import com.management.entity.User;
import com.management.exception.EmailAlreadyExistsException;
import com.management.exception.InvalidCredentialsException;
import com.management.repository.RoleRepository;
import com.management.repository.UserRepository;
import com.management.config.JwtService;
import com.management.mapper.UserMapper;
import com.management.service.AuditService;
import com.management.service.EventPublisherService;
import com.management.service.imp.AuthServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EventPublisherService eventPublisherService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // REGISTER SUCCESS

    @Test
    void register_ShouldSaveUser_WhenValidRequest() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("Password1");

        Role role = Role.builder().name("ROLE_USER").build();
        User user = User.builder().email("test@gmail.com").build();

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userMapper.toEntity(any(), any(), any())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        authService.register(request);

        verify(userRepository).save(user);
        verify(auditService).log(eq("REGISTERED"), any());
        verify(eventPublisherService).publish(any());
    }

    // REGISTER FAIL

    @Test
    void register_ShouldThrowException_WhenEmailExists() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(request));
    }

    // LOGIN SUCCESS

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("Password1");

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());

        verify(auditService).log("LOGIN", 1L);
        verify(eventPublisherService).publish(any());
    }

    // LOGIN FAIL
    @Test
    void login_ShouldThrowException_WhenBadCredentials() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }
}