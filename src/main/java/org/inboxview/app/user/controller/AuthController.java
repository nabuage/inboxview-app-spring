package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.service.AuthenticationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(
        @RequestBody final AuthenticationRequestDto request
    ) {        
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    
    
}
