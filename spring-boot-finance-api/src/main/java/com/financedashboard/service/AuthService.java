package com.financedashboard.service;

import com.financedashboard.dto.request.LoginRequest;
import com.financedashboard.dto.request.RefreshTokenRequest;
import com.financedashboard.dto.request.RegisterRequest;
import com.financedashboard.dto.response.AuthResponse;
import com.financedashboard.entity.RefreshToken;
import com.financedashboard.entity.Role;
import com.financedashboard.entity.User;
import com.financedashboard.exception.BadRequestException;
import com.financedashboard.exception.DuplicateResourceException;
import com.financedashboard.exception.TokenRefreshException;
import com.financedashboard.repository.RefreshTokenRepository;
import com.financedashboard.repository.RoleRepository;
import com.financedashboard.repository.UserRepository;
import com.financedashboard.security.JwtTokenProvider;
import com.financedashboard.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Update last login time
        userRepository.updateLastLoginTime(userPrincipal.getId(), LocalDateTime.now());

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = createRefreshToken(userPrincipal);

        // Log the login
        auditService.logLogin(userPrincipal.getId(), userPrincipal.getUsername());

        return buildAuthResponse(userPrincipal, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get default role (Viewer)
        Role viewerRole = roleRepository.findByName("ROLE_VIEWER")
                .orElseThrow(() -> new BadRequestException("Default role not found"));

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(User.UserStatus.ACTIVE)
                .build();

        user.addRole(viewerRole);
        user = userRepository.save(user);

        // Create user principal and generate tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = createRefreshToken(userPrincipal);

        log.info("New user registered: {}", user.getUsername());

        return buildAuthResponse(userPrincipal, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(requestRefreshToken, "Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new TokenRefreshException(requestRefreshToken, "User account is not active");
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);

        return buildAuthResponse(userPrincipal, newAccessToken, requestRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllUserTokensById(userId);
        auditService.logLogout(userId);
        log.info("User logged out: {}", userId);
    }

    private String createRefreshToken(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Revoke existing refresh tokens
        refreshTokenRepository.revokeAllUserTokens(user);

        String token = tokenProvider.generateRefreshToken(userPrincipal);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private AuthResponse buildAuthResponse(UserPrincipal userPrincipal, String accessToken, String refreshToken) {
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getJwtExpiration() / 1000)
                .user(AuthResponse.UserResponse.builder()
                        .id(userPrincipal.getId())
                        .username(userPrincipal.getUsername())
                        .email(userPrincipal.getEmail())
                        .firstName(userPrincipal.getFirstName())
                        .lastName(userPrincipal.getLastName())
                        .roles(roles)
                        .build())
                .build();
    }
}
