package org.inboxview.app.user.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.config.JwtService;
import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.dto.RefreshTokenRequestDto;
import org.inboxview.app.user.entity.RefreshToken;
import org.inboxview.app.user.repository.RefreshTokenRepository;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final static String INVALID_CREDENTIALS = "Invalid credentials.";
    private final static String NOT_VERIFIED = "User is not verified.";

    @Value("${jwt.refresh-token-ttl}")
    private Duration ttl;

    public AuthenticationResponseDto authenticate(
        final AuthenticationRequestDto request
    ) {
        final var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        try {
            authenticationManager.authenticate(authToken);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        var user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        if (user.getDateVerified() == null) {
            throw new InvalidRequest(NOT_VERIFIED);
        }

        final var accessToken = jwtService.generateToken(request.username());

        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .accessToken(accessToken)
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(ttl))
            .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResponseDto(accessToken, refreshToken.getGuid());
    }

    public AuthenticationResponseDto refreshToken(
        final RefreshTokenRequestDto request
    ) {
        final var refreshToken = refreshTokenRepository
            .findByGuidAndAccessTokenAndExpirationDateAfter(
                request.refreshToken(),
                request.accessToken(),
                OffsetDateTime.now()
            )
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        final var user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        final var accessToken = jwtService.generateToken(user.getUsername());

        refreshToken.setAccessToken(accessToken);
        refreshToken.setExpirationDate(OffsetDateTime.now().plus(ttl));

        return new AuthenticationResponseDto(accessToken, request.refreshToken());
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByGuid(refreshToken);
    }
}
