package org.inboxview.app.user.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.config.JwtService;
import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.entity.RefreshToken;
import org.inboxview.app.user.repository.RefreshTokenRepository;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final static String INVALID_REFRESH_TOKEN = "Invalid refresh token.";
    @Value("${jwt.refresh-token-ttl}")
    private Duration ttl;

    public AuthenticationResponseDto authenticate(
        final AuthenticationRequestDto request
    ) {
        final var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        final var authentication = authenticationManager.authenticate(authToken);

        final var token = jwtService.generateToken(request.username());

        var user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setGuid(UUID.randomUUID().toString());
        refreshToken.setDateAdded(OffsetDateTime.now());
        refreshToken.setExpirationDate(OffsetDateTime.now().plus(ttl));
        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResponseDto(token, refreshToken.getGuid());
    }

    public AuthenticationResponseDto refreshToken(String refreshToken) {
        final var refreshTokenE = refreshTokenRepository
            .findByGuidAndExpirationDateAfter(refreshToken, OffsetDateTime.now())
            .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN));

        final var user = userRepository.findById(refreshTokenE.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_REFRESH_TOKEN));

        final var token = jwtService.generateToken(user.getUsername());

        return new AuthenticationResponseDto(token, refreshToken);
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByGuid(refreshToken);
    }
}
