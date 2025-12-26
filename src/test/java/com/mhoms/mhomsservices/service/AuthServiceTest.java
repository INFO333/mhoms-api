package com.mhoms.mhomsservices.service;

import com.mhoms.mhomsservices.dto.AuthResponse;
import com.mhoms.mhomsservices.dto.LoginRequest;
import com.mhoms.mhomsservices.dto.RegisterRequest;
import com.mhoms.mhomsservices.model.Role;
import com.mhoms.mhomsservices.model.User;
import com.mhoms.mhomsservices.repository.UserRepository;
import com.mhoms.mhomsservices.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests registration, login, and token generation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFullName("Test User");
        registerRequest.setRole(Role.PATIENT);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(Role.PATIENT);
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("accessToken123");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refreshToken123");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken123");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("Password123");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegister_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalStateException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegister_EmailExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalStateException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Authentication successful
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(testUser)).thenReturn("accessToken123");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refreshToken123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken123");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void testLogin_UserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_Success() {
        // Arrange
        String refreshToken = "validRefreshToken";
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken(refreshToken, testUser)).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn("newAccessToken123");

        // Act
        AuthResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken123");
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should throw exception for invalid refresh token")
    void testRefreshToken_InvalidToken() {
        // Arrange
        String refreshToken = "invalidRefreshToken";
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken(refreshToken, testUser)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid refresh token");
    }
}