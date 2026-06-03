package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void register_newUser_savesSuccessfully() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        authService.register(new RegisterRequest("test@example.com", "password123"));

        verify(userRepository).save(argThat(u ->
            u.getEmail().equals("test@example.com") &&
            u.getRole().equals("CUSTOMER")
        ));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("test@example.com", "password123")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setRole("CUSTOMER");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password123"), eq("hashed"))).thenReturn(true);
        when(jwtUtil.generateToken(any(), eq("test@example.com"), eq("CUSTOMER"))).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("test@example.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    void login_unknownEmail_throwsException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "pass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_wrongPassword_throwsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setRole("CUSTOMER");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrongpass"), eq("hashed"))).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrongpass")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid credentials");
    }
}
