package org.inboxview.app.user.service;

import org.inboxview.app.config.JwtService;
import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationResponseDto authenticate(
        final AuthenticationRequestDto request
    ) {
        final var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        final var authentication = authenticationManager.authenticate(authToken);

        final var token = jwtService.generateToken(request.username());

        return new AuthenticationResponseDto(token);
    }
    
}
