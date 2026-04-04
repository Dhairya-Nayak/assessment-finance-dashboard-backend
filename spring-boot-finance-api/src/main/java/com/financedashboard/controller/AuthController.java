package com.financedashboard.controller;

import com.financedashboard.dto.request.LoginRequest;
import com.financedashboard.dto.request.RefreshTokenRequest;
import com.financedashboard.dto.request.RegisterRequest;
import com.financedashboard.dto.response.ApiResponse;
import com.financedashboard.dto.response.AuthResponse;
import com.financedashboard.security.CurrentUser;
import com.financedashboard.security.UserPrincipal;
import com.financedashboard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh requested");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser UserPrincipal currentUser) {
        log.info("Logout for user: {}", currentUser.getUsername());
        authService.logout(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserResponse>> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        AuthResponse.UserResponse userResponse = AuthResponse.UserResponse.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .roles(currentUser.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}
