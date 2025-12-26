package com.mhoms.mhomsservices.security;

import com.mhoms.mhomsservices.model.Role;
import com.mhoms.mhomsservices.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtUtil
 * Tests JWT token generation, validation, and extraction
 */
@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Set test values using reflection (simulating @Value injection)
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY",
                "mhoms-super-secret-key-for-jwt-token-generation-minimum-256-bits-required-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L); // 7 days

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
    @DisplayName("Should generate valid access token")
    void testGenerateToken_Success() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken_Success() {
        // Act
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        // Assert
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername_Success() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void testExtractExpiration_Success() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date()); // Token should not be expired
    }

    @Test
    @DisplayName("Should validate token successfully")
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, testUser);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate token with wrong username")
    void testValidateToken_WrongUsername_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        User differentUser = new User();
        differentUser.setUsername("differentuser");

        // Act
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Refresh token should have longer expiration than access token")
    void testTokenExpiration_RefreshTokenLonger() {
        // Arrange
        String accessToken = jwtUtil.generateToken(testUser);
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        // Act
        Date accessExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshExpiration = jwtUtil.extractExpiration(refreshToken);

        // Assert
        assertThat(refreshExpiration).isAfter(accessExpiration);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        // Act
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Assert
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extractUsername(token1)).isEqualTo("user1");
        assertThat(jwtUtil.extractUsername(token2)).isEqualTo("user2");
    }

    @Test
    @DisplayName("Should generate different tokens on consecutive calls")
    void testGenerateToken_ConsecutiveCalls_DifferentTokens() throws InterruptedException {
        // Act
        String token1 = jwtUtil.generateToken(testUser);
        Thread.sleep(1000);
        String token2 = jwtUtil.generateToken(testUser);

        // Assert
        assertThat(token1).isNotEqualTo(token2); // Different due to timestamp
    }

    @Test
    @DisplayName("Token should contain username in subject claim")
    void testExtractUsername_MatchesUserDetails() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Should handle token validation with same user")
    void testValidateToken_SameUser_AlwaysValid() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        Boolean isValid1 = jwtUtil.validateToken(token, testUser);
        Boolean isValid2 = jwtUtil.validateToken(token, testUser);

        // Assert
        assertThat(isValid1).isTrue();
        assertThat(isValid2).isTrue();
    }
}